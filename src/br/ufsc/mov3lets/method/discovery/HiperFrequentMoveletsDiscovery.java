/**
 * 
 */
package br.ufsc.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.filter.EqualCandidatesFilter;
import br.ufsc.mov3lets.method.filter.OverlappingPointsFilter;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class HiperPivotsMoveletsDiscovery.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class HiperFrequentMoveletsDiscovery<MO> extends HiperPivotsMoveletsDiscovery<MO> {

	/**
	 * Instantiates a new hiper pivots movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor the descriptor
	 */
	public HiperFrequentMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajsFromClass, data, train, test, qualityMeasure, descriptor);
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
		
		List<Subtrajectory> candidatesOfSize = findCandidates(trajectory, trajectories, size, base);
		
//		GAMMA = getDescriptor().getParamAsDouble("gamma");
		calculateProportion(candidatesOfSize, random);
//		orderCandidates(candidatesOfSize);
		// Relative TAU based on the higher proportion:
		double rel_tau = relativeFrequency(candidatesByProp);	addStats("TAU", rel_tau);
		int bs = bucketSize(candidatesByProp.size());  			addStats("Bucket Size", bs);
		candidatesOfSize = filterByProportion(candidatesOfSize, rel_tau, bs);
//		candidatesOfSize = filterByProportion(candidatesOfSize, random);

		if( minSize <= 1 ) {
			candidatesByProp.addAll(candidatesOfSize);
		}				
		
		double[][][][] newSize = clone4DArray(base);		

		total_size = total_size + candidatesOfSize.size();
		
		// Tratar o resto dos tamanhos 
		for (size = 2; size <= maxSize; size++) {
			
			// Precompute de distance matrix
   			newSize = newSize(trajectory, trajectories, base, newSize, size);
			
			candidatesOfSize = growPivots(candidatesOfSize, trajectory, trajectories, base, newSize, size);
//			GAMMA = getDescriptor().getParamAsDouble("gamma");

			calculateProportion(candidatesOfSize, random);
//			orderCandidates(candidatesOfSize);
//			candidatesOfSize = filterOvelappingPoints(candidatesOfSize);
			candidatesOfSize = new OverlappingPointsFilter(bucket).filter(candidatesOfSize);
			candidatesOfSize = filterByProportion(candidatesOfSize, relativeFrequency(candidatesOfSize), bucketSize(candidatesOfSize.size()));
	
			total_size = total_size + candidatesOfSize.size();
			
			if (size >= minSize) {
				//for (Subtrajectory candidate : candidatesOfSize) assesQuality(candidate);				
//				candidatesOfSize.forEach(x -> assesQuality(x, random));
				candidatesByProp.addAll(candidatesOfSize);
			}
		
//			lastSize = newSize;
						
		} // for (int size = 2; size <= max; size++)	
		
		/** STEP 2.2: SELECTING BEST CANDIDATES */	
		orderCandidates(candidatesByProp);
//		candidatesByProp = filterEqualCandidates(candidatesByProp);
		candidatesByProp = new EqualCandidatesFilter(getDescriptor()).filter(candidatesByProp);
		
		if (getDescriptor().getFlag("feature_limit"))
			candidatesByProp = selectMaxFeatures(candidatesByProp);
		
//		bestCandidates = filterByQuality(bestCandidates, random, trajectory);	
		
		/* STEP 2.1.5: Recover Approach (IF Nothing found)
		 * * * * * * * * * * * * * * * * * * * * * * * * */
//		if (bestCandidates.isEmpty()) { 
//			bestCandidates = recoverCandidates(trajectory, random, candidatesByProp);
//		}
		
//		queue.removeAll(getCoveredInClass(bestCandidates));	
	
		progressBar.plus("Class: " + trajectory.getMovingObject() 
						+ ". Trajectory: " + trajectory.getTid() 
						+ ". Trajectory Size: " + trajectory.getPoints().size() 
						+ ". Number of Candidates: " + candidatesByProp.size()
						+ ". Max Size: " + maxSize
						+ ". Used Features: " + this.maxNumberOfFeatures);
//						+ ". Memory Use: " + Mov3letsUtils.getUsedMemory());
	
		base =  null;
		newSize = null;
	
		return candidatesByProp;
	}

}
