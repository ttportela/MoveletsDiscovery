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
package br.ufsc.mov3lets.method.discovery.deprecated;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.discovery.BaseMoveletsDiscovery;
import br.ufsc.mov3lets.method.discovery.structures.ClassDiscovery;
import br.ufsc.mov3lets.method.qualitymeasure.ProportionQualityMeasure;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class SuperMoveletsDiscovery.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 * @param <MO> the generic type
 */
public class SuperClassMoveletsDiscovery<MO> extends BaseMoveletsDiscovery<MO> implements ClassDiscovery {
	
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
	public SuperClassMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, QualityMeasure qualityMeasure, 
			Descriptor descriptor) {
		super(trajsFromClass.get(0), trajsFromClass, data, train, test, qualityMeasure, descriptor);
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

		progressBar.trace("By Class - SUPERMovelets Discovery for Class: " + trajsFromClass.get(0).getMovingObject() 
				+ ". Trajectory: " + trajectory.getTid());
		
		this.proportionMeasure = new ProportionQualityMeasure<MO>(this.trajsFromClass); //, TAU);
		
		for (MAT<MO> trajectory : trajsFromClass) {
			// This guarantees the reproducibility
			Random random = new Random(trajectory.getTid());
			/** STEP 2.1: Starts at discovering movelets */
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
			
//			progressBar.trace("Class: " + trajectory.getMovingObject() 
//					+ ". Trajectory: " + trajectory.getTid() 
//					+ ". Used GAMMA: " + GAMMA);
			
			/** STEP 2.4: CANDIDATES */			
			movelets.addAll(candidates);
			
			setStats("");

//			System.gc();
		}
		
		movelets = selectBestCandidates(trajectory, maxSize, new Random(trajectory.getTid()), movelets);
		
		/** STEP 2.2: Runs the pruning process */
		if(getDescriptor().getFlag("last_prunning"))
			movelets = lastPrunningFilter(movelets);

		/** STEP 2.2: ---------------------------- */
//		if (outputers != null)
//			for (OutputterAdapter<MO,?> output : outputers) {
//				output.setDelayCount(1);			
//			}
		
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
		
//		List<Subtrajectory> bestCandidates = selectBestCandidates(trajectory, maxSize, random, candidatesByProp);	
	
		base =  null;
		lastSize = null;
		
		progressBar.plus("Class: " + trajectory.getMovingObject() 
						+ ". Trajectory: " + trajectory.getTid() 
						+ ". Trajectory Size: " + trajectory.getPoints().size() 
						+ ". Number of Candidates: " + candidatesByProp.size() 
						+ ". Max Size: " + maxSize);
		
		return candidatesByProp;
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

		calculateProportion(candidatesByProp, random);
		double rel_tau = relativeFrequency(candidatesByProp);
		int bs = bucketSize(candidatesByProp.size());
		bestCandidates = filterByProportion(candidatesByProp, rel_tau, bs);
//		bestCandidates = filterByProportion(candidatesByProp, random);
		
		if (getDescriptor().getFlag("feature_limit"))
			bestCandidates = selectMaxFeatures(bestCandidates);
		
		bestCandidates = filterByQuality(bestCandidates, random, trajectory);
		
		progressBar.trace("Class: " + trajectory.getMovingObject() 
						+ ". TAU: " + rel_tau 
						+ ". Bucket Size: " + bs 
						+ ". Total of Movelets: " + bestCandidates.size() 
						+ ". Used Features: " + this.maxNumberOfFeatures);

		return bestCandidates;
	}
	
}
