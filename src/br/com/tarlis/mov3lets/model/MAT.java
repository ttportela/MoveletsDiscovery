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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 
 * Moving object can be anything, including an instance of MovingObject.
 * 
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class MAT<MO> {

	private int tid = -1;
	private MO movingObject;
	private List<?> aspects = null;
	private List<Point> points = new ArrayList<Point>();
	
	// Attributtes resulting of method discovery.
	private Map<String,Double> attributes = new ConcurrentHashMap<>();;
	
	/**
	 * @return the tid
	 */
	public int getTid() {
		return tid;
	}
	
	/**
	 * @param tid the tid to set
	 */
	public void setTid(int tid) {
		this.tid = tid;
	}
	
	/**
	 * @return the movingObject
	 */
	public MO getMovingObject() {
		return movingObject;
	}
	/**
	 * @param movingObject the movingObject to set
	 */
	public void setMovingObject(MO movingObject) {
		this.movingObject = movingObject;
	}
	
	/**
	 * @return the aspects
	 */
	public List<?> getAspects() {
		return aspects;
	}
	
	/**
	 * @param aspects the aspects to set
	 */
	public void setAspects(List<?> aspects) {
		this.aspects = aspects;
	}
	
	/**
	 * @return the points
	 */
	public List<Point> getPoints() {
		return points;
	}
	
	/**
	 * @param points the points to set
	 */
	public void setPoints(List<Point> points) {
		this.points = points;
	}
	
	/**
	 * @return the attributes
	 */
	public Map<String, Double> getAttributes() {
		return attributes;
	}
	
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Map<String, Double> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		String string = new String();

		string += "Label: " + getMovingObject().toString() + "\n";
		string += "Points: \n";

		for (int i = 0; i < getPoints().size(); i++)
			string += getPoints().get(i).toString() + "\n";

		return string;
	}

}
