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
package br.com.tarlis.mov3lets.model.distancemeasure;

import br.com.tarlis.mov3lets.view.AttributeDescriptor;
import br.com.tarlis.mov3lets.model.mat.aspect.Aspect;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class NominalWeekdayDistance extends DistanceInterface<Aspect<String>> {

	@Override
	public double distance(Aspect<String> asp0, Aspect<String> asp1, AttributeDescriptor attr) {
		return weekdayDistance(asp0.getValue(), asp1.getValue());
	}
	
	/** SAME as Original Movelets */
	public double weekdayDistance(String value1, String value2){
		if ( isWeekendDay(value1) && isWeekendDay(value2))
			return 0;
		else if ( !isWeekendDay(value1) && !isWeekendDay(value2))
			return 0;
		else 
			return 1;
	}	
	
	/** SAME as Original Movelets */
	public boolean isWeekendDay(String value1){
		return value1.equalsIgnoreCase("Sunday") || value1.equalsIgnoreCase("Saturday");
	}

}
