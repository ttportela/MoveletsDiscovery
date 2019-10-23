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
import java.util.stream.Collectors;

import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.mat.MAT;
import br.com.tarlis.mov3lets.model.mat.Subtrajectory;
import br.com.tarlis.mov3lets.model.qualitymeasure.QualityMeasure;

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
	protected void discover() {
		List<Subtrajectory> candidates = null;
		
		List<MAT<?>> trajectories = data.stream()                				 // convert list to stream
                .filter(e -> e.getMovingObject().equals(					 // Only of this trajectory class
                		     this.trajectory.getMovingObject()))       	 		
                .collect(Collectors.toList());
		for (MAT<?> trajectory : trajectories) {	
			// TODO: my way, prunning by classes (Tarlis)
		}
		
//		return candidates;
		
	}

}
