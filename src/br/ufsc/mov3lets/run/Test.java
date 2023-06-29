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
import java.util.List;

import org.apache.commons.math3.util.Combinations;

import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class Test.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 */
public class Test<T extends MAT<?>> {
	
	public Subtrajectory a, b;
	
	public void setAB(Subtrajectory a, Subtrajectory b) {
		this.a = a;
		this.b = b;
	}

	
	/**
	 * The main method.
	 *
	 * @param arg the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] arg) throws Exception {
		
		long classCount = 13;
		for (int recall = 1; recall <= 10; recall++) {
			long meTarget = Math.max(Math.round((classCount - 1) * recall / 10.0), 1);
			System.out.println(meTarget);
		}
	}

	private static void extracted(int n, double trainProp, double stratifyProp) {
		int size      = (int) (n * stratifyProp);
        int trainSize = (int) Math.round(size * trainProp);
        
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) list.add(i % 2);
        
        List<Integer> train = list.subList(0,trainSize);
        List<Integer> test = list.subList(trainSize, size);
        
        System.out.println("N: " + n + " \t " +
        		"trainProp: " + trainProp + " \t " +
        		"stratifyProp: " + stratifyProp + " \t " +
        		"size: " + size + " \t " +
        		"trainSize: " + trainSize + " \t " +
        		"train.size(): " + train.size() + " \t " +
        		"test.size(): " + test.size());
	}

}