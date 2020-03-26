/**
 * Mov3lets - Multiple Aspect Trajectory (MASTER) Classification Version 3. 
 * Copyright (C) 2019  Tarlis Portela <tarlis@tarlis.com.br>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package br.com.tarlis.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.RankingAlgorithm;
import org.apache.commons.math3.util.Pair;

import br.com.tarlis.mov3lets.method.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.method.output.OutputterAdapter;
import br.com.tarlis.mov3lets.method.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.method.structures.Matrix3D;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Point;
import br.com.tarlis.mov3lets.model.Subtrajectory;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;
import br.com.tarlis.mov3lets.utils.ProgressBar;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class PrecomputeMoveletsDiscovery<MO> extends MoveletsDiscovery<MO> {
	
	private static Matrix3D base = null;
	
	/**
	 * @param trajectory
	 * @param train
	 * @param candidates 
	 */
	public PrecomputeMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> train, List<MAT<MO>> test, List<Subtrajectory> candidates, QualityMeasure qualityMeasure, 
			Descriptor descriptor) {
		super(trajectory, train, test, candidates, qualityMeasure, descriptor);
		
//		initBaseCases(this.data, getDescriptor().getParamAsInt("nthreads"), getDescriptor());
//		if (base == null) {
//			int N_THREADS = getDescriptor().getParamAsInt("nthreads");
//			if (N_THREADS > 1)
//				multithreadComputeBaseDistances(this.data, N_THREADS, getDescriptor());
//			else 
//				computeBaseDistances(this.data);
//			System.gc();
//		}
	}
	
	public  static <MO> void initBaseCases(List<MAT<MO>> data, int N_THREADS, Descriptor descriptor) {
		if (base == null) {
			if (N_THREADS > 1)
				multithreadComputeBaseDistances(data, N_THREADS, descriptor);
			else 
				computeBaseDistances(data, descriptor);
			System.gc();
		}
	}
	
	/*** * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE DISCOVERING PROCEDURES: * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 *** * * * * * * * * * * * * * * * * * * * **/

	/**
	 * @param trajectory2
	 * @param data2
	 * @param minSize
	 * @param maxSize
	 * @param random
	 * @return
	 */
	public List<Subtrajectory> moveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajectories, int minSize, int maxSize, Random random) {
		List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();

		int n = trajectory.getPoints().size();
		
		// TO USE THE LOG, PUT "-Ms -3"
		switch (maxSize) {
			case -1: maxSize = n; break;
			case -2: maxSize = (int) Math.round( Math.log10(n) / Math.log10(2) ); break;	
			case -3: maxSize = (int) Math.ceil(Math.log(n))+1; break;	
			default: break;
		}

		// It starts with the base case	
		int size = 1;
		Integer total_size = 0;
		
		Matrix3D baseCase = getDescriptor().getParamAsInt("nthreads") > 1? base.clone() : base;
		
		if( minSize <= 1 ) {
			candidates.addAll(findCandidates(trajectory, trajectories, size, baseCase));
			candidates.forEach(x -> assesQuality(x, random));
		}				
		
//		Matrix3D lastSize = base; //(Matrix4D) base.clone(); // TODO: maybe need for override...		

		total_size = total_size + candidates.size();
		
		// Tratar o resto dos tamanhos 
		for (size = 2; size <= maxSize; size++) {
	
			// Precompute de distance matrix
//			newSize(trajectory, this.data, lastSize, size);
//			double[][][][] newSize = getNewSize(trajectory, this.data, base, lastSize, size);
			
			// Create candidates and compute min distances		
			List<Subtrajectory> candidatesOfSize = findCandidates(trajectory, this.data, size, baseCase);
		
			total_size = total_size + candidatesOfSize.size();
			
			if (size >= minSize){
				
				//for (Subtrajectory candidate : candidatesOfSize) assesQuality(candidate);				
				candidatesOfSize.forEach(x -> assesQuality(x, random));
				
				//candidatesOfSize = MoveletsFilterAndRanker.getShapelets(candidatesOfSize);
				
				candidates.addAll(candidatesOfSize);
			}
		
//			lastSize = newSize;
						
		} // for (int size = 2; size <= max; size++)	
	
		baseCase =  null;
//		lastSize = null;

		candidates = filterMovelets(candidates);
		
		progressBar.trace("Class: " + trajectory.getMovingObject() + ". Trajectory: " + trajectory.getTid() + ". Trajectory Size: " + trajectory.getPoints().size() + ". Number of Candidates: " + total_size + ". Total of Movelets: " + candidates.size() + ". Max Size: " + maxSize+ ". Used Features: " + this.maxNumberOfFeatures);

		return candidates;
	}

	/**
	 * [THE GREAT GAP]
	 * 
	 * @param trajectory
	 * @param trajectories
	 * @param bar 
	 * @return
	 */
	
