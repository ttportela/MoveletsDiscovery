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
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.util.Pair;

import br.com.tarlis.mov3lets.method.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.method.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Point;
import br.com.tarlis.mov3lets.model.Subtrajectory;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class BaseCaseMoveletsDiscovery<MO> extends MoveletsDiscovery<MO> {
	
	/**
	 * @param trajectory
	 * @param train
	 * @param test 
	 */	
	public BaseCaseMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> train, List<MAT<MO>> test, List<Subtrajectory> candidates, QualityMeasure qualityMeasure, 
			Descriptor descriptor) {
		super(trajectory, train, test, candidates, qualityMeasure, descriptor);
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
		
		double[][][][] base = computeBaseDistances(trajectory, trajectories);
		
		if( minSize <= 1 ) {
			candidates.addAll(findCandidates(trajectory, trajectories, size, base));
			candidates.forEach(x -> assesQuality(x, random));
		}				
		
		double[][][][] lastSize = clone4DArray(base);		

		total_size = total_size + candidates.size();
		
		// Tratar o resto dos tamanhos 
		for (size = 2; size <= maxSize; size++) {
	
			// Precompute de distance matrix
			double[][][][] newSize = newSize(trajectory, trajectories, base, lastSize, size);

			// Create candidates and compute min distances		
			List<Subtrajectory> candidatesOfSize = findCandidates(trajectory, trajectories, size, lastSize);
		
			total_size = total_size + candidatesOfSize.size();
			
			if (size >= minSize){
				
				//for (Subtrajectory candidate : candidatesOfSize) assesQuality(candidate);				
				candidatesOfSize.forEach(x -> assesQuality(x, random));
				
				candidates.addAll(candidatesOfSize);
			}
		
			lastSize = newSize;
						
		} // for (int size = 2; size <= max; size++)	
	
		base =  null;
		lastSize = null;

		candidates = filterMovelets(candidates);
		
		progressBar.trace("Class: " + trajectory.getMovingObject() + ". Trajectory: " + trajectory.getTid() + ". Trajectory Size: " + trajectory.getPoints().size() + ". Number of Candidates: " + total_size + ". Total of Movelets: " + candidates.size() + ". Max Size: " + maxSize+ ". Used Features: " + this.maxNumberOfFeatures);

		return candidates;
	}
	
	private double[][][][] computeBaseDistances(MAT<?> trajectory, List<MAT<MO>> trajectories){
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
						double distance = attr.getDistanceComparator().calculateDistance(
								a.getAspects().get(k), 
								b.getAspects().get(k), 
								attr);
					
						base[start][i][k][j] = (distance != Double.POSITIVE_INFINITY) ? (distance) : Double.POSITIVE_INFINITY;					
					
					} // for (int k = 0; k < distance.length; k++)
					
				} // for (int j = 0; j <= (train.size()-size); j++)
				
			} //for (int i = 0; i < train.size(); i++)
			
		} // for (int start = 0; start <= (n - size); start++)

		return base;
	}
	
	private double[][][][] clone4DArray(double [][][][] source){
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

	private double[][][][] newSize(MAT<?> trajectory, List<MAT<MO>> trajectories, double[][][][] base, double[][][][] lastSize, int size) {
		
		int n = trajectory.getPoints().size();	
		
		for (int start = 0; start <= (n - size); start++) {
						
			for (int i = 0; i < trajectories.size(); i++) {
				
				if (trajectories.get(i).getPoints().size() >= size) {						
							
					for (int j = 0; j <= (trajectories.get(i).getPoints().size()-size); j++) {
												
						for (int k = 0; k < lastSize[start][i].length; k++) {
							
							if (lastSize[start][i][k][j] != Double.POSITIVE_INFINITY)
								
								lastSize[start][i][k][j] += base[start+size-1][i][k][j+size-1];
						
						} // for (int k = 0; k < distance.length; k++) {
											
					} // for (int j = 0; j <= (train.size()-size); j++)
					
				} // if (train.get(i).getData().size() >= size) 
				
			} // for (int i = 0; i < train.size(); i++)
			
		} // for (int start = 0; start <= (n - size); start++)
		
		return lastSize;
	}

	/**
	 * 
	 * [THE GREAT GAP]
	 * 
	 * @param trajectory
	 * @param trajectories
	 * @param size
	 * @param mdist
	 * @return
	 */
	public List<Subtrajectory> findCandidates(MAT<MO> trajectory, List<MAT<MO>> trajectories, int size, double[][][][] mdist) {
		
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
				
				int limit = T.getPoints().size() - size + 1;
				
				if (limit > 0)
					for (Subtrajectory subtrajectory : list) {						
						double[] distances = bestAlignmentByPointFeatures(subtrajectory, T, mdist).getSecond();
						for (int j = 0; j < subtrajectory.getPointFeatures().length; j++) {
							subtrajectory.getDistances()[j][i] = distances[j];							
						}
					}
				
			} // for (int currentFeatures = 1; currentFeatures <= numberOfFeatures; currentFeatures++)
			
			candidates.addAll(list);

		} // for (int start = 0; start <= (n - size); start++)
		
		return candidates;
		
	}
	
