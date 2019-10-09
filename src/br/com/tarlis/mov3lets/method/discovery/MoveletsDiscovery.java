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
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.RankingAlgorithm;
import org.apache.commons.math3.util.Combinations;

import br.com.tarlis.mov3lets.model.mat.MAT;
import br.com.tarlis.mov3lets.model.mat.Point;
import br.com.tarlis.mov3lets.model.mat.Subtrajectory;
import br.com.tarlis.mov3lets.model.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.utils.Matrix4D;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;
import br.com.tarlis.mov3lets.view.AttributeDescriptor;
import br.com.tarlis.mov3lets.view.Descriptor;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class MoveletsDiscovery<MO> extends DiscoveryAdapter<MO> {

	private int numberOfFeatures = 1;
	private int maxNumberOfFeatures = 2;
	private boolean exploreDimensions;
	
	private QualityMeasure qualityMeasure = null;
	private RankingAlgorithm rankingAlgorithm = new NaturalRanking();
	
	/**
	 * @param trajectory
	 * @param train
	 * @param candidates 
	 */
	public MoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> train, List<Subtrajectory> candidates, QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajectory, train, candidates, descriptor);
		
		this.qualityMeasure = qualityMeasure;
		
		this.numberOfFeatures = getDescriptor().getParamAsInt("max_number_of_features");
		this.exploreDimensions = getDescriptor().getFlag("explore_dimensions");
		
		switch (maxNumberOfFeatures) {
			case -1: this.maxNumberOfFeatures = numberOfFeatures; break;
			case -2: this.maxNumberOfFeatures = (int) Math.ceil(Math.log(numberOfFeatures))+1; break;
			default: break;
		}
	}
	
	/*** * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE DISCOVERING PROCEDURES: * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 *** * * * * * * * * * * * * * * * * * * * **/

	/**
	 * Looks for candidates in the trajectory, then compares with every other trajectory
	 */
	protected void discover() {

		// This guarantees the reproducibility
		Random random = new Random(this.trajectory.getTid());
		
		int n = this.data.size();
		int maxSize = getDescriptor().getParamAsInt("max_size");
		maxSize = (maxSize == -1) ? n : maxSize;
		int minSize = getDescriptor().getParamAsInt("min_size");
		
		List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.data, minSize, maxSize, random);
		
		// TODO
		for (Subtrajectory candidate : candidates) {
			/** STEP 1: COMPUTE DISTANCES, IF NOT COMPUTED YET */
			if (candidate.getDistances() == null) {	
//				computeDistances(candidate);
				System.out.println("TODO? COMPUTE DISTANCES MD-96");
			}
			
			/** STEP 2: ASSES QUALITY, IF REQUIRED */
			if (qualityMeasure != null & candidate.getQuality() != null) {
				assesQuality(candidate, random);
				System.out.println("TODO? ASSES QUALITY, IF REQUIRED MD-102");
			}
		}
		
		/** STEP 3: SELECTING BEST CANDIDATES */	
		getCandidates().addAll(rankCandidates(candidates));
		
