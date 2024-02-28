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

import java.util.Random;

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
		//int n = 1000; // 1. 0.081501958| 2. 1.395447458
		//int n = 1500; // 1. 0.083571084| 2. 7.923277292000001
		int n = 2000; // 1. 0.152417417| 2. 26.792661083000002
		
		Random r = new Random();

		double[][] A = new double[n][n];
		double[][] B = new double[n][n];
		double[][] C = new double[n][n];

		long start = System.nanoTime();
		for (int i=0; i < n; i++)
			for (int j=0; j < n; j++) {
				A[i][j] = r.nextDouble();
				B[i][j] = r.nextDouble();
				C[i][j] = 0;
			}
		long end = System.nanoTime();
		System.out.println("1. Elapsed Time in seconds " + ((end-start) * 1e-9));

		start = System.nanoTime();
		for (int i=0; i < n; i++)
			for (int j=0; j < n; j++)
				for (int k=0; k < n; k++)
		            C[i][j] += A[i][k] * B[k][j];

		end = System.nanoTime();
		System.out.println("2. Elapsed Time in seconds " + ((end-start) * 1e-9));
		
	}

}