//	private int[][] combinations = null;
//	public int[][] makeCombinations(boolean exploreDimensions, int numberOfFeatures, int maxNumberOfFeatures) {
//		
//		if (combinations != null)
//			return combinations;
//		
//		int currentFeatures;
//		if (exploreDimensions){
//			currentFeatures = 1;
//		} else {
//			currentFeatures = numberOfFeatures;
//		}
//		
//		combinations = new int[(int) (Math.pow(2, maxNumberOfFeatures) - 1)][];
//		int k = 0;
//		// For each possible NumberOfFeatures and each combination of those: 
//		for (;currentFeatures <= maxNumberOfFeatures; currentFeatures++) {
//			for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) {					
//				
//				combinations[k++] = comb;
//				
//			} // for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) 					
//		} // for (int i = 0; i < train.size(); i++
//
//		return combinations;
//	}
	
	public Pair<Subtrajectory, double[]> bestAlignmentByPointFeatures(Subtrajectory s, MAT<MO> t, double[][][][] mdist) {
		double[] maxValues = new double[numberOfFeatures];
		Arrays.fill(maxValues, Double.POSITIVE_INFINITY);
				
		if (s.getSize() > t.getPoints().size())
			return new Pair<>(null, maxValues);

		List<Point> menor = s.getPoints();
		List<Point> maior = t.getPoints();
		
//		int idxs = this.train.indexOf(s.getTrajectory()); 
		int idxt = this.train.indexOf(t); // mdist[idxs][idx];
		int diffLength = maior.size() - menor.size();		
				
		int[] comb = s.getPointFeatures();
		double currentSum[] = new double[comb.length];
//		double[] values = new double[numberOfFeatures];
		double[][] distancesForT = new double[comb.length][diffLength+1];
						
		double[] x = new double[comb.length];
		Arrays.fill(x, Double.POSITIVE_INFINITY);
				
		for (int i = 0; i <= diffLength; i++) {

			Arrays.fill(currentSum, 0);
						
			for (int j = 0; j < menor.size(); j++) {

//				values = getDistances(menor.get(j), maior.get(i + j));
				// Here we get from mdist:
//				double[][][] distancesForAllT = mdist[i];
//				values = mdist[i][j].getBaseDistances(menor.get(j), maior.get(i + j), comb);

				for (int k = 0; k < comb.length; k++) {					
					if (currentSum[k] != Double.POSITIVE_INFINITY && mdist[s.getStart()+j][idxt][k][i+j] != Double.POSITIVE_INFINITY)
						currentSum[k] += mdist[s.getStart()+j][idxt][k][i+j]; //values[comb[k]] * values[comb[k]];
					else {
						currentSum[k] = Double.POSITIVE_INFINITY;
					}
				}
				
				
				if (firstVectorGreaterThanTheSecond(currentSum, x) ){
					for (int k = 0; k < comb.length; k++) {
						currentSum[k] = Double.POSITIVE_INFINITY;
					}					
					break;					
				} 											
				
			}
			
			if (firstVectorGreaterThanTheSecond(x, currentSum) ){
				for (int k = 0; k < comb.length; k++) {
					x[k] = currentSum[k];					
				}				
			}
			
			for (int k = 0; k < comb.length; k++) {
				distancesForT[k][i] = currentSum[k];
			}
		}
		
//		double[][] ranksForT = new double[distancesForT.length][];
		double[][] ranksForT = new double[comb.length][];
		
		for (int k = 0; k < comb.length; k++) {
			ranksForT[k] = rankingAlgorithm.rank(distancesForT[k]);
		} // for (int k = 0; k < numberOfFeatures; k++)
		
		
		int bestPosition = bestAlignmentByRanking(ranksForT,comb);
		
		double[] bestAlignment = new double[comb.length];
		
		for (int j = 0; j < comb.length; j++) {
			
			double distance = distancesForT[j][bestPosition];
			
			bestAlignment[j] = 
					(distance != Double.POSITIVE_INFINITY) ? Math.sqrt(distance / menor.size()) 
												   : Double.POSITIVE_INFINITY;
			
		} // for (int j = 0; j < comb.length; j++)
		
		int start = bestPosition;
		int end = bestPosition + menor.size() - 1;
//		int end = bestPosition + menor.size() - 1;
		
//		return bestAlignment;
		return new Pair<>(new Subtrajectory(start, end , t), bestAlignment);
	}
	
