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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.tarlis.wyzard.view.Descriptor;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Wyzard {
	
	private static Wyzard instance;
	
	// CONFIG:
	private Descriptor descriptor;
	
	/**
	 * 
	 */
	public static Wyzard getInstance() {
		if (Wyzard.instance == null) {
			Wyzard.instance = new Wyzard();
		}
		
		return Wyzard.instance;
	}
	
	/**
	 * 
	 */
	private Wyzard() {}

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
		
		// PARAMS:
		String descFile = (args.length > 0? args[0] : "data/descriptor.json");
		String inputFile = (args.length > 1? args[1] : "data/descriptor.json");
		
		// 1 - LOAD - Descriptor:
		Reader reader = new InputStreamReader(
				new FileInputStream(descFile), "UTF-8");
        Gson gson = new GsonBuilder().create();
        Wyzard.getInstance().descriptor = gson.fromJson(reader, Descriptor.class);
        Wyzard.getInstance().descriptor.configure();

		System.out.println(Wyzard.getInstance().descriptor);
		
		// TODO Auto-generated method stub
		System.out.println(inputFile);
	}

}
