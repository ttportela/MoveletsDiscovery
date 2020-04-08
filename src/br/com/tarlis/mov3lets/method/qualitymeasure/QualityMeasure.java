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

import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.util.Pair;

import br.com.tarlis.mov3lets.model.Subtrajectory;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public abstract class QualityMeasure {
	
	public double MAX_VALUE = Double.MAX_VALUE;
	
	public abstract void assesQuality(Subtrajectory candidate, Random random);
	
	public Pair<double[],double[]> fillSplitPointsLimits(Map<String, double[]> splitpointsData, String medium){
		int n = splitpointsData.get("mean").length;
		double[] splitpointsLI = new double[n];
		double[] splitpointsLS = new double[n];
		
		switch (medium){
		
			case "interquartil" :
				splitpointsLI = splitpointsData.get("p25");
				splitpointsLS = splitpointsData.get("p75");				
				break;
			case "sd" :
				for (int i = 0; i < n; i++) {
					splitpointsLI[i] = splitpointsData.get("mean")[i] - splitpointsData.get("sd")[i];
					splitpointsLS[i] = splitpointsData.get("mean")[i] + splitpointsData.get("sd")[i];
				}
				break;
			case "minmax" :
				splitpointsLI = splitpointsData.get("min");
				splitpointsLS = splitpointsData.get("max");				
				break;
			case "mean" :
				splitpointsLI = splitpointsData.get("mean");
				splitpointsLS = splitpointsData.get("mean");	
				break;	
				
			default :
				splitpointsLI = splitpointsData.get("mean");
				splitpointsLS = splitpointsData.get("mean");					
		
		}		
		
		return new Pair<double[], double[]>(splitpointsLI,splitpointsLS);
	}
	
	public boolean isCovered(double[] point, double[] limits){
		
		int dimensions = limits.length;
		
		for (int i = 0; i < dimensions; i++) {
			if (limits[i] > 0){
				if (point[i] >= limits[i])
					return false;
			} else
				if (point[i] > limits[i])
					return false;
		}
		
		return true;
	}

}