//	public int bestAlignmentByRanking(double[][] ranksForT, int[] comb) {
//		
//		double[] rankMerged = new double[ranksForT[0].length];
//		
////		if (ranksForT.length > 1)
//			// In case it's a combination of more than one dimension
//			for (int i = 0; i < comb.length; i++) {
//				for (int j = 0; j < ranksForT[0].length; j++) {
//					rankMerged[j] += ranksForT[comb[i]][j];
////					rankMerged[j] += ranksForT[i][j]; // It's indexed differently now
//				}
//			}
////		else
////			// It's one dimention, no need to merge (use directly)
////			rankMerged = ranksForT[0];
//
//		int minRankIndex = 0;
//		for (int j = 1; j < rankMerged.length; j++) {
//			if (rankMerged[j] < rankMerged[minRankIndex])
//				minRankIndex = j;
//		}
//		
//		return minRankIndex;
//	}

//	public List<Subtrajectory> buildSubtrajectory(
//			int start, int end, MAT<MO> t, double[][][][] mdist, int numberOfTrajectories, int[][] combinations){
//		
//		List<Subtrajectory> list = new ArrayList<>();
//		
//		for (int k = 0; k < combinations.length; k++) {
//			list.add(new Subtrajectory(start, end, t, numberOfTrajectories, combinations[k], k));
//		}
//				
//		return list;
//	}
	

	
	/*** * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE QUALITY ASSESMENT:    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 *** * * * * * * * * * * * * * * * * * * **/
	
	/**
	 * @param x
	 * @param random
	 * @return
	 */
//	public void assesQuality(Subtrajectory candidate, Random random) {
//		qualityMeasure.assesQuality(candidate, random);
//	}
	
	/*** * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE OUTPUT TRANSFORMATIONS:     * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 *** * * * * * * * * * * * * * * * * * * **/

//	private void transformOutput(List<Subtrajectory> candidates, List<MAT<MO>> trajectories, String filename) {
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
	
	// TODO: esse método é um problema, tem que ver como fazer isso e para que serve.
//	public void computeDistances(Subtrajectory candidate, List<MAT<MO>> trajectories) {
//		/* This pairs will store the subtrajectory of the best alignment 
//		 * of the candidate into each trajectory and the distance 
//		 * */
//		Pair<Subtrajectory, double[]> distance;
//		
//		double[][] trajectoryDistancesToCandidate = new double[candidate.getSplitpoints().length]
//															  [trajectories.size()];
//		
//		List<Subtrajectory> bestAlignments = new ArrayList<Subtrajectory>();
//				
//		/* It calculates the distance of trajectories to the candidate
//		 */
//		for (int i = 0; i < trajectories.size(); i++) {
//			
//			distance = bestAlignmentByPointFeatures(candidate, trajectories.get(i), null);
//			
//			for (int j = 0; j < candidate.getSplitpoints().length; j++) {
//				trajectoryDistancesToCandidate[j][i] = distance.getSecond()[j];							
//			}
//						
//			bestAlignments.add(distance.getFirst());
////			trajectoryDistancesToCandidate[i] = distance.getSecond();			
//		}
//		
//		candidate.setDistances(trajectoryDistancesToCandidate);
//		candidate.setBestAlignments(bestAlignments);
//	}
//	
//	public double[] getDistances(Point a, Point b) {
//
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
//		
//	}
//	
//	public boolean firstVectorGreaterThanTheSecond(double [] first, double [] second){
//		
//		for (int i = 0; i < first.length; i++) {
//			if (first[i] <= second[i])
//				return false;
//		}
//		
//		return true;
//	}
	
	/*** * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE FILTERING PROCEDURES: * * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 *** * * * * * * * * * * * * * * * * * * **/
	
	/**
	 * 
	 * @param candidates
	 * @return
	 */
