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

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public abstract class Quality {
	
	public abstract void setData(Map<String,Double> data);
	
	public abstract Map<String,Double> getData();
	
	public abstract boolean hasZeroQuality();
	
	public abstract <Q extends Quality> int compareTo(Q other);	

}
