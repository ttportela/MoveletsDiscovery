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

import java.nio.CharBuffer;

import org.apache.commons.lang3.StringUtils;

import br.ufsc.mov3lets.utils.ProgressBar;
import br.ufsc.mov3lets.utils.SimpleOutput;

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
		
		double[] ps = new double[] {1.0/6.0, 2.0/6.0, 3.0/6.0, 0.0};
		
		double entropy = 0.0;
		for (double p : ps) {
			entropy += -(p * Math.log(p) / Math.log(2));
			System.out.println(-(p * Math.log(p) / Math.log(2)));
		}

		System.out.println(entropy);
				
	}

}