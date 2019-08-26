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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import br.com.tarlis.wyzard.model.mat.MAT;
import br.com.tarlis.wyzard.model.mat.MovingObject;
import br.com.tarlis.wyzard.model.mat.aspect.Aspect;
import br.com.tarlis.wyzard.model.mat.aspect.Space2DAspect;
import br.com.tarlis.wyzard.view.AttributeDescriptor;
import br.com.tarlis.wyzard.view.Descriptor;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Wyzard<MO> {
	
	// CONFIG:
	private Descriptor descriptor = null;
	
	// TRAJS:
	private List<MAT> trajectories = null;

	// LOAD TRAJ

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public void wyzard(String descriptorFile) throws IOException {
				
		// STEP 1 - Input:
		this.descriptor = Descriptor.load(descriptorFile);

		List<MAT> trajectories = new ArrayList<MAT>();
		for (String file : descriptor.getInputFiles()) {
			trajectories.addAll(loadTrajectories(file));
		}
		
		// STEP 2 - Select Candidates:
		
		// STEP 3 - Qualify Candidates:
		
		// STEP 4 - Output:
	}
	
	public List<MAT> loadTrajectories(String inputFile) throws IOException {
		List<MAT> trajectories = new ArrayList<MAT>();
//		String row;
		MO mo = instantiateMovingObject("");
		
		// Load File:
//		BufferedReader csvReader = new BufferedReader(new FileReader(inputFile));
//		csvReader.readLine();
//		while ((row = csvReader.readLine()) != null) {
			
		CSVParser csvParser = CSVFormat.DEFAULT.parse(new InputStreamReader((new FileInputStream(inputFile))));
		csvParser.iterator().next();
		for (CSVRecord line : csvParser) {
//			line = row.split(",");
			
			// Create a MO:
			String label = line.get(this.descriptor.getLabelFeature().getOrder()-1);
			if (!mo.equals(label)) {
				mo = instantiateMovingObject(label);
			}
			
			MAT mat = instantiateMAT(line);
			mat.setMovingObject(mo);
		    trajectories.add(mat);
		}
//		csvReader.close();
		return trajectories;
	}
	
	public MO instantiateMovingObject(String label) {
		return (MO) new MovingObject<String>(label);
	}
	
	public MAT instantiateMAT(CSVRecord line) {
		MAT mat = new MAT<MovingObject<String>>();
		mat.setTid(Integer.parseInt(line.get(this.descriptor.getIdFeature().getOrder()-1)));
		mat.setAspects(new ArrayList<Aspect<?>>());
		
		for (AttributeDescriptor attr : this.descriptor.getAttributes()) {
			mat.getAspects().add(instantiateAspect(attr, line.get(attr.getOrder()-1)));
		}
		
		return mat;
	}
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public Aspect<?> instantiateAspect(AttributeDescriptor attr, String value) {
		switch (attr.getType()) {
			case "numeric":
				return new Aspect<Double>(Double.parseDouble(value));
			case "space2d":
				return new Space2DAspect(value);
			case "datetime":
				try {
					return new Aspect<Date>(formatter.parse(value));
				} catch (ParseException e) {
					trace("Atribute datetime '"+value+"' in wrong format, must be yyyy/MM/dd HH:mm:ss");
					return new Aspect<Date>(new Date());
				}
			case "foursquarevenue":
			case "gowallacheckin":
			case "nominal":
			default:
				return new Aspect<String>(value);
		}
	}
	
	public static void trace(String s) {
		System.out.println(s);
	}

}
