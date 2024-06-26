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
package br.ufsc.mov3lets.model.aspect;

/**
 * The Class Space2DAspect.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 */
public class Space3DAspect extends Aspect<String> {

	/** The x. */
	private double x;
	
	/** The y. */
	private double y;
	
	/** The z. */
	private double z;
	
	/**
	 * Instantiates a new space 2 D aspect.
	 *
	 * @param value the value
	 */
	public Space3DAspect(String value) {
		super(value);
		
		if (value != null) {
			String[] row = value.trim().split(" ");
			
			if (row.length < 3)
//				System.out.println(value);
				value = null;
			else {
				this.x = Double.parseDouble(row[0]);
				this.y = Double.parseDouble(row[1]);
				this.z = Double.parseDouble(row[2]);
			}
		}
	}

	/**
	 * Gets the x.
	 *
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * Sets the x.
	 *
	 * @param x the new x
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Gets the y.
	 *
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * Sets the y.
	 *
	 * @param y the new y
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Gets the z.
	 *
	 * @return the z
	 */
	public double getZ() {
		return z;
	}

	/**
	 * Sets the z.
	 *
	 * @param z the new z
	 */
	public void setZ(double z) {
		this.z = z;
	}

}
