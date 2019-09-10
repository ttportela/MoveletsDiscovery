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
package br.com.tarlis.mov3lets.run;

import java.io.IOException;
import java.util.HashMap;

import br.com.tarlis.mov3lets.method.Mov3lets;
import br.com.tarlis.mov3lets.view.Descriptor;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Mov3letsRun {
	
	public static void main(String[] args) throws IOException {
		HashMap<String, Object> params = configure(args);
		
		// PARAMS:
		String descFile = (params.containsKey("descfile")? params.get("descfile").toString() : "data/descriptor.json");
		String inputFile = (args.length > 1? args[1] : "data/foursquare.csv");
		
		// 2 - RUN
		Mov3lets mov = new Mov3lets(descFile);
		mov.getDescriptor().addParams(params);
		System.out.println(showConfiguration(mov.getDescriptor()));
		
		mov.mov3lets();
//		System.out.println(inputFile);
	}
	
	public static HashMap<String, Object> configure(String[] args) {
		
		HashMap<String, Object> params = new HashMap<String, Object>();

		for (int i = 0; i < args.length; i = i + 2) {
			String key = args[i];
			String value = args[i + 1];
			switch (key) {
			case "-curpath":
				params.put("curpath", value);
				break;
			case "-respath":
				params.put("respath", value);
				break;
			case "-descfile":
				params.put("descfile", value);
				break;
			case "-outside_pivots":
				params.put("outside_pivots", value);
				break;
			case "-nt":
			case "-nthreads":
				params.put("nthreads", Integer.valueOf(value));
				break;
			case "-ms":
			case "-minSize":
			case "-min_size":
				params.put("min_size", Integer.valueOf(value));
				break;
			case "-Ms":
			case "-maxSize":
			case "-max_size":
				params.put("max_size", Integer.valueOf(value));
				break;
			case "-q":
			case "-strQualityMeasure":
				params.put("str_quality_measure", value);
				break;
			case "-cache":
				params.put("cache", Boolean.valueOf(value));		
				break;
			case "-ed":
			case "-exploreDimensions":
			case "-explore_dimensions":
				params.put("explore_dimensions", Boolean.valueOf(value));
				break;					
			case "-mnf":			
			case "-maxNumberOfFeatures":			
			case "-max_number_of_features":
				params.put("max_number_of_features", Integer.valueOf(value));
				break;
			case "-samples":
				params.put("samples", Integer.valueOf(value));			
				break;					
			case "-sampleSize":	
			case "-sample_size":
				params.put("sample_size", Double.valueOf(value));			
				break;					
			case "-medium":
				params.put("medium", value);		
				break;			
			case "-output":
				params.put("output", value);			
				break;		
			case "-pvt":
			case "-pivots":
				params.put("pivots", Boolean.valueOf(value));				
				break;	
			case "-mpt":
			case "-movelets_per_trajectory":
			case "-moveletsPerTrajectory":
			case "-movelets_per_traj":
				params.put("respath", Integer.valueOf(value));			
				break;
			case "-lowm":
			case "-lowm_memory":
				params.put("lowm_memory", Boolean.valueOf(value));		
				break;			
			case "-last_prunning":		
			case "-lp":
				params.put("last_prunning", Boolean.valueOf(value));		
				break;	
			case "-pp":
			case "-pivot_porcentage":
				params.put("pivot_porcentage", Integer.valueOf(value));
				break;
			case "-only_pivots":
			case "-op":
				params.put("only_pivots", Boolean.valueOf(value));
				break;
			default:
				System.err.println("Parâmetro " + key + " inválido.");
				System.exit(1);
			}
		}

		return params;
	}
	
	
	public static String showConfiguration(Descriptor descriptor) {

		String str = new String();
		
		if(descriptor.getFlag("pivots"))
			str += "Starting running Movelet_pivots extractor (10%) " + System.getProperty("line.separator");
		else if(descriptor.getParamAsInt("max_size")==-3)
			str += "Starting running MASTERMovelets_Log extractor " + System.getProperty("line.separator");
		else
			str += "Starting running MASTERMovelets extractor " + System.getProperty("line.separator");
		
		if (descriptor.hasParam("outside_pivots"))
			str += "Getting pivots from outside file:" + descriptor.getParam("outside_pivots") + System.getProperty("line.separator");
		
		str += "Configurations:" + System.getProperty("line.separator");

		str += "\tDatasets directory:	    " + descriptor.getParam("curpath") + System.getProperty("line.separator");

		str += "\tResults directory:    " + descriptor.getParam("respath") + System.getProperty("line.separator");
		
		str += "\tDescription file :    " + descriptor.getParam("descfile") + System.getProperty("line.separator");

		str += "\tAllowed Threads:      " + descriptor.getParam("nthreads") + System.getProperty("line.separator");

		str += "\tMin size:             " + descriptor.getParam("min_size") + System.getProperty("line.separator");

		str += "\tMax size:             " + descriptor.getParam("max_size") + System.getProperty("line.separator");

		str += "\tQuality Measure:      " + descriptor.getParam("str_quality_measure") + System.getProperty("line.separator");
		
		str += "\tExplore dimensions:   " + descriptor.getParam("explore_dimensions") + System.getProperty("line.separator");

		str += "\tSamples:      " + descriptor.getParam("samples") + System.getProperty("line.separator");
		
		str += "\tSample Size:   " + descriptor.getParam("sample_size") + System.getProperty("line.separator");
		
		str += "\tMedium:   " + descriptor.getParam("medium") + System.getProperty("line.separator");
		
		str += "\tMPT:   " + descriptor.getParam("movelets_per_trajectory") + System.getProperty("line.separator");
		
		str += "\tOutput:   " + descriptor.getParam("output") + System.getProperty("line.separator");
		
		if(descriptor.getFlag("last_prunning"))
			str += "WITH LAST PRUNNING" + System.getProperty("line.separator");
		else
			str += "WITHOUT LAST PRUNNING" + System.getProperty("line.separator");
		
		return str;

	}

}