//	public Matrix3D computeBaseDistances_bkp(MAT<?> trajectory, List<MAT<MO>> trajectories){
////		int index = trajectories.indexOf(trajectory);
//		int n = trajectory.getPoints().size();
//		int size = 1;
//
//		base = new Matrix3D(exploreDimensions, numberOfFeatures, maxNumberOfFeatures);
//		ProgressBar bar = new ProgressBar("Computing Base Distances", 
//				trajectory.getPoints().size() * 2);
//		
//		for (int start = 0; start <= (n - size); start++) {		
//			Point a = trajectory.getPoints().get(start);
//			
//			for (int k = 0; k < trajectories.size(); k++) {						
//				MAT<?> T = trajectories.get(k);
//				
//				for (int j = 0; j <= (T.getPoints().size()-size); j++) {
//					Point b = T.getPoints().get(j);
//					
//					double[] distances = new double[getDescriptor().getAttributes().size()];
//					
//					for (int i = 0; i < getDescriptor().getAttributes().size(); i++) {
//						AttributeDescriptor attr = getDescriptor().getAttributes().get(i);
//						distances[i] = getDescriptor().getAttributes().get(i)
//								.getDistanceComparator().calculateDistance(
//								a.getAspects().get(i), 
//								b.getAspects().get(i), 
//								attr); // This also enhance distances
//					}
//
//					// For each possible *Number Of Features* and each combination of those:
//					base.addDistances(a, b, distances);
//					
//				} // for (int j = 0; j <= (train.size()-size); j++)
//				
//			} //for (MAT<?> T : trajectories) { --//-- for (int i = 0; i < train.size(); i++)
//			
//			bar.plus();
//			
//		} // for (int start = 0; start <= (n - size); start++)
//
//		return base;
//	}
	
