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
package br.ufsc.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.RankingAlgorithm;
import org.apache.commons.math3.util.Pair;

import br.ufsc.mov3lets.method.discovery.structures.DiscoveryAdapter;
import br.ufsc.mov3lets.method.discovery.structures.TrajectoryDiscovery;
import br.ufsc.mov3lets.method.filter.BestMoveletsFilter;
import br.ufsc.mov3lets.method.filter.LastPrunningMoveletsFilter;
import br.ufsc.mov3lets.method.filter.MoveletsFilter;
import br.ufsc.mov3lets.method.filter.MoveletsFilterRanker;
import br.ufsc.mov3lets.method.filter.MoveletsRanker;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.Subtrajectory;
import br.ufsc.mov3lets.model.aspect.Aspect;

/**
 * The Class MoveletsDiscovery.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 * @param <MO> the generic type
 */
public abstract class MoveletsDiscovery<MO> extends DiscoveryAdapter<MO> implements TrajectoryDiscovery {
	
	/** The quality measure. */
	protected QualityMeasure qualityMeasure = null;
	
	/** The ranking algorithm. */
	protected RankingAlgorithm rankingAlgorithm = new NaturalRanking();
	
	/** The base. */
	protected double[][][][] base;
	
	/** The max distances. */
	protected double[] maxDistances; // Max distances by dimension
	
	protected MoveletsFilter bestFilter = new BestMoveletsFilter(0.0, 0.0);
	protected MoveletsRanker qualityRanker = new MoveletsFilterRanker(0.0);
	
