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
import java.util.List;
import java.util.Random;

import br.com.tarlis.mov3lets.method.Mov3lets;
import br.com.tarlis.mov3lets.model.mat.MAT;
import br.com.tarlis.mov3lets.model.mat.SubMAT;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class MoveletsDiscovery extends DiscoveryAdapter {

	protected MAT trajectory;
	protected List<MAT> data;
	
	/**
	 * @param train
	 */
	public MoveletsDiscovery(MAT trajectory, List<MAT> train) {
		this.trajectory = trajectory;
		this.data = train;
	}

	/**
	 * Looks for candidates in the trajectory, then compares with every other trajectory
	 */
	protected void discover() {
		List<SubMAT<?>> candidates = new ArrayList<>();
		
		int n = this.data.size();
		int maxSize = (Mov3lets.getDescriptor().getParamAsInt("max_size") == -1) ? 
				   n : Mov3lets.getDescriptor().getParamAsInt("max_size");
		int numberOfCandidates = (maxSize * (maxSize-1) / 2);
		int numberOfFeatures = Mov3lets.getDescriptor().getParamAsInt("max_number_of_features");

		Random random = new Random(this.trajectory.getTid());
		for (MAT trajectory : this.data) {
			
		}
		
	}

}