//	private void multithreadComputeBaseDistances_bkp(List<MAT<MO>> trajectories, int N_THREADS) {
//		ProgressBar bar = new ProgressBar("Computing Base Distances", 
//				Mov3letsUtils.getInstance().totalPoints((List) trajectories));
//		
//		base = new Matrix3D(exploreDimensions, numberOfFeatures, maxNumberOfFeatures);
//		
//		ExecutorService executor = (ExecutorService) 
//				Executors.newFixedThreadPool(N_THREADS);
//		List<Future<Matrix3D>> futures = new ArrayList<Future<Matrix3D>>();
//		
//		for (int i = 0; i < trajectories.size(); i++) {
//			Callable<Matrix3D> task = new PrecomputeBaseDistances<MO>(i, trajectories, 
//					new Matrix3D(base.getCombinations()), 
//					getDescriptor(), bar);
//			futures.add(executor.submit(task));
//		}
//		
//		for (Future<Matrix3D> future : futures) {
//			try {
//				base.putAll(future.get());
//				future.cancel(true);
//				System.gc();
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
//			}
//		}
//		executor.shutdown();
//	}

	public static <MO> Matrix3D computeBaseDistances(List<MAT<MO>> trajectories, Descriptor descriptor){
//		int index = trajectories.indexOf(trajectory);
		int size = 1;

		base = new Matrix3D(descriptor.getFlag("explore_dimensions"),
				descriptor.numberOfFeatures(), 
				descriptor.getParamAsInt("max_number_of_features"));
		ProgressBar bar = new ProgressBar("Computing Base Distances", 
				Mov3letsUtils.getInstance().totalPoints((List) trajectories));

		for (int fromIndex = 0; fromIndex < trajectories.size(); fromIndex++) {				
			MAT<?> trajectory = trajectories.get(fromIndex);
			int n = trajectory.getPoints().size();
			
			for (int start = 0; start <= (n - size); start++) {		
				Point a = trajectory.getPoints().get(start);
				
				for (int k = fromIndex+1; k < trajectories.size(); k++) {						
					MAT<?> T = trajectories.get(k);
					
					for (int j = 0; j <= (T.getPoints().size()-size); j++) {
						Point b = T.getPoints().get(j);
						
						double[] distances = new double[descriptor.getAttributes().size()];
						
						for (int i = 0; i < descriptor.getAttributes().size(); i++) {
							AttributeDescriptor attr = descriptor.getAttributes().get(i);
							distances[i] = descriptor.getAttributes().get(i)
									.getDistanceComparator().calculateDistance(
									a.getAspects().get(i), 
									b.getAspects().get(i), 
									attr); // This also enhance distances
						}
	
						// For each possible *Number Of Features* and each combination of those:
						base.addDistances(a, b, distances);
						
					} // for (int j = 0; j <= (train.size()-size); j++)
					
				} //for (MAT<?> T : trajectories) { --//-- for (int i = 0; i < train.size(); i++)
				
				bar.plus();
				
			} // for (int start = 0; start <= (n - size); start++)
			
		} // for (int m = 0; m < trajectories.size(); m++) {	

		return base;
	}
	
	public static <MO> void multithreadComputeBaseDistances(List<MAT<MO>> trajectories, int N_THREADS, Descriptor descriptor) {
		ProgressBar bar = new ProgressBar("Computing Base Distances", 
				Mov3letsUtils.getInstance().totalPoints((List) trajectories));

		base = new Matrix3D(descriptor.getFlag("explore_dimensions"),
							descriptor.numberOfFeatures(), 
							descriptor.getParamAsInt("max_number_of_features"));
		
		ExecutorService executor = (ExecutorService) 
				Executors.newFixedThreadPool(N_THREADS);
		List<Future<Matrix3D>> futures = new ArrayList<Future<Matrix3D>>();
		
		for (int i = 0; i < trajectories.size(); i++) {
			Callable<Matrix3D> task = new PrecomputeBaseDistances<MO>(i, trajectories, 
					new Matrix3D(base.getCombinations()), 
					descriptor, bar);
			futures.add(executor.submit(task));
		}
		
		for (Future<Matrix3D> future : futures) {
			try {
				base.putAll(future.get());
//				future.cancel(true);
				System.gc();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		executor.shutdown();
	}

	/**
	 * 
	 * [THE GREAT GAP]
	 * 
	 * @param trajectory
	 * @param train
	 * @param size
	 * @param mdist
	 * @return
	 */
	public List<Subtrajectory> findCandidates(MAT<MO> trajectory, List<MAT<MO>> train, int size, Matrix3D mdist) {
		
		// Trajectory P size => n
		int n = trajectory.getPoints().size();
		int[][] combinations = makeCombinations(exploreDimensions, numberOfFeatures, maxNumberOfFeatures);
		
		// List of Candidates to extract from P:
		List<Subtrajectory> candidates = new ArrayList<>();

		// From point 0 to (n - <candidate max. size>) 
		for (int start = 0; start <= (n - size); start++) {
//			Point p = trajectory.getPoints().get(start);
			
			// Extract possible candidates from P to max. candidate size:
			List<Subtrajectory> list = buildSubtrajectory(start, start + size - 1, trajectory, train.size(), combinations);
									
			// For each trajectory in the database
			for (int i = 0; i < train.size(); i++) {
				MAT<MO> T = train.get(i);	
				
				int limit = T.getPoints().size() - size + 1;
				
				if (limit > 0)
					for (Subtrajectory subtrajectory : list) {
						subtrajectory.getDistances()[i] = bestAlignmentByPointFeatures(subtrajectory, T).getSecond();
					}
				
			} // for (int currentFeatures = 1; currentFeatures <= numberOfFeatures; currentFeatures++)
			
			candidates.addAll(list);

		} // for (int start = 0; start <= (n - size); start++)
		
		return candidates;
		
	}
	
	/*** * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE QUALITY ASSESMENT:    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 *** * * * * * * * * * * * * * * * * * * **/
	
	/**
	 * @param x
	 * @param random
	 * @return
	 */
	public void assesQuality(Subtrajectory candidate, Random random) {
		qualityMeasure.assesQuality(candidate, random);
	}
	
	/*** * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE DISTANCES & BEST ALIGNMENT BY POINT:    * * * * * * * * * * * * * * * * * * * * >>
	 *** * * * * * * * * * * * * * * * * * * **/
	
	// TODO: esse método é um problema, tem que ver como fazer isso e para que serve.
	public void computeDistances(Subtrajectory candidate) {
		/* This pairs will store the subtrajectory of the best alignment 
		 * of the candidate into each trajectory and the distance 
		 * */
		Pair<Subtrajectory, double[]> distance;
		
		double[][] trajectoryDistancesToCandidate = new double[candidate.getSplitpoints().length]
															  [this.data.size()];
		
		List<Subtrajectory> bestAlignments = new ArrayList<Subtrajectory>();
				
		/* It calculates the distance of trajectories to the candidate
		 */
		for (int i = 0; i < this.data.size(); i++) {
			
			distance = bestAlignmentByPointFeatures(candidate, this.data.get(i));
						
			bestAlignments.add(distance.getFirst());
			trajectoryDistancesToCandidate[i] = distance.getSecond();			
		}
		
		candidate.setDistances(trajectoryDistancesToCandidate);
		candidate.setBestAlignments(bestAlignments);
	}
	
//	public Pair<Subtrajectory, double[]> bestAlignment(Subtrajectory s, MAT<MO> t) {
//		
//		double[] maxValues = new double[s.getPointFeatures().length];
//		Arrays.fill(maxValues, Double.MAX_VALUE);
//		
//		/*
//		 * If subtrajectory is largest than trajectory return maximum distance
//		 */
//		if (s.getSize() > t.getPoints().size())
//			return new Pair<>(null,maxValues);
//
//		/*
//		 * Compute min distance
//		 */
////		if (featureComparisonDescToPoints != null) {
////			if (featureComparisonDescToSubtrajectories.size() > 0)
//////				return getBestAlignmentByPointAndSubtrajectoryFeatures(s, t);
////			else
//				return getBestAlignmentByPointFeatures(s, t);
////		} else {
////			if (featureComparisonDescToSubtrajectories.size() > 0)
//////				return getBestAlignmentSubtrajectoryFeatures(s, t);
////			else
////				return new Pair<>(null,maxValues);
////		}
//		// By Rank - ADPT: Tarlis
//	}
	
//	public Pair<Subtrajectory, double[]> getBestAlignmentByPointFeatures(Subtrajectory s, MAT<MO> t) {
//
//		double[] maxValues = new double[numberOfFeatures];
//		Arrays.fill(maxValues, Double.POSITIVE_INFINITY);
//				
//		if (s.getSize() > t.getPoints().size())
//			return new Pair<>(null,maxValues);
//
//		List<Point> menor = s.getPoints();
//		List<Point> maior = t.getPoints();
//		
//		int diffLength = maior.size() - menor.size();		
//				
//		int[] comb = s.getPointFeatures();
//		double currentSum[] = new double[comb.length];
//		double[] values = new double[numberOfFeatures];
//		double[][] distancesForT = new double[comb.length][diffLength+1];
//						
//		double[] x = new double[comb.length];
//		Arrays.fill(x, Double.POSITIVE_INFINITY);
//				
//		for (int i = 0; i <= diffLength; i++) {
//
//			Arrays.fill(currentSum, 0);
//						
//			for (int j = 0; j < menor.size(); j++) {
//
//				values = getDistances(menor.get(j), maior.get(i + j));
//
//				for (int k = 0; k < comb.length; k++) {					
//					if (currentSum[k] != Double.POSITIVE_INFINITY && values[k] != Double.POSITIVE_INFINITY)
//						currentSum[k] += values[comb[k]] * values[comb[k]];
//					else {
//						currentSum[k] = Double.POSITIVE_INFINITY;
//					}
//				}
//				
//				
//				if (firstVectorGreaterThanTheSecond(currentSum, x) ){
//					for (int k = 0; k < comb.length; k++) {
//						currentSum[k] = Double.POSITIVE_INFINITY;
//					}					
//					break;					
//				} 											
//				
//			}
//			
//			if (firstVectorGreaterThanTheSecond(x, currentSum) ){
//				for (int k = 0; k < comb.length; k++) {
//					x[k] = currentSum[k];					
//				}				
//			}
//			
//			for (int k = 0; k < comb.length; k++) {
//				distancesForT[k][i] = currentSum[k];
//			}
//		}
//		
//		double[][] ranksForT = new double[distancesForT.length][];
//		
//		for (int k = 0; k < comb.length; k++) {
//			ranksForT[k] = rankingAlgorithm.rank(distancesForT[k]);
//		} // for (int k = 0; k < numberOfFeatures; k++)
//		
//		
//		int bestPosition = bestAlignmentByRanking(ranksForT,comb);
//		
//		double[] bestAlignment = new double[comb.length];
//		
//		for (int j = 0; j < comb.length; j++) {
//			
//			double distance = distancesForT[j][bestPosition];
//			
//			bestAlignment[j] = 
//					(distance != Double.POSITIVE_INFINITY) ? Math.sqrt(distance / menor.size()) 
//												   : Double.POSITIVE_INFINITY;
//			
//		} // for (int j = 0; j < comb.length; j++)
//		
//		int start = bestPosition;
//		int end = bestPosition + menor.size() - 1;
//		
//		return new Pair<>(new Subtrajectory(start, end , t), bestAlignment);
//	}
	
	public double[] getDistances(Point a, Point b) {

//		double[] distances = new double[this.descriptor.getAttributes().size()];
//		
//		for (int i = 0; i < this.descriptor.getAttributes().size(); i++) {
//			AttributeDescriptor attr = this.descriptor.getAttributes().get(i);
//			
//			distances[i] = attr.getDistanceComparator().calculateDistance(
//					a.getAspects().get(i), 
//					b.getAspects().get(i), 
//					attr); // This also enhance distances
//		}
//		
//		return distances;
		
		return base.get(a, b).stream()
				.mapToDouble(Double::doubleValue)
				.toArray(); 
		
	}
	

	/*** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE OUTPUT PROCEDURES: * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 *** * * * * * * * * * * * * * * * * * * **/

//	// TODO
//	public void transformOutput(List<Subtrajectory> candidates, List<MAT<MO>> trajectories, String filename) {
//		for (Subtrajectory movelet : candidates) {
//			// It initializes the set of distances of all movelets to null
//			movelet.setDistances(null);
//			// In this step the set of distances is filled by this method
//			computeDistances(movelet, trajectories);
//		}
//		
//		/** STEP 3.0: Output Movelets */
//		super.output(filename, trajectories, candidates);
//	}

}
