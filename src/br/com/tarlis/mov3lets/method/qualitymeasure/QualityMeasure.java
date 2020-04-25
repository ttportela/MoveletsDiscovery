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
package br.com.tarlis.mov3lets.method.qualitymeasure;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.util.Pair;

import br.com.tarlis.mov3lets.method.distancemeasure.DistanceMeasure;
import br.com.tarlis.mov3lets.model.Subtrajectory;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public abstract class QualityMeasure {
	
	public double MAX_VALUE = DistanceMeasure.DEFAULT_MAX_VALUE;
	
	public abstract void assesQuality(Subtrajectory candidate, Random random);

	protected double[] getMaxDistances(double[][] distances) {
		
		double[] maxDistances = new double[distances.length];
		for (int i = 0; i < maxDistances.length; i++) {
			maxDistances[i] =
					Arrays.stream(distances[i]).filter(e -> e != MAX_VALUE).max().getAsDouble();
		}
				
		return maxDistances;
	}

}
