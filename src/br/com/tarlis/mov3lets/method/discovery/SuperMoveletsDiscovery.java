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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

import br.com.tarlis.mov3lets.method.qualitymeasure.ProportionQualityMeasure;
import br.com.tarlis.mov3lets.method.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Point;
import br.com.tarlis.mov3lets.model.Subtrajectory;
import br.com.tarlis.mov3lets.model.aspect.Aspect;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class SuperMoveletsDiscovery<MO> extends MemMoveletsDiscovery<MO> {
	
	protected double TAU 		= 0.5;
//	protected double GAMMA 		= 1.0;

	ProportionQualityMeasure<MO> proportionMeasure;
	
	/**
	 * @param train
	 */
	public SuperMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, QualityMeasure qualityMeasure, 
			Descriptor descriptor) {
		super(trajsFromClass, data, train, test, qualityMeasure, descriptor);
		
		TAU 	= getDescriptor().getParamAsDouble("tau");
//		GAMMA 	= getDescriptor().getParamAsDouble("gamma");
	}
	
	public List<Subtrajectory> discover() {

		int maxSize = getDescriptor().getParamAsInt("max_size");
		int minSize = getDescriptor().getParamAsInt("min_size");

		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();

		progressBar.trace("Super Movelets Discovery for Class: " + trajsFromClass.get(0).getMovingObject());
		
		this.proportionMeasure = new ProportionQualityMeasure<MO>(this.trajsFromClass, TAU);
		
		for (MAT<MO> trajectory : trajsFromClass) {
			// This guarantees the reproducibility
			Random random = new Random(trajectory.getTid());
			/** STEP 2.1: Starts at discovering movelets */
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
			
//			progressBar.trace("Class: " + trajectory.getMovingObject() 
//					+ ". Trajectory: " + trajectory.getTid() 
//					+ ". Used GAMMA: " + GAMMA);
			
			/** STEP 2.4: SELECTING BEST CANDIDATES */			
//			candidates = filterMovelets(candidates);		
			movelets.addAll(filterMovelets(candidates));

//			System.gc();
		}
		
		/** STEP 2.2: Runs the pruning process */
		if(getDescriptor().getFlag("last_prunning"))
			movelets = lastPrunningFilter(movelets);
		/** STEP 2.2: --------------------------------- */
		
		/** STEP 2.3.1: Output Movelets (partial) */
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
		/** --------------------------------- */
		
//		progressBar.trace("Class: " + trajsFromClass.get(0).getMovingObject() 
//				   + ". Total of Movelets: " + movelets.size());

		/** STEP 2.5, to write all outputs: */
		super.output("train", this.train, movelets, false);
		
		if (!this.test.isEmpty())
			super.output("test", this.test, movelets, false);
		
		return movelets;
	}
	
	/**
	 * @param trajectory2
	 * @param data2
	 * @param minSize
	 * @param maxSize
	 * @param random 
	 * @return
	 */
	public List<Subtrajectory> moveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajectories, int minSize, int maxSize, Random random) {
		List<Subtrajectory> candidatesByProp = new ArrayList<Subtrajectory>();

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
		
		base = computeBaseDistances(trajectory, trajectories);
		
		if( minSize <= 1 ) {
			candidatesByProp.addAll(findCandidates(trajectory, trajectories, size, base));
//			candidates.forEach(x -> assesQuality(x, random));
		}				
		
		double[][][][] lastSize = clone4DArray(base);		

		total_size = total_size + candidatesByProp.size();
		
		// Tratar o resto dos tamanhos 
		for (size = 2; size <= maxSize; size++) {
	
			// Precompute de distance matrix
   			double[][][][] newSize = newSize(trajectory, trajectories, base, lastSize, size);

			// Create candidates and compute min distances		
			List<Subtrajectory> candidatesOfSize = findCandidates(trajectory, trajectories, size, newSize);
		
			total_size = total_size + candidatesOfSize.size();
			
			if (size >= minSize){
				
				//for (Subtrajectory candidate : candidatesOfSize) assesQuality(candidate);				
//				candidatesOfSize.forEach(x -> assesQuality(x, random));
				candidatesByProp.addAll(candidatesOfSize);
			}
		
			lastSize = newSize;
						
		} // for (int size = 2; size <= max; size++)
		
		List<Subtrajectory> bestCandidates = selectBestCandidates(trajectory, maxSize, random, candidatesByProp);	
	
		base =  null;
		lastSize = null;
		
		return bestCandidates;
	}

	public List<Subtrajectory> selectBestCandidates(MAT<MO> trajectory, int maxSize, Random random,
			List<Subtrajectory> candidatesByProp) {
		List<Subtrajectory> bestCandidates;

//		GAMMA = getDescriptor().getParamAsDouble("gamma");
		bestCandidates = filterByProportion(candidatesByProp, random);
		bestCandidates = filterByQuality(bestCandidates, random, trajectory);

		/* STEP 2.1.5: Recover Approach (IF Nothing found)
		 * * * * * * * * * * * * * * * * * * * * * * * * */
		if (bestCandidates.isEmpty()) { 
			int n = (int) Math.ceil((double) (candidatesByProp.size()+bucket.size()) * 0.1); // By 10%
			
			for (int i = n; i < n*10; i += n) {
				bestCandidates = filterByQuality(bucket.subList(i-n, (i > bucket.size()? bucket.size() : i)), random, trajectory);
				
				if (i > bucket.size() || !bestCandidates.isEmpty()) break;
			}
		}
		
		progressBar.plus("Class: " + trajectory.getMovingObject() 
						+ ". Trajectory: " + trajectory.getTid() 
						+ ". Trajectory Size: " + trajectory.getPoints().size() 
						+ ". Number of Candidates: " + candidatesByProp.size() 
						+ ". Total of Movelets: " + bestCandidates.size() 
						+ ". Max Size: " + maxSize
						+ ". Used Features: " + this.maxNumberOfFeatures);

		return bestCandidates;
	}

	protected List<Subtrajectory> bucket = new ArrayList<Subtrajectory>();
	public List<Subtrajectory> filterByProportion(List<Subtrajectory> candidatesByProp, Random random) {
		calculateProportion(candidatesByProp, random);
		
		// Relative TAU based on the higher proportion:
		double rel_tau = (candidatesByProp.size() > 0? candidatesByProp.get(0).getQuality().getData().get("quality") : 0.0) * TAU;

		/* STEP 2.1.2: SELECT ONLY CANDIDATES WITH PROPORTION > 50%
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		List<Subtrajectory> orderedCandidates = new ArrayList<>();
		for(Subtrajectory candidate : candidatesByProp)
			if(candidate.getQuality().getData().get("quality") >= rel_tau) //TAU)
				orderedCandidates.add(candidate);
			else 
//				break;
				bucket.add(candidate);
		
		if (orderedCandidates.isEmpty()) 
			return orderedCandidates;
//			orderedCandidates = bucket;
				
		List<Subtrajectory> bestCandidates = filterEqualCandidates(orderedCandidates);
		
//		bestCandidates = bestCandidates.subList(0, (int) Math.ceil((double) bestCandidates.size() * GAMMA));
		
		return bestCandidates;
	}

	public List<Subtrajectory> filterEqualCandidates(List<Subtrajectory> orderedCandidates) {
		/* STEP 2.1.4: IDENTIFY EQUAL CANDIDATES
		 * * * * * * * * * * * * * * * * * * * * * * * * */
		List<Subtrajectory> bestCandidates = new ArrayList<>();
//		int[] attribute_usage = new int [numberOfFeatures]; // array of 5 ints
		
		for(Subtrajectory candidate : orderedCandidates) {
			
			if(bestCandidates.isEmpty())
				bestCandidates.add(candidate);
			else {
				boolean equal = false;
				for(Subtrajectory best_candidate : bestCandidates) {
					
					List<HashMap<Integer, Aspect<?>>> used_features_c1 = getDimensions(candidate);
					List<HashMap<Integer, Aspect<?>>> used_features_c2 = getDimensions(best_candidate);
					
					if(used_features_c1.size()==used_features_c2.size())
						if(areEqual(used_features_c1, used_features_c2)) {
							equal = true;
							break;
						}
					
				}
				if(!equal) {
					bestCandidates.add(candidate);
//					attribute_usage[candidate.getPointFeatures().length-1]++;
				}
			}
		}
		return bestCandidates;
	}

	public void calculateProportion(List<Subtrajectory> candidatesByProp, Random random) {
		candidatesByProp.forEach(x -> proportionMeasure.assesClassQuality(x, maxDistances, random));
		
		orderCandidates(candidatesByProp);
	}

	public void orderCandidates(List<Subtrajectory> candidatesByProp) {
		/* STEP 2.1.3: SORT THE CANDIDATES BY PROPORTION VALUE
		 * * * * * * * * * * * * * * * * * * * * * * * * * */
		candidatesByProp.sort(new Comparator<Subtrajectory>() {
			@Override
			public int compare(Subtrajectory o1, Subtrajectory o2) {
				
//				return (-1) * o1.getQuality().compareTo(o2.getQuality());
				return o1.getQuality().compareTo(o2.getQuality());				
				
			}
		});
	}

	public List<Subtrajectory> filterByQuality(List<Subtrajectory> bestCandidates, Random random, MAT<MO> trajectory) {
		/** STEP 2.3, for this trajectory movelets: 
		 * It transforms the training and test sets of trajectories using the movelets */
		for (Subtrajectory candidate : bestCandidates) {
			// It initializes the set of distances of all movelets to null
			candidate.setDistances(null);
			candidate.setQuality(null);
			// In this step the set of distances is filled by this method
			computeDistances(candidate, this.train); // computeDistances(movelet, trajectories);

			/* STEP 2.1.6: QUALIFY BEST HALF CANDIDATES 
			 * * * * * * * * * * * * * * * * * * * * * * * * */
//			assesQuality(candidate);
			assesQuality(candidate, random); //TODO change?
		}

		/** STEP 2.2: SELECTING BEST CANDIDATES */	
		return filterMovelets(bestCandidates);
	}
	
