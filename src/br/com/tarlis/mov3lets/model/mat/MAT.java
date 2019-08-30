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
package br.com.tarlis.mov3lets.model.mat;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class MAT<MO> {

	private int tid;
	private MO movingObject;
	private List<?> aspects = new ArrayList<>();
	
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

}