//		int numberOfCandidates = (maxSize * (maxSize-1) / 2);
		
	}
	
	/**
	 * @param trajectory2
	 * @param data2
	 * @param minSize
	 * @param maxSize
	 * @param random
	 * @return
	 */
	private List<Subtrajectory> moveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajectories, int minSize, int maxSize, Random random) {
		List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();
		
		int n = trajectory.getAspects().size();
		
		// TO USE THE LOG, PUT "-Ms -3"
		switch (maxSize) {
			case -1: maxSize = n; break;
			case -2: maxSize = (int) Math.round( Math.log10(n) / Math.log10(2) ); break;	
			case -3: maxSize = (int) Math.ceil(Math.log(n))+1; break;	
			default: break;
		}
		
//		int numberOfCandidates = (maxSize * (maxSize-1) / 2);
		// It starts with the base case	
		int size = 1;

		Integer total_size = 0;
		
		Matrix4D base = computeBaseDistances(trajectory, trajectories);
		
		if( minSize <= 1 ) {
			candidates.addAll(findCandidates(trajectory, this.data, size, base));
			candidates.forEach(x -> assesQuality(x, random));
		}				
		
		Matrix4D lastSize = (Matrix4D) base.clone(); // TODO: maybe need for override...			

		total_size = total_size + candidates.size();
		
		// Tratar o resto dos tamanhos 
		for (size = 2; size <= maxSize; size++) {
	
			// Precompute de distance matrix
			newSize(trajectory, this.data, lastSize, size);
//			double[][][][] newSize = getNewSize(trajectory, this.data, base, lastSize, size);
			
			// Create candidates and compute min distances		
			List<Subtrajectory> candidatesOfSize = findCandidates(trajectory, this.data, size, lastSize);
		
			total_size = total_size + candidatesOfSize.size();
			
			if (size >= minSize){
				
				//for (Subtrajectory candidate : candidatesOfSize) assesQuality(candidate);				
				candidatesOfSize.forEach(x -> assesQuality(x, random));
				
				//candidatesOfSize = MoveletsFilterAndRanker.getShapelets(candidatesOfSize);
				
				candidates.addAll(candidatesOfSize);
			}
		
//			lastSize = newSize;
						
		} // for (int size = 2; size <= max; size++)	
	
		base =  null;
//		lastSize = null;
		
		candidates = filterMovelets(candidates);
		
		Mov3letsUtils.trace("\tTrajectory: " + trajectory.getTid() + ". Trajectory Size: " + trajectory.getPoints().size() + ". Number of Candidates: " + total_size + ". Total of Movelets: " + candidates.size() + ". Max Size: " + maxSize+ ". Used Features: " + this.maxNumberOfFeatures);
		
		return candidates;
	}
	
	/**
	 * [THE GREAT GAP]
	 * 
	 * @param trajectory
	 * @param trajectories
	 * @return
	 */
	private Matrix4D computeBaseDistances(MAT<?> trajectory, List<MAT<MO>> trajectories){
		int n = trajectory.getPoints().size();
		int size = 1;
		
		Matrix4D base = new Matrix4D();		
		
		for (int start = 0; start <= (n - size); start++) {
			
//			base[start] = new double[train.size()][][];				
			
			for (MAT<?> T : trajectories) {
//			for (int i = 0; i < train.size(); i++) {
								
//				base[start][i] = new double[dmbt.getDMBP().getNumberOfFeatures()][(train.get(i).getData().size()-size)+1];
						
				for (int j = 0; j <= (T.getPoints().size()-size); j++) {
					Point a = trajectory.getPoints().get(start);
					Point b = T.getPoints().get(j);
					
					if (!base.contains(a, b))
						for (AttributeDescriptor attr : this.descriptor.getAttributes()) {
							double distance = attr.getDistanceComparator().calculateDistance(
									a.getAspects().get(attr.getText()), 
									b.getAspects().get(attr.getText()), 
									attr); // This also enhance distances
							
							base.add(a, b, distance);
						}
					
				} // for (int j = 0; j <= (train.size()-size); j++)
				
			} //for (MAT<?> T : trajectories) { --//-- for (int i = 0; i < train.size(); i++)
			
		} // for (int start = 0; start <= (n - size); start++)

		return base;
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
	private List<Subtrajectory> findCandidates(MAT<MO> trajectory, List<MAT<MO>> train, int size, Matrix4D mdist) {
		
		// Trajectory P size => n
		int n = trajectory.getPoints().size();
		
		// List of Candidates to extract from P:
		List<Subtrajectory> candidates = new ArrayList<>();

		// From point 0 to (n - <candidate max. size>) 
		for (int start = 0; start <= (n - size); start++) {
			Point p = trajectory.getPoints().get(start);
			
			// Extract possible candidates from P to max. candidate size:
			List<Subtrajectory> list = buildSubtrajectory(start, start + size - 1, trajectory, numberOfFeatures, train.size(), exploreDimensions, maxNumberOfFeatures);
			
			// TODO: Retrieve distances from smaller sizes
//			double[][][] distancesForAllT = mdist[start];
			
			double distance;
			
			// For each trajectory in the database
//			for (MAT<MO> T : train) {
			for (int i = 0; i < train.size(); i++) {
				MAT<MO> T = train.get(i);				
				
				double[][] distancesForT = new double[maxNumberOfFeatures][];
				double[][] ranksForT = new double[maxNumberOfFeatures][];

				int limit = T.getPoints().size() - size + 1;	
//				int limit = train.get(i).getPoints().size() - size + 1;				
				
				if (limit > 0)
					for (int k = 0; k < numberOfFeatures; k++) {	
						distancesForT[k] = mdist.distancesForAspect(k, p, T);
						ranksForT[k] = rankingAlgorithm.rank(Arrays.stream(distancesForT[k],0,limit).toArray());
					} // for (int k = 0; k < numberOfFeatures; k++)
				
				int k2 = 0;
				int currentFeatures;
				if (exploreDimensions){
					currentFeatures = 1;
				} else {
					currentFeatures = numberOfFeatures;
				}
				
				// For each possible NumberOfFeatures and each combination of those: 
				for (;currentFeatures <= maxNumberOfFeatures; currentFeatures++) {
					for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) {
												
						// Finds the best alignment to each T trajectory:
						int bestPosition = (limit > 0) ? bestAlignmentByRanking(ranksForT,comb) : -1;					
						// Process distances ...
						for (int j = 0; j < comb.length; j++) {
							
							distance = (bestPosition >= 0) ? distancesForT[comb[j]][bestPosition] : Double.POSITIVE_INFINITY;
							list.get(k2).getDistances()[j][i] = 
									(distance != Double.POSITIVE_INFINITY) ? Math.sqrt(distance / size) 
																   : Double.POSITIVE_INFINITY;
							
						} // for (int j = 0; j < comb.length; j++)
												
						k2++;
						
					} // for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) 					
				} // for (int i = 0; i < train.size(); i++)
				
			} // for (int currentFeatures = 1; currentFeatures <= numberOfFeatures; currentFeatures++)
			
			candidates.addAll(list);

		} // for (int start = 0; start <= (n - size); start++)
		
		return candidates;
		
	}

	/**
	 * @param trajectory
	 * @param data
	 * @param base
	 * @param size
	 */
	private void newSize(MAT<MO> trajectory, List<MAT<MO>> train, Matrix4D lastSize, int size) {
		for (Point p : trajectory.getPoints()) {
			for (MAT<MO> T : train) {
				Point q = T.getPoints().get(0);
				for (int i = 1; i < T.getPoints().size()-size; i++) {
					ArrayList<Double> distances = lastSize.get(p,q);
					Point r = T.getPoints().get(i);
					for (int f = 0; f < distances.size(); f++) {
						distances.set(f, 
								distances.get(f) + lastSize.get(p,r).get(f)
						);
					}
					q = r;
				}
			}
		}
	}

	public List<Subtrajectory> buildSubtrajectory(
			int start, int end, MAT<MO> t, 
			int numberOfFeatures, int numberOfTrajectories, 
			boolean exploreDimensions, int maxNumberOfFeatures){
		
		List<Subtrajectory> list = new ArrayList<>();
		
		int currentFeatures;
		if (exploreDimensions)
			currentFeatures = 1;
		else
			currentFeatures = numberOfFeatures;
		
		for (; currentFeatures <= maxNumberOfFeatures; currentFeatures++) {
			
			for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) {
				
					list.add(new Subtrajectory(start, end, t, comb, numberOfTrajectories));
				
			}

		}
				
		return list;
	}
	
	private int bestAlignmentByRanking(double[][] ranksForT, int[] comb) {
		
		double[] rankMerged = new double[ranksForT[0].length];
		for (int i = 0; i < comb.length; i++) {
			for (int j = 0; j < ranksForT[0].length; j++) {
				rankMerged[j] += ranksForT[comb[i]][j];
			}
		}

		int minRankIndex = 0;
		for (int j = 1; j < rankMerged.length; j++) {
			if (rankMerged[j] < rankMerged[minRankIndex])
				minRankIndex = j;
		}
		
		return minRankIndex;
	}
	

	
	/*** * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE QUALITY ASSESMENT:    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 *** * * * * * * * * * * * * * * * * * * **/
	
	/**
	 * @param x
	 * @param random
	 * @return
	 */
	private void assesQuality(Subtrajectory candidate, Random random) {
		qualityMeasure.assesQuality(candidate, random);
	}
	
