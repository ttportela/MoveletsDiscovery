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
package br.com.tarlis.mov3lets.method.descriptor;

import br.com.tarlis.mov3lets.model.distancemeasure.DistanceMeasure;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class AttributeDescriptor {
	
	private Integer order;
    private String type;
    private String text;
    private Comparator comparator;
    private DistanceMeasure<?> distanceComparator = null;
    
	/**
	 * @return the order
	 */
	public Integer getOrder() {
		return order;
	}
	/**
	 * @param order the order to set
	 */
	public void setOrder(Integer order) {
		this.order = order;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type != null? type.trim().toLowerCase() : type;
	}
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	/**
	 * @return the comparator
	 */
	public Comparator getComparator() {
		return comparator;
	}
	/**
	 * @param comparator the comparator to set
	 */
	public void setComparator(Comparator comparator) {
		this.comparator = comparator;
	}
	/**
	 * @return the distanceComparator
	 */
	public DistanceMeasure<?> getDistanceComparator() {
		return distanceComparator;
	}
	/**
	 * @param distanceComparator the distanceComparator to set
	 */
	public void setDistanceComparator(DistanceMeasure<?> distanceComparator) {
		this.distanceComparator = distanceComparator;
	}
	
	@Override
	public String toString() {
		return "Attr: " + getOrder() +" - "+ getText() +"/"+ getType() +" ("+getComparator()+")";
	}

}