//	public List<Subtrajectory> filterMovelets(List<Subtrajectory> candidates) {
//
//		List<Subtrajectory> orderedCandidates = rankCandidates(candidates);
//
//		return bestShapelets(orderedCandidates, 0);
//	}
//	
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
//
//	public List<Subtrajectory> bestShapelets(List<Subtrajectory> rankedCandidates, double selfSimilarityProp) {
//
//		// Realiza o loop até que acabem os atributos ou até que atinga o número
//		// máximo de nBestShapelets
//		// Isso é importante porque vários candidatos bem rankeados podem ser
//		// selfsimilares com outros que tiveram melhor score;
//		for (int i = 0; (i < rankedCandidates.size()); i++) {
//
//			// Se a shapelet candidata tem score 0 então já termina o processo
//			// de busca
//			if (rankedCandidates.get(i).getQuality().hasZeroQuality())
//				return rankedCandidates.subList(0, i);
//
//			Subtrajectory candidate = rankedCandidates.get(i);
//
//			// Removing self similar
//			if (searchIfSelfSimilarity(candidate, rankedCandidates.subList(0, i), selfSimilarityProp)) {
//				rankedCandidates.remove(i);
//				i--;
//			}
//
//		}
//
//		return rankedCandidates;
//	}
//
//	public boolean searchIfSelfSimilarity(Subtrajectory candidate, List<Subtrajectory> list,
//			double selfSimilarityProp) {
//
//		for (Subtrajectory s : list) {
//			if (areSelfSimilar(candidate, s, selfSimilarityProp))
//				return true;
//		}
//
//		return false;
//	}
//	
//	public boolean areSelfSimilar(Subtrajectory candidate, Subtrajectory subtrajectory,
//			double selfSimilarityProp) {
//		
//		//return false;
//		
//		// If their tids are different return false
//		
//		if (candidate.getTrajectory().getTid() != subtrajectory.getTrajectory().getTid())
//			return false;
//
//		else if (candidate.getStart() < subtrajectory.getStart()) {
//
//			if (candidate.getEnd() < subtrajectory.getStart())
//				return false;
//
//			if (selfSimilarityProp == 0)
//				return true;
//
//			double intersection = (candidate.getEnd() - subtrajectory.getStart())
//					/ (double) Math.min(candidate.getSize(), subtrajectory.getSize());
//
//			return intersection >= selfSimilarityProp;
//
//		} else {
//
//			if (subtrajectory.getEnd() < candidate.getStart())
//				return false;
//
//			if (selfSimilarityProp == 0)
//				return true;
//
//			double intersection = (subtrajectory.getEnd() - candidate.getStart())
//					/ (double) Math.min(candidate.getSize(), subtrajectory.getSize());
//
//			return intersection >= selfSimilarityProp;
//
//		}
//
//	}
	

	/*** * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 * HERE FOLLOWS THE PRUNNING PROCEDURES: ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * >>
	 *** * * * * * * * * * * * * * * * * * * **/
	
	/**
	 * Last Prunning Method
	 * 
	 * @param candidates
	 * @return
	 */
//	public List<Subtrajectory> lastPrunningFilter(List<Subtrajectory> movelets) {
//		List<Subtrajectory> noveltyShapelets = new ArrayList<>();
//		Set<Integer> allCovered = new HashSet<Integer>();
//		
//		for (int i = 0; i < movelets.size(); i++) {
//			double[][] distances = movelets.get(i).getDistances();
//			double[] splitpoint = movelets.get(i).getSplitpoints();
//			Set<Integer> currentCovered = findIndexesLowerSplitPoint(distances, splitpoint);
//			
//			if ( ! SetUtils.difference(currentCovered, allCovered).isEmpty()){
//				noveltyShapelets.add(movelets.get(i));
//				allCovered.addAll(currentCovered);
//			}
//		}
//		
//		return noveltyShapelets;
//	}
//	
//	public Set<Integer> findIndexesLowerSplitPoint(double[][] distances, double[] splitpoints ){
//		Set<Integer> indexes = new HashSet();
//		
//		RealMatrix rm = new Array2DRowRealMatrix(distances);
//		
//		for (int i = 0; i < distances[0].length; i++) {
//			if (isCovered(rm.getColumn(i), splitpoints) )			
//				indexes.add(i);
//			}
//		
//		return indexes;		
//	}
//
//	/* Para o caso de empate por conta de movelets discretas
//	 */
//	public boolean isCovered(double[] point, double[] limits){
//		
//		int dimensions = limits.length;
//		
//		for (int i = 0; i < dimensions; i++) {
//			if (limits[i] > 0){
//				if (point[i] >= limits[i])
//					return false;
//			} else
//				if (point[i] > limits[i])
//					return false;
//		}
//		
//		return true;
//	}

}