	/**
	 * Instantiates a new movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor the descriptor
	 */	
	public MoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, QualityMeasure qualityMeasure, 
			Descriptor descriptor) {
		super(trajectory, trajsFromClass, data, train, test, descriptor);
		this.qualityMeasure = qualityMeasure;
		init();
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE DISCOVERING PROCEDURES: * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * ** * * * * * * * * * * * * * * * * * * * *.
	 *
	 * @return the list
	 */

	/**
	 * Looks for candidates in the trajectory, then compares with every other trajectory
	 */
	public List<Subtrajectory> discover() {
		
//		int n = this.data.size();
		int maxSize = getDescriptor().getParamAsInt("max_size");
		int minSize = getDescriptor().getParamAsInt("min_size");

		/** STEP 2.1: Starts at discovering movelets */
//		progressBar.trace("Class: " + trajsFromClass.get(0).getMovingObject() + "."); // Might be saved in HD
//		Mov3letsUtils.getInstance().startTimer("\tClass >> " + trajectory.getClass());
		
		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();

		progressBar.trace("Movelets Discovery for Class [No MEM]: " + trajsFromClass.get(0).getMovingObject() 
				+ ". Trajectory: " + trajectory.getTid());
		
//		for (MAT<MO> trajectory : trajsFromClass) {
			// This guarantees the reproducibility
			Random random = new Random(trajectory.getTid());

			/** STEP 2.1: --------------------------------- */
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.train, minSize, maxSize, random);
			
			/** STEP 2.4: SELECTING BEST CANDIDATES */			
//			candidates = filterMovelets(candidates);		
//			movelets.addAll(filterMovelets(candidates));
			movelets.addAll(candidates);
			
//			System.gc();
//		}
		
		/** STEP 2.2: Runs the pruning process */
		if(getDescriptor().getFlag("last_prunning"))
			movelets = lastPrunningFilter(movelets);

		/** STEP 2.2: ---------------------------- */
		outputMovelets(movelets);
		/** -------------------------------------- */
//		System.gc();
		
		return movelets;
				
	}

	/**
	 * Method to output movelets. It is synchronized by thread. 
	 * 
	 * @param movelets
	 */
	public void outputMovelets(List<Subtrajectory> movelets) {
		/** STEP 2.3.1: Output Movelets (partial) */
//		synchronized (DiscoveryAdapter.class) {
		this.lock.getWriteLock().lock();
		
		super.output("train", this.train, movelets, true);
		
		// Compute distances and best alignments for the test trajectories:
		/* If a test trajectory set was provided, it does the same.
		 * and return otherwise */
		/** STEP 2.3.2: Output Movelets (partial) */
		if (!this.test.isEmpty()) {
//			base = computeBaseDistances(trajectory, this.test);
			for (Subtrajectory candidate : movelets) {
				// It initializes the set of distances of all movelets to null
				candidate.setDistances(null);
				// In this step the set of distances is filled by this method
				computeDistances(candidate, this.test); //, computeBaseDistances(trajectory, this.test));
			}
			super.output("test", this.test, movelets, true);
		}
		
		this.lock.getWriteLock().unlock();
//		}
	}

	/**
	 * Movelets discovery.
	 *
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param minSize the min size
	 * @param maxSize the max size
	 * @param random the random
	 * @return the list
	 */
	public List<Subtrajectory> moveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajectories, int minSize, int maxSize, Random random) {
		List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();
		
		int n = trajectory.getPoints().size();

		minSize = minSize(minSize, n);
		maxSize = maxSize(maxSize, minSize, n);

		// It starts with the base case	
		int size = 1;
		Integer total_size = 0;
		
		if( minSize <= 1 ) {
			candidates.addAll(findCandidates(trajectory, trajectories, size));
			candidates.forEach(x -> assesQuality(x, random));
		}				
		
		total_size = total_size + candidates.size();
		
		// Tratar o resto dos tamanhos 
		for (size = 2; size <= maxSize; size++) {
			
			if (size >= minSize){

				// Create candidates and compute min distances		
				List<Subtrajectory> candidatesOfSize = findCandidates(trajectory, trajectories, size);
				
				total_size = total_size + candidatesOfSize.size();
				
				//for (Subtrajectory candidate : candidatesOfSize) assesQuality(candidate);				
				candidatesOfSize.forEach(x -> assesQuality(x, random));
				candidates.addAll(candidatesOfSize);
			}
						
		} // for (int size = 2; size <= max; size++)	
		
		candidates = this.bestFilter.filter(candidates);
		
		progressBar.plus("Class: " + trajectory.getMovingObject() 
						+ ". Trajectory: " + trajectory.getTid() 
						+ ". Trajectory Size: " + trajectory.getPoints().size() 
						+ ". Number of Candidates: " + total_size 
						+ ". Total of Movelets: " + candidates.size() 
						+ ". Max Size: " + maxSize
						+ ". Used Features: " + this.maxNumberOfFeatures);
				
		return candidates;
	}

	protected int minSize(int minSize, int n) {
		switch (minSize) {			
			// LOG Window size:
			case -2: minSize = ((int) Math.ceil(Math.log(n))+1); break;	
			
			case -1:
			default: minSize = -1;
		}
		return minSize;
	}

	protected int maxSize(int maxSize, int minSize, int n) {
		// TO USE THE LOG, PUT "-Ms -3"
		switch (maxSize) {
			case -1: maxSize = n; break;
			case -2: maxSize = (int) Math.round( Math.log10(n) / Math.log10(2) ); break;	
			case -3: maxSize = (int) Math.ceil(Math.log(n))+1; break;		
			
			// LOG Window size:
			case -4: maxSize = minSize + ((int) Math.ceil(Math.log(n))+1); break;	
			
			// Max Size is the value set:
			default: break;
		}
		return maxSize;
	}

	/**
	 * [THE GREAT GAP].
	 *
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param size the size
	 * @return the list
	 */
	public List<Subtrajectory> findCandidates(MAT<MO> trajectory, List<MAT<MO>> trajectories, int size) {
		
		// Trajectory P size => n
		int n = trajectory.getPoints().size();
		int[][] combinations = makeCombinations(exploreDimensions, numberOfFeatures, maxNumberOfFeatures);
		
		// List of Candidates to extract from P:
		List<Subtrajectory> candidates = new ArrayList<>();
		

		// From point 0 to (n - <candidate max. size>) 
		for (int start = 0; start <= (n - size); start++) {
//			Point p = trajectory.getPoints().get(start);
			
			// Extract possible candidates from P to max. candidate size:
			List<Subtrajectory> list = buildSubtrajectory(start, start + size - 1, trajectory, trajectories.size(), combinations);
									
			// For each trajectory in the database
			for (int i = 0; i < trajectories.size(); i++) {
				MAT<MO> T = trajectories.get(i);	
				
				for (Subtrajectory subtrajectory : list) {						
					double[] distances = bestAlignmentByPointFeatures(subtrajectory, T).getSecond();
					for (int j = 0; j < subtrajectory.getPointFeatures().length; j++) {
						subtrajectory.getDistances()[j][i] = distances[j]; //Math.sqrt(distances[j] / size);							
					}
				}
				
			} // for (int currentFeatures = 1; currentFeatures <= numberOfFeatures; currentFeatures++)
			
			candidates.addAll(list);

		} // for (int start = 0; start <= (n - size); start++)
		
		return candidates;
		
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE DISTANCE MATRIX:         * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * ** * * * * * * * * * * * * * * * * * * .
	/**
	 * Compute base distances.
	 *
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @return the double[][][][]
	 */
	public double[][][][] computeBaseDistances(MAT<?> trajectory, List<MAT<MO>> trajectories){
		int n = trajectory.getPoints().size();
		int size = 1;
		
		double[][][][] base = new double[(n - size)+1][][][];		
		
		for (int start = 0; start <= (n - size); start++) {
			
			base[start] = new double[trajectories.size()][][];				
			
			for (int i = 0; i < trajectories.size(); i++) {
				
				MAT<?> T = trajectories.get(i);
				Point a = trajectory.getPoints().get(start);
								
				base[start][i] = new double[getDescriptor().getAttributes().size()][(trajectories.get(i).getPoints().size()-size)+1];
						
				for (int j = 0; j <= (T.getPoints().size()-size); j++) {
					Point b = T.getPoints().get(j);
					

					for (int k = 0; k < getDescriptor().getAttributes().size(); k++) {
						AttributeDescriptor attr = getDescriptor().getAttributes().get(k);						
						base[start][i][k][j] = 
								calculateDistance(
										a.getAspects().get(k), 
										b.getAspects().get(k), 
										attr
								);

					} // for (int k = 0; k < distance.length; k++)
					
				} // for (int j = 0; j <= (train.size()-size); j++)
				
			} //for (int i = 0; i < train.size(); i++)
			
		} // for (int start = 0; start <= (n - size); start++)

		return base;
	}

	public abstract double calculateDistance(Aspect<?> aspect, Aspect<?> aspect2, AttributeDescriptor attr);

	/**
	 * Clone 4 D array.
	 *
	 * @param source the source
	 * @return the double[][][][]
	 */
	public double[][][][] clone4DArray(double [][][][] source){
		double[][][][] dest = new double[source.length][][][];
		for (int i = 0; i < dest.length; i++) {
			dest[i] = new double[source[i].length][][];
			for (int j = 0; j < dest[i].length; j++) {
				dest[i][j] = new double[source[i][j].length][];
				for (int k = 0; k < dest[i][j].length; k++) {
					dest[i][j][k] = new double[source[i][j][k].length];
					for (int k2 = 0; k2 < source[i][j][k].length; k2++) {
						dest[i][j][k][k2] = source[i][j][k][k2];
					}
				}
			}
		}
		return dest;		
	}

	/**
	 * New size.
	 *
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param base the base
	 * @param lastSize the last size
	 * @param size the size
	 * @return the double[][][][]
	 */
	public double[][][][] newSize(MAT<?> trajectory, List<MAT<MO>> trajectories, double[][][][] base, double[][][][] lastSize, int size) {
		
		int n = trajectory.getPoints().size();	
		
		for (int start = 0; start <= (n - size); start++) {
						
			for (int i = 0; i < trajectories.size(); i++) {
				
				if (trajectories.get(i).getPoints().size() >= size) {						
							
					for (int j = 0; j <= (trajectories.get(i).getPoints().size()-size); j++) {
												
						for (int k = 0; k < lastSize[start][i].length; k++) {
							
							if (lastSize[start][i][k][j] != MAX_VALUE)
								
								lastSize[start][i][k][j] += base[start+size-1][i][k][j+size-1];
						
						} // for (int k = 0; k < distance.length; k++) {
											
					} // for (int j = 0; j <= (train.size()-size); j++)
					
				} // if (train.get(i).getData().size() >= size) 
				
			} // for (int i = 0; i < train.size(); i++)
			
		} // for (int start = 0; start <= (n - size); start++)
		
		return lastSize;
	}

	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * BEST ALIGNMENT:                           * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * ** * * * * * * * * * * * * * * * * * * .
	/**
	 * Best alignment by point features.
	 *
	 * @param s the s
	 * @param t the t
	 * @return the pair
	 */
	public Pair<Subtrajectory, double[]> bestAlignmentByPointFeatures(Subtrajectory s, MAT<MO> t) {
		double[] maxValues = new double[numberOfFeatures];
		Arrays.fill(maxValues, MAX_VALUE);
				
		if (s.getSize() > t.getPoints().size())
			return new Pair<>(null, maxValues);

		List<Point> menor = s.getPoints();
		List<Point> maior = t.getPoints();
		
		int size =  s.getSize();
		int diffLength = maior.size() - size;	
		int limit = maior.size() - size + 1;		
				
		int[] comb = s.getPointFeatures();
		double[] currentSum; 
		double[] values = new double[numberOfFeatures];
		double[][] distancesForT = new double[comb.length][diffLength+1];
				
		for (int i = 0; i <= diffLength; i++) {

			currentSum = new double[comb.length];
						
			for (int j = 0; j < size; j++) {

				// Here we get from mdist:
				values = getDistances(menor.get(j), maior.get(i + j), s.getPointFeatures());

				for (int k = 0; k < comb.length; k++) {					
					if (currentSum[k] != MAX_VALUE && values[k] != MAX_VALUE)
						currentSum[k] += values[k]; 
					else {
						currentSum[k] = MAX_VALUE;
					}
				}											
				
			}
			
			for (int k = 0; k < comb.length; k++) {
				distancesForT[k][i] = currentSum[k];
			}
		}
		
		double[][] ranksForT = new double[distancesForT.length][];
		
		if (limit > 0)
			for (int k = 0; k < comb.length; k++) {
				ranksForT[k] = rankingAlgorithm.rank(Arrays.stream(distancesForT[k],0,limit).toArray());
			} // for (int k = 0; k < numberOfFeatures; k++)
		
		
		int bestPosition = (limit > 0) ? bestAlignmentByRanking(ranksForT,comb) : -1;
		
		double[] bestAlignment = new double[comb.length];
		
		for (int j = 0; j < comb.length; j++) {
			
//			double distance = distancesForT[j][bestPosition];
			double distance = (bestPosition >= 0) ? distancesForT[j][bestPosition] : MAX_VALUE;
			
			bestAlignment[j] = (distance != MAX_VALUE) ? 
					Math.sqrt( distance / size ) : MAX_VALUE;
			
		} // for (int j = 0; j < comb.length; j++)
		
		int start = bestPosition;
		int end = bestPosition + size - 1;
		
//		return bestAlignment;
		return new Pair<>(new Subtrajectory(start, end , t), bestAlignment);
	}
	
	/**
	 * Best alignment by ranking.
	 *
	 * @param ranksForT the ranks for T
	 * @param comb the comb
	 * @return the int
	 */
	public int bestAlignmentByRanking(double[][] ranksForT, int[] comb) {
		
		double[] rankMerged = new double[ranksForT[0].length];
		
		if (ranksForT.length > 1)
			// In case it's a combination of more than one dimension
			for (int i = 0; i < comb.length; i++) {
				for (int j = 0; j < ranksForT[0].length; j++) {
	//				rankMerged[j] += ranksForT[comb[i]][j];
					rankMerged[j] += ranksForT[i][j]; // It's indexed differently now
				}
			}
		else
			// W/ one dimention, no need to merge (use directly)
			rankMerged = ranksForT[0];

		int minRankIndex = 0;
		for (int j = 1; j < rankMerged.length; j++) {
			if (rankMerged[j] < rankMerged[minRankIndex])
				minRankIndex = j;
		}
		
		return minRankIndex;
	}

	/** The k. */
	protected int K = 0;
	
	/**
	 * Builds the subtrajectory.
	 *
	 * @param start the start
	 * @param end the end
	 * @param t the t
	 * @param numberOfTrajectories the number of trajectories
	 * @param combinations the combinations
	 * @return the list
	 */
	public List<Subtrajectory> buildSubtrajectory(
			int start, int end, MAT<MO> t, int numberOfTrajectories, int[][] combinations){
		
		List<Subtrajectory> list = new ArrayList<>();
		
		for (int k = 0; k < combinations.length; k++) {
			list.add(new Subtrajectory(start, end, t, numberOfTrajectories, combinations[k], K++));
		}
				
		return list;
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE QUALITY ASSESMENT:    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * ** * * * * * * * * * * * * * * * * * * *.
	 */

	/**
	 * Compute quality.
	 *
	 * @param candidates the best candidates
	 * @param random the random
	 * @param trajectory the trajectory
	 * @return the list
	 */
	public void computeQuality(List<Subtrajectory> candidates, Random random, MAT<MO> trajectory) {
		/** STEP 2.3, for this trajectory movelets: 
		 * It transforms the training and test sets of trajectories using the movelets */
		for (Subtrajectory candidate : candidates) {
			// It initializes the set of distances of all movelets to null
			candidate.setDistances(null);
			candidate.setQuality(null);
			// In this step the set of distances is filled by this method
			computeDistances(candidate, this.train);

			/* STEP 2.1.6: QUALIFY BEST HALF CANDIDATES 
			 * * * * * * * * * * * * * * * * * * * * * * * * */
			assesQuality(candidate, random); //TODO change?
		}
	}
	
	/**
	 * @param candidate
	 * @param random
	 * @return
	 */
	public void assesQuality(Subtrajectory candidate, Random random) {
		qualityMeasure.assesQuality(candidate, random);
	}
	
	/**
	 * Asses quality.
	 *
	 * @param candidate the candidate
	 */
	public void assesQuality(Subtrajectory candidate) {
		qualityMeasure.assesQuality(candidate, new Random());
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE OUTPUT TRANSFORMATIONS:     * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * ** * * * * * * * * * * * * * * * * * * .
	 *
	 * @param candidates the candidates
	 * @param trajectories the trajectories
	 * @param file the file
	 * @return *
	 */

//	public List<Subtrajectory> transformTrajectoryOutput(List<Subtrajectory> candidates, List<MAT<MO>> trajectories, String file) {
//		for (Subtrajectory movelet : candidates) {
//			// It initializes the set of distances of all movelets to null
//			movelet.setDistances(null);
//			// In this step the set of distances is filled by this method
//			computeDistances(movelet, trajectories); // computeDistances(movelet, trajectories);
//		}
//
//		/** STEP 2.5: SELECTING BEST CANDIDATES */			
//		candidates = filterMovelets(candidates); //TODO is necessary?
//		
//		/** STEP 3.0: Output Movelets (partial) */
//		super.output(file, trajectories, candidates, true);
//		
//		return candidates;
//	}
	
	/**
	 * Compute distances.
	 *
	 * @param candidate the candidate
	 * @param trajectories the trajectories
	 */
	public void computeDistances(Subtrajectory candidate, List<MAT<MO>> trajectories) {
		/* This pairs will store the subtrajectory of the best alignment 
		 * of the candidate into each trajectory and the distance 
		 * */
		Pair<Subtrajectory, double[]> distance;
		
		double[][] trajectoryDistancesToCandidate = new double[candidate.getPointFeatures().length]
															  [trajectories.size()];
		
		List<Subtrajectory> bestAlignments = new ArrayList<Subtrajectory>();
				
		/* It calculates the distance of trajectories to the candidate
		 */
		for (int i = 0; i < trajectories.size(); i++) {
			
			distance = bestAlignmentByPointFeatures(candidate, trajectories.get(i));
			
			for (int j = 0; j < candidate.getPointFeatures().length; j++) {
				trajectoryDistancesToCandidate[j][i] = distance.getSecond()[j];							
			}
						
			bestAlignments.add(distance.getFirst());		
		}
		
		candidate.setDistances(trajectoryDistancesToCandidate);
		candidate.setBestAlignments(bestAlignments);
	}
	
	/**
	 * Gets the distances.
	 *
	 * @param a the a
	 * @param b the b
	 * @param comb the comb
	 * @return the distances
	 */
	public double[] getDistances(Point a, Point b, int[] comb) {
		double[] distances = new double[comb.length];
		
		int i = 0;
		for (int k : comb) {
			AttributeDescriptor attr = this.descriptor.getAttributes().get(k);
			
			distances[i++] = 
					calculateDistance(
							a.getAspects().get(k), 
							b.getAspects().get(k), 
							attr
					); // This also normalize and enhance distances
		}
		
		return distances;
		
	}
	
	/**
	 * First vector greater than the second.
	 *
	 * @param first the first
	 * @param second the second
	 * @return true, if successful
	 */
	public boolean firstVectorGreaterThanTheSecond(double [] first, double [] second){
		
		for (int i = 0; i < first.length; i++) {
			if (first[i] <= second[i])
				return false;
		}
		
		return true;
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE FILTERING PROCEDURES: * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * ** * * * * * * * * * * * * * * * * * * *.
	 *
	 * @param candidates the candidates
	 * @return the list
	 */
	
//	/**
//	 * 
//	 * @param candidates
//	 * @return
//	 */
//	public List<Subtrajectory> filterMovelets(List<Subtrajectory> candidates) {
//
////		List<Subtrajectory> orderedCandidates = rankCandidates(candidates);
//
////		return bestShapelets(orderedCandidates, 0);
//		return new BestMoveletsFilter(0).filter(candidates);
//	}
	
//	/**
//	 * Rank candidates.
//	 *
//	 * @param candidates the candidates
//	 * @return the list
//	 */
//	public List<Subtrajectory> rankCandidates(List<Subtrajectory> candidates) {
//
//		List<Subtrajectory> orderedCandidates = new ArrayList<>(candidates);
//		
//		orderedCandidates.removeIf(e -> e == null);
//		
//		orderedCandidates.sort(new Comparator<Subtrajectory>() {
//			@Override
//			public int compare(Subtrajectory o1, Subtrajectory o2) {
//				
//				return o1.getQuality().compareTo(o2.getQuality());				
//				
//			}
//		});
//
//		return orderedCandidates;
//	}
	

	/**
	 * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE PRUNNING PROCEDURES: ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * ** * * * * * * * * * * * * * * * * * * *.
	 *
	 * @param movelets the movelets
	 * @return the list
	 */
	
	/**
	 * Last Prunning Method
	 * 
	 * @param candidates
	 * @return
	 */
	public List<Subtrajectory> lastPrunningFilter(List<Subtrajectory> movelets) {
		return new LastPrunningMoveletsFilter(getDescriptor()).filter(movelets);
	}

}
