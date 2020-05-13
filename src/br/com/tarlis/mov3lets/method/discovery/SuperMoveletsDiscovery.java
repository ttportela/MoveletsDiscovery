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
	
	protected final double TAU 	= 0.5;
	protected double GAMMA 		= 0.1;

	ProportionQualityMeasure<MO> proportionMeasure;
	
	/**
	 * @param train
	 */
	public SuperMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, List<Subtrajectory> candidates, QualityMeasure qualityMeasure, 
			Descriptor descriptor) {
		super(trajsFromClass, data, train, test, candidates, qualityMeasure, descriptor);
		this.proportionMeasure = new ProportionQualityMeasure<MO>(this.trajsFromClass, TAU);
	}
	
	public void discover() {

		int maxSize = getDescriptor().getParamAsInt("max_size");
		int minSize = getDescriptor().getParamAsInt("min_size");

		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();

		progressBar.trace("Super Movelets Discovery for Class: " + trajsFromClass.get(0).getMovingObject());
		
		for (MAT<MO> trajectory : trajsFromClass) {
			// This guarantees the reproducibility
			Random random = new Random(trajectory.getTid());
			/** STEP 2.1: Starts at discovering movelets */
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
			
//			progressBar.trace("Class: " + trajectory.getMovingObject() 
//					+ ". Trajectory: " + trajectory.getTid() 
//					+ ". Used GAMMA: " + GAMMA);
			
			/** STEP 2.3: Runs the pruning process */
			if(getDescriptor().getFlag("last_prunning"))
				candidates = lastPrunningFilter(candidates); // TODO is this here?
			
			movelets.addAll(candidates);
				
			/** STEP 2.4.1: Output Movelets (partial) */
			super.output("train", this.train, candidates, true);
			
			// Compute distances and best alignments for the test trajectories:
			/* If a test trajectory set was provided, it does the same.
			 * and return otherwise */
			/** STEP 2.4.2: Output Movelets (partial) */
			if (!this.test.isEmpty()) {
				for (Subtrajectory candidate : candidates) {
					// It initializes the set of distances of all movelets to null
					candidate.setDistances(null);
					// In this step the set of distances is filled by this method
					computeDistances(candidate, this.test);
				}
				super.output("test", this.test, candidates, true);
			}
			
			
			System.gc();
		}		
		
//		progressBar.trace("Class: " + trajsFromClass.get(0).getMovingObject() 
//				   + ". Total of Movelets: " + movelets.size());

		/** STEP 2.5, to write all outputs: */
		super.output("train", this.train, movelets, false);
		
		if (!this.test.isEmpty())
			super.output("test", this.test, movelets, false);
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
	
//		base =  null;
		lastSize = null;
		
		return selectBestCandidates(trajectory, maxSize, random, candidatesByProp);
	}

	public List<Subtrajectory> selectBestCandidates(MAT<MO> trajectory, int maxSize, Random random,
			List<Subtrajectory> candidatesByProp) {
		List<Subtrajectory> bestCandidates;

		GAMMA = getDescriptor().getParamAsDouble("gamma");
		bestCandidates = filterByProportion(candidatesByProp, GAMMA, random);
		bestCandidates = filterByQuality(bestCandidates, random);
		if (bestCandidates.isEmpty()) { 
			/* STEP 2.1.5: SELECT ONLY HALF OF THE CANDIDATES (IF Nothing found)
			 * * * * * * * * * * * * * * * * * * * * * * * * */
			calculateProportion(candidatesByProp, 1.0, random); 
			bestCandidates = candidatesByProp.subList(0, (int) Math.ceil((double) candidatesByProp.size() * TAU));
			GAMMA = 0.0;
			
			/** STEP 2.2: SELECTING BEST CANDIDATES */	
			bestCandidates = filterByQuality(bestCandidates, random);
		}
		
		progressBar.plus("Class: " + trajectory.getMovingObject() 
						+ ". Trajectory: " + trajectory.getTid() 
						+ ". Trajectory Size: " + trajectory.getPoints().size() 
						+ ". Number of Candidates: " + candidatesByProp.size() 
						+ ". Total of Movelets: " + bestCandidates.size() 
						+ ". Max Size: " + maxSize
						+ ". Used Features: " + this.maxNumberOfFeatures 
						+ ". Used GAMMA: " + GAMMA);

		return bestCandidates;
	}

	public List<Subtrajectory> filterByProportion(List<Subtrajectory> candidatesByProp, double gamma, Random random) {
		calculateProportion(candidatesByProp, gamma, random);

		/* STEP 2.1.2: SELECT ONLY CANDIDATES WITH PROPORTION > 50%
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		List<Subtrajectory> orderedCandidates = new ArrayList<>();
		for(Subtrajectory candidate : candidatesByProp)
			if(candidate.getQuality().getData().get("proportion") >= TAU)
				orderedCandidates.add(candidate);
			else 
				break;
		
		if (orderedCandidates.isEmpty()) return orderedCandidates;
				
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
		
		bestCandidates = bestCandidates.subList(0, (int) Math.ceil((double) bestCandidates.size() * GAMMA));
		
		return bestCandidates;
	}

	public void calculateProportion(List<Subtrajectory> candidatesByProp, double gamma, Random random) {
		candidatesByProp.forEach(x -> proportionMeasure.assesClassQuality(x, gamma, random));
		
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

	public List<Subtrajectory> filterByQuality(List<Subtrajectory> bestCandidates, Random random) {
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
			assesQuality(candidate);
//			assesQuality(candidate, random); //TODO change?
		}

		/** STEP 2.2: SELECTING BEST CANDIDATES */	
		return filterMovelets(bestCandidates);
	}
	
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
	
//	public double calculateProportion(Subtrajectory candidate) {
//
//		/*
//		 * STEP 1: VERIFY WHICH ARE THE TRAJECTORIES THAT CONTAIN THAT CANDIDATE FOR EACH DIMENSION
//		 */
//		
//		List<List<Integer>> trajectories_with_candidate = new ArrayList<>();
//		candidate.setCoveredInClass((List) new ArrayList<MAT<MO>>());
//		
//		for(double[] distances : candidate.getDistances()) {
//			
//			List<Integer> one_dimension_coverage = new ArrayList<>();
//			Integer i=0;
//			
//			for(double distance:distances) {
//				
//				if(distance==0.0)
//					one_dimension_coverage.add(i);
//				
//				i++;
//			}
//			
//			trajectories_with_candidate.add(one_dimension_coverage);
//		}
//
//			
//		/*
//		 * STEP 2: CALCULATE THE PROPORTION
//		 */
//		
//		double proportions = 0.0;
//		
//		for (int j = 0; j < trajectories_with_candidate.size(); j++) {
//			List<Integer> trajectories_per_dimension = trajectories_with_candidate.get(j);
//						
//			int a = trajectories_per_dimension.size();
//			int b = this.trajsFromClass.size();
//			proportions += (double) a / b;
//			
//
//			if (((double) a / b) > 0.5)
//				candidate.getCoveredInClass().add(this.trajsFromClass.get(j));
//			
//		}	
//			
//		double proportion = proportions/trajectories_with_candidate.size();
//		
//		/*
//		 * STEP 3: IF THE CANDIDATE COVERS ONLY LESS THAN HALF OF THE TRAJECTORIES, THEN ABORT IT.
//		 */
//		
////		if(proportion<0.5) {
////			return -1.0;
////		}
//		
//		return proportion;
//		
//	}

}
