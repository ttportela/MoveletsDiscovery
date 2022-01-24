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

import br.ufsc.mov3lets.method.loader.MATInternLoader;
import br.ufsc.mov3lets.model.MAT;

/**
 * The Class Test.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 */
public class Test<T extends MAT<?>> {

	/**
	 * The main method.
	 *
	 * @param arg the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] arg) throws Exception {
		
////		String file = "/Users/tarlisportela/OneDrive/3 - Projetos/workdir/data/multivariate_ts/InsectWingbeat/InsectWingbeat_TEST";
//		String file = "/Users/tarlisportela/OneDrive/3 - Projetos/workdir/data/test/TEST";
//		
////		TSReader reader = new TSReader(new FileReader(new File(file)));
//		MATInternLoader<MAT<String>> reader = new MATInternLoader<MAT<String>>();
//		System.out.println(reader.loadTrajectories(file, null).get(1));
//		System.out.println(reader.getAttributes().toString());
		

		System.out.println(
		Double.POSITIVE_INFINITY == (Double.POSITIVE_INFINITY * Double.POSITIVE_INFINITY)
		);

		System.out.println(
		(Double.MAX_VALUE * Double.MAX_VALUE) >= Double.MAX_VALUE
		);
	}

}