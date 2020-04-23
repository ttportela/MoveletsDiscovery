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
package br.com.tarlis.mov3lets.method.distancemeasure;

import br.com.tarlis.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.model.aspect.Aspect;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public abstract class DistanceMeasure<A extends Aspect<?>> {
	
	public static double DEFAULT_MAX_VALUE = Double.MAX_VALUE;

	public double MAX_VALUE = DistanceMeasure.DEFAULT_MAX_VALUE;
	
	/**
	 * MEASURES OF DISTANCE (so far)
	 * ** Nominal: **
	 * - equals
	 * - equalsignorecase
	 * --- provided << NOT IMPLEMENTED >>
	 * - weekday
	 * 
	 * ** Foursquare:
	 * --- lca => Lowest common antecesor << NOT IMPLEMENTED >>
	 * 
	 * ** Gowallacheckin:
	 * --- w2v (WordToVec) << NOT IMPLEMENTED >>
	 * 
	 * ** Numeric: **
	 * - diffnotneg -> Difference if not Negative
	 * - difference
	 * - proportion
	 * 
	 * ** Space2d: **
	 * - euclidean
	 * - manhattan
	 * 
	 * ** Date:
	 * - difference
	 * 
	 * ** Time
	 * - difference
	 * 
	 * ** Localtime
	 * - difference
	 * 
	 * ** Localdate
	 * - difference
	 * - diffdaysofweek
	 * - equaldayofweek
	 * - isworkday
	 * - isweekend
	 * - isworkdayorweekend
	 */
	public abstract double distance(A asp0, A asp1, AttributeDescriptor attr);
	
	/** SAME as Original Movelets */
	public double normalizeDistance(double distance, double maxValue){
		/* If maxValue was not defined */
		if (maxValue == -1)
			return distance;
	
		if (distance >= maxValue)
			return MAX_VALUE;	
		
		return distance / maxValue;
	}
	
	public double enhance(double distance) {
		return (distance != MAX_VALUE) ? (distance * distance) : MAX_VALUE;
		// TRUE for: MAX_VALUE == (MAX_VALUE * MAX_VALUE)
		// TRUE for: Double.POSITIVE_INFINITY == (Double.POSITIVE_INFINITY * Double.POSITIVE_INFINITY)
	}
	
	public double calculateDistance(Aspect<?> asp0, Aspect<?> asp1, AttributeDescriptor attr) {
		return enhance(distance((A) asp0, (A) asp1, attr));
	}
	
}
