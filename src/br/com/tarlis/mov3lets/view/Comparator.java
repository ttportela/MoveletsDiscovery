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
package br.com.tarlis.mov3lets.view;

import br.com.tarlis.mov3lets.model.distancemeasure.DistanceInterface;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Comparator {

	private String distance;
    private Double maxValue = -1.0;
    private DistanceInterface distanceComparator = null;
    
	/**
	 * @return the distance
	 */
	public String getDistance() {
		return distance;
	}
	/**
	 * @param distance the distance to set
	 */
	public void setDistance(String distance) {
		this.distance = distance;
	}
	/**
	 * @return the maxValue
	 */
	public Double getMaxValue() {
		return maxValue;
	}
	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}
	
	/**
	 * @return the distanceComparator
	 */
	public <DM extends DistanceInterface> DM getDistanceComparator() {
		return (DM) distanceComparator;
	}
	
	/**
	 * @param distanceComparator the distanceComparator to set
	 */
	public <DM extends DistanceInterface> void setDistanceComparator(DM distanceComparator) {
		this.distanceComparator = distanceComparator;
	}
	
	@Override
	public String toString() {
		return getDistance() +"/"+ getMaxValue();
	}

}
