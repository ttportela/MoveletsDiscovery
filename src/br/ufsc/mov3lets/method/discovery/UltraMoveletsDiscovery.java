/**
 * 
 */
package br.ufsc.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.discovery.structures.TrajectoryDiscovery;
import br.ufsc.mov3lets.method.qualitymeasure.ProportionQualityMeasure;
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
public class UltraMoveletsDiscovery<MO> extends HiperMoveletsDiscovery<MO> implements TrajectoryDiscovery {

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
	public UltraMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajsFromClass, data, train, test, qualityMeasure, descriptor);
		this.trajectory = trajectory;
		
		// Ultra default is Log^2:
		BU 		= getDescriptor().hasParam("bucket_slice")? getDescriptor().getParamAsDouble("bucket_slice") : -2;
	}

	
	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.discovery.SuperMoveletsDiscovery#discover().
	 * 
	 * @return
	 */
	public List<Subtrajectory> discover() {

		int maxSize = getDescriptor().getParamAsInt("max_size");
		int minSize = getDescriptor().getParamAsInt("min_size");

		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();

//		progressBar.trace("HiperT-Pivots Movelets Discovery for Class: " + trajsFromClass.get(0).getMovingObject()); 
		
		this.proportionMeasure = new ProportionQualityMeasure<MO>(this.trajsFromClass); //, TAU);
		
		// This guarantees the reproducibility
		Random random = new Random(trajectory.getTid());
		/** STEP 2.1: Starts at discovering movelets */
		List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
		
		/** STEP 2.4: SELECTING BEST CANDIDATES */		
		movelets.addAll(filterMovelets(candidates));
		
		setStats("");
		
		/** STEP 2.2: Runs the pruning process */
		if(getDescriptor().getFlag("last_prunning"))
			movelets = lastPrunningFilter(movelets);

		/** STEP 2.2: ---------------------------- */
		outputMovelets(movelets);
		/** -------------------------------------- */	
		System.gc();
		
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

}