//	public List<Subtrajectory> filterByQuality(List<Subtrajectory> list, Random random, 
//			MAT<MO> trajectory) {
//		
//		// Sort by size:
//		list.sort(new Comparator<Subtrajectory>() {
//			@Override
//			public int compare(Subtrajectory o1, Subtrajectory o2) {
//				
//				return Integer.compare(o1.getSize(), o2.getSize());				
//				
//			}
//		});
//		
//		int n = trajectory.getPoints().size();
//		
//		double[][][][] mdist = computeBaseDistances(trajectory, this.train);
//		
//		for (int size = 1; size <= n; size++) {
//						
//			for (Subtrajectory subtrajectory : list) {	
//				
//				if (subtrajectory.getSize() != size) continue;
//				
//				double[][][] distancesForAllT = mdist[subtrajectory.getStart()];
//				
//				// For each trajectory in the database
//				for (int i = 0; i < this.train.size(); i++) {
//					MAT<MO> T = this.train.get(i);	
//					
//					double[][] distancesForT = distancesForAllT[i];
//					double[][] ranksForT = new double[distancesForT.length][];
//					
//					int limit = T.getPoints().size() - size + 1;
//					
//					if (limit > 0)
//						for (int k = 0; k < numberOfFeatures; k++) {				
//							ranksForT[k] = rankingAlgorithm.rank(Arrays.stream(distancesForT[k],0,limit).toArray());
//						} // for (int k = 0; k < numberOfFeatures; k++)
//						
//					int bestPosition = (limit > 0) ? bestAlignmentByRanking(ranksForT, subtrajectory.getPointFeatures()) : -1;
//					for (int j = 0; j < subtrajectory.getPointFeatures().length; j++) {	
//						double distance = (bestPosition >= 0) ? 
//								distancesForT[subtrajectory.getPointFeatures()[j]][bestPosition] : MAX_VALUE;
//						subtrajectory.getDistances()[j][i] = (distance != MAX_VALUE) ? 
//								Math.sqrt( distance / size ) : MAX_VALUE;					
//					}
//					
//				} // for (int currentFeatures = 1; currentFeatures <= numberOfFeatures; currentFeatures++)
//				
//				assesQuality(subtrajectory, random);
//					
//			} // for (int start = 0; start <= (n - size); start++)
//			
//			mdist = newSize(trajectory, this.train, base, mdist, size+1);
//		}
//		
//		return filterMovelets(list);
//		
//	}
	
	public List<HashMap<Integer, Aspect<?>>> getDimensions(Subtrajectory candidate) {
		
		List<Integer> features_in_movelet = new ArrayList<>();
		
		int[] list_features = candidate.getPointFeatures();
		
		for(int i=0; i <= getDescriptor().getAttributes().size(); i++) {
			
			if(ArrayUtils.contains(list_features, i))				
				features_in_movelet.add(i);
			
		}
		
		List<HashMap<Integer, Aspect<?>>> used_features = new ArrayList<>();
		
		for(int i=0; i < candidate.getPoints().size(); i++) {
			
			Point point = candidate.getPoints().get(i);
			
			HashMap<Integer, Aspect<?>> features_in_point = new HashMap<>();
			
			for(Integer feature : features_in_movelet) {
				features_in_point.put(feature, point.getAspects().get(feature));
			}
			
			used_features.add(features_in_point);
		}
		
		return used_features;
	}
	
	public boolean areEqual(List<HashMap<Integer, Aspect<?>>> first, List<HashMap<Integer, Aspect<?>>> second) {
		
		if (first.size() != second.size())
	        return false;
		
		if (first.get(0).size() != second.get(0).size())
	        return false;
	 
		for ( Integer key : first.get(0).keySet() ) {
			if(!second.get(0).containsKey(key)) {
		        return false;
		    }
		}
		
		boolean all_match = true;
		
		for(int i=0; i<first.size();i++) {
			
			HashMap<Integer, Aspect<?>> f = first.get(i);
			HashMap<Integer, Aspect<?>> s = second.get(i);
			
			if(f.entrySet().stream()
				      .allMatch(e -> e.getValue().equals(s.get(e.getKey()))))
				return false;
			
		}
	    return all_match;
	}
	
	protected double[] maxDistances;
	public double[][][][] computeBaseDistances(MAT<?> trajectory, List<MAT<MO>> trajectories){
		int n = trajectory.getPoints().size();
		int size = 1;
		
		maxDistances = new double[getDescriptor().getAttributes().size()];
		
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
						base[start][i][k][j] = attr.getDistanceComparator().calculateDistance(
								a.getAspects().get(k), 
								b.getAspects().get(k), 
								attr);
						
						if (maxDistances[k] < base[start][i][k][j] && base[start][i][k][j] != MAX_VALUE)
							maxDistances[k] = base[start][i][k][j];
					
//						base[start][i][k][j] = (distance != MAX_VALUE) ? (distance) : MAX_VALUE;	// No sense				
					
					} // for (int k = 0; k < distance.length; k++)
					
				} // for (int j = 0; j <= (train.size()-size); j++)
				
			} //for (int i = 0; i < train.size(); i++)
			
		} // for (int start = 0; start <= (n - size); start++)

		return base;
	}
	
	public double[][][][] newSize(MAT<?> trajectory, List<MAT<MO>> trajectories, double[][][][] base, double[][][][] lastSize, int size) {
		
		int n = trajectory.getPoints().size();	
		
		for (int start = 0; start <= (n - size); start++) {
						
			for (int i = 0; i < trajectories.size(); i++) {
				
				if (trajectories.get(i).getPoints().size() >= size) {						
							
					for (int j = 0; j <= (trajectories.get(i).getPoints().size()-size); j++) {
												
						for (int k = 0; k < lastSize[start][i].length; k++) {
							
							if (lastSize[start][i][k][j] != MAX_VALUE)
								lastSize[start][i][k][j] += base[start+size-1][i][k][j+size-1];
							

							if (maxDistances[k] < lastSize[start][i][k][j] && lastSize[start][i][k][j] != MAX_VALUE)
								maxDistances[k] = lastSize[start][i][k][j];
						
						} // for (int k = 0; k < distance.length; k++) {
											
					} // for (int j = 0; j <= (train.size()-size); j++)
					
				} // if (train.get(i).getData().size() >= size) 
				
			} // for (int i = 0; i < train.size(); i++)
			
		} // for (int start = 0; start <= (n - size); start++)
		
		return lastSize;
	}

}