//	private void computeDistances(Subtrajectory candidate) {
//		
//		/* This pairs will store the subtrajectory of the best alignment 
//		 * of the candidate into each trajectory and the distance 
//		 * */
//		Pair<Subtrajectory,double[]> distance;
//		
//		double[][] trajectoryDistancesToCandidate = new double[candidate.getSplitpoints().length]
//															  [trajectories.size()];
//		
//		Subtrajectory[] bestAlignments = new Subtrajectory[trajectories.size()];
//				
//		/* It calculates the distance of trajectories to the candidate
//		 */
//		for (int i = 0; i < trajectories.size(); i++) {
//			
//			distance = dmbt.getBestAlignment(candidate, trajectories.get(i));
//						
//			bestAlignments[i] = distance.getFirst();
//			trajectoryDistancesToCandidate[i] = distance.getSecond();			
//		}
//		
//		candidate.setDistances(trajectoryDistancesToCandidate);
//		candidate.setBestAlignments(bestAlignments);
//	}
	
	/*** * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE FILTERING PROCEDURES: * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 *** * * * * * * * * * * * * * * * * * * **/
	
	/**
	 * 
	 * @param candidates
	 * @return
	 */
	public List<Subtrajectory> filterMovelets(List<Subtrajectory> candidates) {

		List<Subtrajectory> orderedCandidates = rankCandidates(candidates);

		return bestShapelets(orderedCandidates, 0);
	}
	
	public List<Subtrajectory> rankCandidates(List<Subtrajectory> candidates) {

		List<Subtrajectory> orderedCandidates = new ArrayList<>(candidates);
		
		orderedCandidates.removeIf(e -> e == null);
		
		orderedCandidates.sort(new Comparator<Subtrajectory>() {
			@Override
			public int compare(Subtrajectory o1, Subtrajectory o2) {
				
				return o1.getQuality().compareTo(o2.getQuality());				
				
			}
		});

		return orderedCandidates;
	}

	public List<Subtrajectory> bestShapelets(List<Subtrajectory> rankedCandidates, double selfSimilarityProp) {

		// Realiza o loop até que acabem os atributos ou até que atinga o número
		// máximo de nBestShapelets
		// Isso é importante porque vários candidatos bem rankeados podem ser
		// selfsimilares com outros que tiveram melhor score;
		for (int i = 0; (i < rankedCandidates.size()); i++) {

			// Se a shapelet candidata tem score 0 então já termina o processo
			// de busca
			if (rankedCandidates.get(i).getQuality().hasZeroQuality())
				return rankedCandidates.subList(0, i);

			Subtrajectory candidate = rankedCandidates.get(i);

			// Removing self similar
			if (searchIfSelfSimilarity(candidate, rankedCandidates.subList(0, i), selfSimilarityProp)) {
				rankedCandidates.remove(i);
				i--;
			}

		}

		return rankedCandidates;
	}

	public boolean searchIfSelfSimilarity(Subtrajectory candidate, List<Subtrajectory> list,
			double selfSimilarityProp) {

		for (Subtrajectory s : list) {
			if (areSelfSimilar(candidate, s, selfSimilarityProp))
				return true;
		}

		return false;
	}
	
	public boolean areSelfSimilar(Subtrajectory candidate, Subtrajectory subtrajectory,
			double selfSimilarityProp) {
		
		//return false;
		
		// If their tids are different return false
		
		if (candidate.getTrajectory().getTid() != subtrajectory.getTrajectory().getTid())
			return false;

		else if (candidate.getStart() < subtrajectory.getStart()) {

			if (candidate.getEnd() < subtrajectory.getStart())
				return false;

			if (selfSimilarityProp == 0)
				return true;

			double intersection = (candidate.getEnd() - subtrajectory.getStart())
					/ (double) Math.min(candidate.getSize(), subtrajectory.getSize());

			return intersection >= selfSimilarityProp;

		} else {

			if (subtrajectory.getEnd() < candidate.getStart())
				return false;

			if (selfSimilarityProp == 0)
				return true;

			double intersection = (subtrajectory.getEnd() - candidate.getStart())
					/ (double) Math.min(candidate.getSize(), subtrajectory.getSize());

			return intersection >= selfSimilarityProp;

		}

	}

}
