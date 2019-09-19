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
import java.util.concurrent.Callable;

import br.com.tarlis.mov3lets.model.mat.MAT;
import br.com.tarlis.mov3lets.model.mat.Subtrajectory;
import br.com.tarlis.mov3lets.view.Descriptor;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public abstract class DiscoveryAdapter<MO> implements Callable<Integer> {

	protected Descriptor descriptor;
	
	protected MAT<MO> trajectory;
	protected List<MAT<MO>> data;
	
	/**
	 * @param train
	 */
	public DiscoveryAdapter(MAT<MO> trajectory, List<MAT<MO>> train, Descriptor descriptor) {
		this.trajectory = trajectory;
		this.data = train;
	}
	
	protected abstract List<Subtrajectory> discover(); 

	@Override
	public Integer call() throws Exception {
		
		discover();
		
		return 0;
	}
	
	/**
	 * @return the descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}

}
