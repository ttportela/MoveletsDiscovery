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
package br.ufsc.mov3lets.run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.util.Combinations;

/**
 * The Class Test.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 */
public class Test {

	/**
	 * The main method.
	 *
	 * @param arg the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] arg) throws Exception {
		
		List<Integer> ls = new ArrayList<Integer>();
		ls.add(1);
		ls.add(2);
		ls.add(3);

		System.out.println(ls.toString());
		Iterator<Integer> i = ls.iterator();
		while (i.hasNext()) {
		   Integer s = i.next(); // must be called before you can call i.remove()
		   // Do something
		   i.remove();
		   System.out.println(ls.toString());
		}
		
				
	}
	
	public static int[][] addCombinations(int minNumberOfFeatures, int maxNumberOfFeatures) {
		int numberOfFeatures = 4;
		int[][] combinations;
		
		int currentFeatures = minNumberOfFeatures;
		ArrayList<int[]> combaux = new ArrayList<int[]>();
		// Start in minimum size until max:
		for (;currentFeatures <= maxNumberOfFeatures; currentFeatures++) {
			for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) {					
				
				combaux.add(comb);
				System.out.println(Arrays.toString(comb));
				
			}		
		}
		
		combinations = combaux.stream().toArray(int[][]::new);
		
		return combinations;
	}

}