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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import br.com.tarlis.mov3lets.method.Mov3lets;
import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Mov3letsRun {
	
	public static void main(String[] args) throws IOException {
		// Starting Date:
		System.out.println(new Date());
		
		HashMap<String, Object> params = configure(args);
		
		// PARAMS:
		String descFile = (params.containsKey("curpath")? params.get("curpath").toString() : "./");
		descFile = descFile + (params.containsKey("descfile")? params.get("descfile").toString() : "descriptor.json");

		// Config to - Show trace messages OR Ignore all
		if (params.containsKey("verbose") && (boolean) params.get("verbose"))
			Mov3letsUtils.getInstance().configLogger();
		
//		String inputFile = (args.length > 1? args[1] : "data/foursquare.csv");
		
		// 2 - RUN
		Mov3lets<String> mov = new Mov3lets<String>(descFile);
		mov.getDescriptor().addParams(defaultParams());
		mov.getDescriptor().addParams(params);
		System.out.println(showConfiguration(mov.getDescriptor()));
				
		// Set Result Dir:
		mov.setResultDirPath(configRespath(descFile, mov.getDescriptor()));
		
		// RUN:
		Mov3letsUtils.getInstance().startTimer("[Runtime] ==> ");
		mov.mov3lets();
		Mov3letsUtils.getInstance().stopTimer("[Runtime] ==> ");
//		System.out.println(inputFile);
		
		// End Date:
		System.out.println(new Date());
	}
	
	public static HashMap<String, Object> defaultParams() {
		
		HashMap<String, Object> params = new HashMap<String, Object>();

//		params.put("curpath", 					 null);
//		params.put("respath",					 null);
//		params.put("descfile",					 null);
//		params.put("outside_pivots",			 null);
		params.put("nthreads",					 1);
		params.put("min_size",					 2);
		params.put("max_size",					 -1); // unlimited maxSize
		params.put("str_quality_measure",		 "LSP");
//		params.put("cache",						 true); // Deprecated: Cache Always.	
		params.put("explore_dimensions",		 false);
		params.put("max_number_of_features",	 -1);
		params.put("samples",					 1);			
		params.put("sample_size",				 1);			
		params.put("medium",					 "none"); // Other values minmax, sd, interquartil
		params.put("output",					 "numeric"); // Other values numeric discretized				
		params.put("pivots",					 false);						
		params.put("supervised",				 false);
		params.put("movelets_per_trajectory",	 -1);	// Filtering		
		params.put("lowm_memory",				 false);		
		params.put("last_prunning",				 false);		
		params.put("pivot_porcentage",			 10);
		params.put("only_pivots",				 false);
		params.put("verbose",				 	 true);
		
		return params;
	}
	
	public static HashMap<String, Object> configure(String[] args) {
		
		HashMap<String, Object> params = defaultParams();

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
				int N_THREADS = Integer.valueOf(value);
				N_THREADS = N_THREADS == 0? 1 : N_THREADS;
				params.put("nthreads", N_THREADS);
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
//			case "-cache": // => Deprecated
//				params.put("cache", Boolean.valueOf(value));		
//				break;
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
			case "-sup":
			case "-supervised":
				params.put("supervised", Boolean.valueOf(value));				
				break;	
			case "-mpt":
			case "-movelets_per_trajectory":
			case "-moveletsPerTrajectory":
			case "-movelets_per_traj":
				params.put("movelets_per_trajectory", Integer.valueOf(value));			
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
			case "-index":
				params.put("index", Boolean.valueOf(value));
				break;
			case "-interning":
				params.put("interning", Boolean.valueOf(value));
				break;
			case "-v":
			case "-verbose":
				params.put("verbose", Boolean.valueOf(value));
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
			str += "Starting MASTERMovelets +Pivots extractor (10%) " + System.getProperty("line.separator");
		else if(descriptor.getParamAsInt("max_size")==-3)
			str += "Starting MASTERMovelets +Log extractor " + System.getProperty("line.separator");
		else
			str += "Starting MASTERMovelets extractor " + System.getProperty("line.separator");
		
		if (descriptor.hasParam("outside_pivots"))
			str += "Getting pivots from outside file:" + descriptor.getParam("outside_pivots") + System.getProperty("line.separator");
		
		str += "Configurations:" + System.getProperty("line.separator");

		str += "\tDatasets directory:\t" + descriptor.getParam("curpath") + System.getProperty("line.separator");

		str += "\tResults directory:\t" + descriptor.getParam("respath") + System.getProperty("line.separator");
		
		str += "\tDescription file :\t" + descriptor.getParam("descfile") + System.getProperty("line.separator");

		str += "\tUsing Index:\t\t" + descriptor.getFlag("index") + System.getProperty("line.separator");

		str += "\tString Interning:\t" + descriptor.getFlag("interning") + System.getProperty("line.separator");

		str += "\tAllowed Threads:\t" + descriptor.getParam("nthreads") + System.getProperty("line.separator");

		str += "\tMin size:\t\t" + descriptor.getParam("min_size") + System.getProperty("line.separator");

		str += "\tMax size:\t\t" + descriptor.getParam("max_size") + System.getProperty("line.separator");

		str += "\tQuality Measure:\t" + descriptor.getParam("str_quality_measure") + System.getProperty("line.separator");
		
		str += "\tExplore dimensions:\t" + descriptor.getFlag("explore_dimensions") + System.getProperty("line.separator");

		str += "\tSamples:\t\t" + descriptor.getParam("samples") + System.getProperty("line.separator");
		
		str += "\tSample Size:\t\t" + descriptor.getParam("sample_size") + System.getProperty("line.separator");
		
		str += "\tMedium:\t\t\t" + descriptor.getParam("medium") + System.getProperty("line.separator");
		
		str += "\tMPT:\t\t\t" + descriptor.getParam("movelets_per_trajectory") + System.getProperty("line.separator");
		
		str += "\tOutput:\t\t\t" + descriptor.getParam("output") + System.getProperty("line.separator");
		
		if(descriptor.getFlag("last_prunning"))
			str += "\tWITH Last Prunning" + System.getProperty("line.separator");
		else
			str += "\tWITHOUT Last Prunning" + System.getProperty("line.separator");
		
		str += "\tAttributes:"+ System.getProperty("line.separator") 
			+ descriptor.toString() + System.getProperty("line.separator");
		
		return str;

	}
	
	public static String configRespath(String descFile, Descriptor descriptor) {
		String DESCRIPTION_FILE_NAME = FilenameUtils.removeExtension(
				new File(descFile).getName());
		
		if (descriptor.getFlag("explore_dimensions"))
			DESCRIPTION_FILE_NAME += "_ED"; 
		
		if (descriptor.getParamAsInt("max_number_of_features") != -1)
			DESCRIPTION_FILE_NAME += "_MNF-" + descriptor.getParamAsInt("max_number_of_features"); 
		
		String resultDirPath = descriptor.getParamAsText("respath");
		
//		if (PIVOTS_FILE != null) {
//			resultDirPath += DESCRIPTION_FILE_NAME + "_MM_PivotsBOW";
//			outside_pivots = true;
//		}
//		else {
			if(descriptor.getFlag("pivots"))
				resultDirPath += DESCRIPTION_FILE_NAME + "_MM_Pivots/Porcentage_" + descriptor.getParamAsText("pivot_porcentage");
			else {
				if(descriptor.getParamAsInt("max_size") == -3) {
					resultDirPath = DESCRIPTION_FILE_NAME + "_MM_LOG";
				}else
					resultDirPath = DESCRIPTION_FILE_NAME + "_MM";
			}
//		}
	
		
		if(descriptor.getFlag("last_prunning"))
			resultDirPath = resultDirPath + "_With-Last-Prunning/";
		else
			resultDirPath = resultDirPath + "_Witout-Last-Prunning/";

		
//		String trainDirPath = descriptor.getParamAsText("respath") + "/train";
//		String testDirPath = descriptor.getParamAsText("respath") + "/test";
		return resultDirPath;
	}

}
