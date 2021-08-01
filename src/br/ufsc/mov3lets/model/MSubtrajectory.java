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
package br.ufsc.mov3lets.model;

/**
 * The Class Subtrajectory.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 */
public class MSubtrajectory extends Subtrajectory {
		
	/** The distances: [trajectory][attribute][position/point] */
	private double[][][] distancesForAllT;

	/**
	 * Instantiates a new subtrajectory.
	 *
	 * @param start the start
	 * @param end the end
	 * @param t the t
	 */
	public MSubtrajectory(int start, int end, MAT<?> t) {
		super(start, end, t);
	}
	
	/**
	 * Instantiates a new subtrajectory.
	 *
	 * @param start the start
	 * @param end the end
	 * @param t the t
	 * @param pointFeatures the point features
	 * @param numberOfTrajectories the number of trajectories
	 */
	public MSubtrajectory(int start, int end, MAT<?> t, int[] pointFeatures, int numberOfTrajectories) {
		super(start, end, t, pointFeatures, numberOfTrajectories);
		this.distancesForAllT = new double[numberOfTrajectories][pointFeatures.length][];
	}
	
	/**
	 * Instantiates a new subtrajectory.
	 *
	 * @param start the start
	 * @param end the end
	 * @param t the t
	 * @param numberOfTrajectories the number of trajectories
	 * @param pointFeatures the point features
	 * @param k the k
	 */
	public MSubtrajectory(int start, int end, MAT<?> t, int numberOfTrajectories, int[] pointFeatures, int k) {
		super(start, end, t, numberOfTrajectories, pointFeatures, k);
		this.distancesForAllT = new double[numberOfTrajectories][pointFeatures.length][];
	}
	
	/**
	 * Gets the distances for all T.
	 *
	 * @return the distances
	 */
	public double[][][] getDistancesForAllT() {
		return distancesForAllT;
	}
	
	/**
	 * Sets the distances for all T.
	 *
	 * @param distances the new distances
	 */
	public void setDistancesForAllT(double[][][] distancesForAllT) {
		this.distancesForAllT = distancesForAllT;
	}
}
