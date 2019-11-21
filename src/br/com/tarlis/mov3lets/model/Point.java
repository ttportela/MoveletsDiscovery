/**
 * Wizard - Multiple Aspect Trajectory (MASTER) Classification. 
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
package br.com.tarlis.mov3lets.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.tarlis.mov3lets.model.aspect.Aspect;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Point {
	
	private int id;
	private MAT trajectory;
	private Map<String, Aspect<?>> aspects = new HashMap<String, Aspect<?>>();
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the trajectory
	 */
	public MAT getTrajectory() {
		return trajectory;
	}
	
	/**
	 * @param trajectory the trajectory to set
	 */
	public void setTrajectory(MAT trajectory) {
		this.trajectory = trajectory;
	}
	
	/**
	 * @return the aspects
	 */
	public Map<String, Aspect<?>> getAspects() {
		return aspects;
	}
	
	/**
	 * @param aspects the aspects to set
	 */
	public void setAspects(Map<String, Aspect<?>> aspects) {
		this.aspects = aspects;
	}

}
