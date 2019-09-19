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
package br.com.tarlis.mov3lets.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.com.tarlis.mov3lets.model.mat.MAT;
import br.com.tarlis.mov3lets.model.mat.Point;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Matrix4D extends HashMap<Pair, ArrayList<Double>> {
	
	private double DEFAULT = Double.POSITIVE_INFINITY;
	
	/**
	 * 
	 */
	public Matrix4D() {}
	
	/**
	 * 
	 */
	public Matrix4D(double defaultValue) {
		this.DEFAULT = defaultValue;
	}

	/**
	 * @param a
	 * @param b
	 * @param text
	 * @param distance
	 */
	public void add(Point a, Point b, double distance) {
		Pair pair = new Pair(a,b);
		ArrayList<Double> cube = null;
		
		if (containsKey(pair)) {
			cube = get(pair);
		} else {
			cube = new ArrayList<Double>();
			put(pair, cube);
		}
		
		cube.add(distance);
	}

	/**
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean contains(Point a, Point b) {
		return containsKey(new Pair(a, b));
	}

	/**
	 * 
	 * @param index Aspect index.
	 * @param p
	 * @param T
	 * @return
	 */
	public double[] distancesForAspect(int index, Point p, MAT<?> T) {
		double[] distancesForAspect = new double[T.getPoints().size()];
		
		for (int j = 0; j < T.getPoints().size(); j++) {
			Point q = T.getPoints().get(j);
			distancesForAspect[j] = get(new Pair(p, q)).get(index);
		}
				
		return distancesForAspect;
	}

	/**
	 * @param p
	 * @param q
	 * @return 
	 */
	public ArrayList<Double> get(Point p, Point q) {
		return get(new Pair(p, q));
	}
	
	@Override
	public Object clone() {
		Matrix4D clone = new Matrix4D();
		for(Entry<Pair, ArrayList<Double>> entry : this.entrySet()) {
	        //iterate through the graph
	        ArrayList<Double> sourceList = entry.getValue();
	        ArrayList<Double> clonedList = new ArrayList<Double>();
	        clonedList.addAll(sourceList);
	        //put value into new graph
	        clone.put(entry.getKey(), clonedList);
	    }
		return clone;
	}
	
//	public ArrayList<ArrayList<Double>> get(int i, int j) {
//		return super.get(i).get(j);
//	}
//	
//	public ArrayList<Double> get(int i, int j, int k) {
//		return super.get(i).get(j).get(k);
//	}
//	
//	public double get(int i, int j, int k, int l) {
//		try {
//			return super.get(i).get(j).get(k).get(l);
//		} catch (IndexOutOfBoundsException e) {
//			return DEFAULT;
//		}
//	}
//	
//	public double set(int i, int j, int k, int l, double value) {
//		if (super.get(i) == null)
//			super.set(i, new ArrayList<ArrayList<ArrayList<Double>>>());
//		if (super.get(i).get(j) == null)
//			super.get(i).set(j, new ArrayList<ArrayList<Double>>());
//		if (super.get(i).get(j).get(k) == null)
//			super.get(i).get(j).set(k, new ArrayList<Double>());
//		
//		return super.get(i).get(j).get(k).set(l, value);
//	}

}
