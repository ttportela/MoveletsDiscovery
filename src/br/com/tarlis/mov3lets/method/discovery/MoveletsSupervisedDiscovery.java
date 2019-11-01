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

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.mat.MAT;
import br.com.tarlis.mov3lets.model.mat.Subtrajectory;
import br.com.tarlis.mov3lets.model.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class MoveletsSupervisedDiscovery<MO> extends MoveletsDiscovery<MO> {
	
	/**
	 * @param train
	 */
	public MoveletsSupervisedDiscovery(MAT<MO> trajectory, List<MAT<MO>> train, List<Subtrajectory> candidates, QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajectory, train, candidates, qualityMeasure, descriptor);
	}

	/**
	 * First find candidates in its Class, then compares with every other trajectory
	 */
	public void discover() {

		// This guarantees the reproducibility
		Random random = new Random(this.trajectory.getTid());
		
		int n = this.data.size();
		int maxSize = getDescriptor().getParamAsInt("max_size");
		maxSize = (maxSize == -1) ? n : maxSize;
		int minSize = getDescriptor().getParamAsInt("min_size");

		Mov3letsUtils.trace("\tClass: " + trajectory.getMovingObject() + ". Discovering movelets."); // Might be saved in HD
//		Mov3letsUtils.getInstance().startTimer("\tClass >> " + trajectory.getClass());
		
		List<MAT<?>> trajectories = data.stream()                				 // convert list to stream
                .filter(e -> e.getMovingObject().equals(					 // Only of this trajectory class
                		     this.trajectory.getMovingObject()))       	 		
                .collect(Collectors.toList());
		
		List<Subtrajectory> candidates = moveletsDiscovery(trajectory, trajectories, minSize, maxSize, random);
//		Mov3letsUtils.getInstance().stopTimer("\tClass >> " + trajectory.getClass());
		
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
//				System.out.println("TODO? ASSES QUALITY, IF REQUIRED MD-102");
			}
		}
		
		/** STEP 3: SELECTING BEST CANDIDATES */	
		getCandidates().addAll(rankCandidates(candidates));
		
//		int numberOfCandidates = (maxSize * (maxSize-1) / 2);
		
	}
	
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

}
