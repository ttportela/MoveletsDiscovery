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
package br.com.tarlis.mov3lets.method.structures;

import java.util.Objects;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Tuple<M, N, I> extends Pair<M, N> {
	
	private I index;
	
    /**
	 * @param x
	 * @param y
	 */
	public Tuple(M x, N y, I index) {
		super(x, y);
		this.index = index;
	}

	/**
	 * @return the p
	 */
	public I getIndex() {
		return index;
	}
	
	/**
	 * @param p the p to set
	 */
	public void setIndex(I p) {
		this.index = p;
	}

	/**
     * Compare the specified object with this entry for equality.
     * Independent of the pair order.
     *
     * @param o Object.
     * @return {@code true} if the given object is also a map entry and
     * the two entries represent the same mapping.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tuple)) {
            return false;
        } else {
            Tuple oP = (Tuple) o;
            return (super.equals(oP))
            		&& getIndex().equals(oP.getIndex());
        }
    }

    /**
     * Compute a hash code.
     *
     * @return the hash code value.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getIndex().hashCode());
    }

}
