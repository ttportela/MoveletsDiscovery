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
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.discovery.structures.TrajectoryDiscovery;
import br.ufsc.mov3lets.method.filter.FeaturesCandidatesFilter;
import br.ufsc.mov3lets.method.filter.FrequentCandidatesFilter;
import br.ufsc.mov3lets.method.filter.FrequentFeaturesCandidatesFilter;
import br.ufsc.mov3lets.method.qualitymeasure.FrequentQualityMeasure;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;
import br.ufsc.mov3lets.model.aspect.Aspect;

/**
 * The Class SuperMoveletsDiscovery.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 * @param <MO> the generic type
 */
public class FrequentMoveletsDiscovery<MO> extends MoveletsDiscovery<MO> implements TrajectoryDiscovery {
	
	/** The tau. */
	protected double TAU 		= 0.9;
//	protected double GAMMA 		= 1.0;

	protected double BU 		= 0.1;

	/** The proportion measure. */
	protected FrequentQualityMeasure<MO> frequencyMeasure;
	
	/**
	 * Instantiates a new super movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor the descriptor
	 */
	public FrequentMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, QualityMeasure qualityMeasure, 
			Descriptor descriptor) {
		super(trajectory, trajsFromClass, data, train, test, qualityMeasure, descriptor);
		
		TAU 	= getDescriptor().hasParam("tau")? getDescriptor().getParamAsDouble("tau") : 0.0;
		BU 		= getDescriptor().hasParam("bucket_slice")? getDescriptor().getParamAsDouble("bucket_slice") : 1.0;
	}
	
	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.discovery.MemMoveletsDiscovery#discover().
	 * 
	 * @return
	 */
	public List<Subtrajectory> discover() {

		int maxSize = getDescriptor().getParamAsInt("max_size");
		int minSize = getDescriptor().getParamAsInt("min_size");

		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();

//		progressBar.trace("SUPERMovelets Discovery for Class: " + trajsFromClass.get(0).getMovingObject() 
//				+ ". Trajectory: " + trajectory.getTid());
		
		this.frequencyMeasure = new FrequentQualityMeasure<MO>(this.trajsFromClass); //, TAU);
		
//		for (MAT<MO> trajectory : trajsFromClass) {
			// This guarantees the reproducibility
			Random random = new Random(trajectory.getTid());
			/** STEP 2.1: Starts at discovering movelets */
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
			
//			progressBar.trace("Class: " + trajectory.getMovingObject() 
//					+ ". Trajectory: " + trajectory.getTid() 
//					+ ". Used GAMMA: " + GAMMA);
			
			/** STEP 2.4: SELECTING BEST CANDIDATES */			
//			candidates = filterMovelets(candidates);		
//			movelets.addAll(filterMovelets(candidates));
			movelets.addAll(candidates);

//			System.gc();
//		}
		
		/** STEP 2.2: Runs the pruning process */
		if(getDescriptor().getFlag("last_pruning"))
			movelets = lastPrunningFilter(movelets);

		/** STEP 2.2: ---------------------------- */
		outputMovelets(movelets);
		/** -------------------------------------- */
		
		return movelets;
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
		List<Subtrajectory> candidatesByProp = new ArrayList<Subtrajectory>();

		int n = trajectory.getPoints().size();

		minSize = minSize(minSize, n);
		maxSize = maxSize(maxSize, minSize, n);

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
			
			if (size >= minSize){

				// Create candidates and compute min distances		
				List<Subtrajectory> candidatesOfSize = findCandidates(trajectory, trajectories, size, newSize);
			
				total_size = total_size + candidatesOfSize.size();
				
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

	/**
	 * [THE GREAT GAP].
	 *
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param size the size
	 * @param mdist the mdist
	 * @return the list
	 */
	public List<Subtrajectory> findCandidates(MAT<MO> trajectory, List<MAT<MO>> trajectories, int size, double[][][][] mdist) {
		
		// Trajectory P size => n
		int n = trajectory.getPoints().size();
		int[][] combinations = makeCombinations(exploreDimensions, numberOfFeatures, maxNumberOfFeatures);
		
		maxDistances = new double[getDescriptor().getAttributes().size()];
		
		// List of Candidates to extract from P:
		List<Subtrajectory> candidates = new ArrayList<>();
		
		// From point 0 to (n - <candidate max. size>) 
		for (int start = 0; start <= (n - size); start++) {
//			Point p = trajectory.getPoints().get(start);
			
			// Extract possible candidates from P to max. candidate size:
			List<Subtrajectory> list = buildSubtrajectory(start, start + size - 1, trajectory, trajectories.size(), combinations);
							
			double[][][] distancesForAllT = mdist[start];
			
			// For each trajectory in the database
			for (int i = 0; i < trajectories.size(); i++) {
				MAT<MO> T = trajectories.get(i);	
				
				double[][] distancesForT = distancesForAllT[i];
				double[][] ranksForT = new double[distancesForT.length][];
				
				int limit = T.getPoints().size() - size + 1;
				
				if (limit > 0)
					for (int k = 0; k < numberOfFeatures; k++) {				
						ranksForT[k] = rankingAlgorithm.rank(Arrays.stream(distancesForT[k],0,limit).toArray());
					} // for (int k = 0; k < numberOfFeatures; k++)
				
				for (Subtrajectory subtrajectory : list) {		
					int bestPosition = (limit > 0) ? bestAlignmentByRanking(ranksForT, subtrajectory.getPointFeatures()) : -1;
					for (int j = 0; j < subtrajectory.getPointFeatures().length; j++) {	
						double distance = (bestPosition >= 0) ? 
								distancesForT[subtrajectory.getPointFeatures()[j]][bestPosition] : MAX_VALUE;
						subtrajectory.getDistances()[j][i] = (distance != MAX_VALUE) ? 
								Math.sqrt( distance / size ) : MAX_VALUE;	
								
						if (maxDistances[j] < subtrajectory.getDistances()[j][i] && subtrajectory.getDistances()[j][i] != MAX_VALUE)
							maxDistances[j] = subtrajectory.getDistances()[j][i];
					}
				}
				
			} // for (int currentFeatures = 1; currentFeatures <= numberOfFeatures; currentFeatures++)
			
			candidates.addAll(list);

		} // for (int start = 0; start <= (n - size); start++)
		
		return candidates;
		
	}

	/**
	 * Select best candidates.
	 *
	 * @param trajectory the trajectory
	 * @param maxSize the max size
	 * @param random the random
	 * @param candidatesByProp the candidates by prop
	 * @return the list
	 */
	public List<Subtrajectory> selectBestCandidates(MAT<MO> trajectory, int maxSize, Random random,
			List<Subtrajectory> candidatesByProp) {
		List<Subtrajectory> bestCandidates;
		
		addStats("Class", trajectory.getMovingObject()); 
		addStats("Trajectory", trajectory.getTid());
		addStats("Trajectory Size", trajectory.getPoints().size()); 
		addStats("Number of Candidates", candidatesByProp.size());

		calculateProportion(candidatesByProp, random);
		
		// Relative TAU based on the higher proportion:
		double rel_tau = relativeFrequency(candidatesByProp);	addStats("TAU", rel_tau);
		int bs = bucketSize(candidatesByProp.size());  			addStats("Bucket Size", bs);
		bestCandidates = filterByProportion(candidatesByProp, rel_tau, bs);
//		bestCandidates = filterTopCandidates(candidatesByProp);
				
		// If using feature limit, remove candidates out of the dimension limit
		if (getDescriptor().getFlag("feature_limit"))
			bestCandidates = selectMaxFeatures(bestCandidates);
		addStats("Scored Candidates", bestCandidates.size());
		
		bestCandidates = filterByQuality(bestCandidates, random, trajectory);

//		/* STEP 2.1.5: Recover Approach (IF Nothing found)
//		 * * * * * * * * * * * * * * * * * * * * * * * * */
//		if (bestCandidates.isEmpty()) { 
//			bestCandidates = recoverCandidates(trajectory, random, candidatesByProp);
//		}

		addStats("Total of Movelets", bestCandidates.size());
		addStats("Max Size", maxSize);
		addStats("Used Features", this.maxNumberOfFeatures);
//		progressBar.plus("Class: " + trajectory.getMovingObject() 
//						+ ". Trajectory: " + trajectory.getTid() 
//						+ ". Trajectory Size: " + trajectory.getPoints().size() 
//						+ ". Number of Candidates: " + candidatesByProp.size() 
//						+ ". Total of Movelets: " + bestCandidates.size() 
//						+ ". Max Size: " + maxSize
//						+ ". Used Features: " + this.maxNumberOfFeatures);

		progressBar.plus(getStats());
		
		return bestCandidates;
	}

	/** The bucket. */
	protected List<Subtrajectory> bucket = new ArrayList<Subtrajectory>();
	
	/**
	 * Mehod bucketSize. 
	 * 
	 */
	protected int bucketSize(int candidatesByProp) {
		int n = candidatesByProp;
		if (BU > 0.0) {
			n = (int) Math.ceil((double) (candidatesByProp+bucket.size()) * BU); // By 10%
		} else if (BU == -2.0) { // LOG^2
			n = (int) Math.ceil(Math.pow( Math.log(candidatesByProp+bucket.size()) , 2));
		}
		return (n > candidatesByProp)? candidatesByProp : n;
	}
	
	/**
	 * Filter by proportion.
	 *
	 * @param candidatesByProp the candidates by prop
	 * @param random the random
	 * @return the list
	 */
	public List<Subtrajectory> filterByProportion(List<Subtrajectory> candidatesByProp, double rel_tau, int n) {
		return new FrequentCandidatesFilter(rel_tau, n, bucket).filter(candidatesByProp);
	}

	protected double relativeFrequency(List<Subtrajectory> candidatesByProp) {
		return getDescriptor().getFlag("relative_tau")? ((candidatesByProp.size() > 0? 
				 candidatesByProp.get(0).getQuality().getData().get("quality") : 0.0) * TAU) : TAU;
	}

	public List<Subtrajectory> filterTopCandidates(List<Subtrajectory> candidatesByProp) {		
		int n = (int) Math.ceil((double) (candidatesByProp.size()) * BU); // By 10%
		return candidatesByProp.subList(0, n);
	}

	/**
	 * TODO Correct these
	 * Mehod selectMaxFeatures. 
	 * Options:
	 * 		-2: Log Limit (not implemented here)
	 * 		-3: Limit by mode of dimension usage from best candidates.
	 * 		-4: Limit by most frequent dimensions from best candidates
	 * 
	 * @param candidatesByProp
	 * @return
	 */
	public List<Subtrajectory> selectMaxFeatures(List<Subtrajectory> candidatesByProp) {
		FeaturesCandidatesFilter filter;
		if (getDescriptor().getParamAsInt("max_number_of_features") == -3) {
			filter = new FeaturesCandidatesFilter(numberOfFeatures, maxNumberOfFeatures);
		} else if (getDescriptor().getParamAsInt("max_number_of_features") == -4) {
			filter = new FrequentFeaturesCandidatesFilter(numberOfFeatures, maxNumberOfFeatures, TAU);
		} else
			return candidatesByProp;
		
		candidatesByProp = filter.filter(candidatesByProp);
		this.maxNumberOfFeatures = filter.getMaxNumberOfFeatures();
		return candidatesByProp;
	}
	
	/**
	 * Recover candidates.
	 *
	 * @param trajectory the trajectory
	 * @param random the random
	 * @param candidatesByProp the candidates by prop
	 * @return the list
	 */
	public List<Subtrajectory> recoverCandidates(MAT<MO> trajectory, Random random,
			List<Subtrajectory> candidatesByProp) {
		
		int n = bucketSize(candidatesByProp.size());
		
		orderCandidates(bucket);

		List<Subtrajectory> bestCandidates = new ArrayList<Subtrajectory>();
		
		long recovered = 0;
		for (int i = n; i < bucket.size(); i += n) {
			bestCandidates = bucket.subList(i-n, (i > bucket.size()? bucket.size() : i));
			recovered += bestCandidates.size();
			bestCandidates = filterByQuality(bestCandidates, random, trajectory);
			
			if (i > bucket.size() || !bestCandidates.isEmpty()) break;
			else n *= 2; // expand the window size
		}
		addStats("Recovered Candidates", recovered);
		
		return bestCandidates;
	}

	/**
	 * Calculate proportion.
	 *
	 * @param candidatesByProp the candidates by prop
	 * @param random the random
	 */
	public void calculateProportion(List<Subtrajectory> candidatesByProp, Random random) {
		candidatesByProp.forEach(x -> frequencyMeasure.assesClassQuality(x, maxDistances, random));
		
		orderCandidates(candidatesByProp);
	}

	/**
	 * Order candidates.
	 *
	 * @param candidatesByProp the candidates by prop
	 */
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

	/**
	 * Filter by quality.
	 *
	 * @param bestCandidates the best candidates
	 * @param random the random
	 * @param trajectory the trajectory
	 * @return the list
	 */
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
		return this.bestFilter.filter(bestCandidates);
	}

//	@Override
//	public double calculateDistance(Aspect<?> a, Aspect<?> b, AttributeDescriptor attr) {
//		return attr.getDistanceComparator().enhance(
//				attr.getDistanceComparator().calculateDistance(a, b, attr)
//		);
//	}

	@Override
	public double calculateDistance(Aspect<?> a, Aspect<?> b, AttributeDescriptor attr) {
		return attr.getDistanceComparator().enhance(
				attr.getDistanceComparator().normalizeDistance(
				attr.getDistanceComparator().calculateDistance(a, b, attr),
				attr.getComparator().getMaxValue()
		));
	}
	
}
