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
package br.com.tarlis.wyzard.run;

import java.io.IOException;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class WizardRun {
	
	public static void main(String[] args) throws IOException {
		
		// PARAMS:
		String descFile = (args.length > 0? args[0] : "data/descriptor.json");
		String inputFile = (args.length > 1? args[1] : "data/foursquare.csv");
		
		// 2 - RUN
		Wyzard wiz = new Wyzard();
		wiz.wyzard(descFile);
		
		// TODO Auto-generated method stub
		System.out.println(inputFile);
	}

}
