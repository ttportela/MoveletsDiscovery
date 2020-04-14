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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import br.com.tarlis.mov3lets.method.Mov3lets;
import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Mov3letsRun {
	
	public static void main(String[] args) {
		// Starting Date:
		Mov3letsUtils.trace(new Date().toString());

		// PARAMS:
		HashMap<String, Object> params = configure(args);
		
		if (!params.containsKey("descfile")) {
			showUsage(params, "-descfile\tDescription file must be set!");
			return;
		}
		String descFile = params.get("descfile").toString();

		// Config to - Show trace messages OR Ignore all
		if (params.containsKey("verbose") && (Boolean) params.get("verbose"))
			Mov3letsUtils.getInstance().configLogger();
		
//		String inputFile = (args.length > 1? args[1] : "data/foursquare.csv");
		
		// 2 - RUN
		Mov3lets<String> mov;
		try {
			mov = new Mov3lets<String>(descFile);
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			showUsage(params, "-descfile\tDescription file not found!");
			e.printStackTrace();
			return;
		}

		mov.getDescriptor().setParams(params);
		params.put("result_dir_path", configRespath(descFile, mov.getDescriptor()));
		
		// STEP 1 - Input:
		Mov3letsUtils.getInstance().startTimer("[1] >> Load Input");
//				Mov3letsUtils.getInstance().printMemory();
		try {
			mov.setTrain(mov.loadTrajectories("train"));
			mov.setTest(mov.loadTrajectories("test"));
		} catch (IOException e) {
			showUsage(params, "-curpath\tCould not load train dataset!");
			e.printStackTrace();
			return;
		}
		
		try {
			// Try loading the test dataset
			mov.setTest(mov.loadTrajectories("test"));
		} catch (IOException e) {
			// Empty if can't
			Mov3letsUtils.trace("Empty test dataset... [continue]");
			mov.setTest(new ArrayList<MAT<String>>());
		}
		
		if (mov.getTrain().isEmpty()) { 
			showUsage(params, "-curpath\tEmpty training set!");
			return;
		}
				
		// Set Result Dir:
		mov.setResultDirPath(mov.getDescriptor().getParamAsText("result_dir_path"));
		Mov3letsUtils.trace(showConfiguration(mov.getDescriptor()));
		Mov3letsUtils.getInstance().stopTimer("[1] >> Load Input");
		Mov3letsUtils.printMemory();
		
		// STEP 2 - RUN:
		Mov3letsUtils.getInstance().startTimer("[2] >> Processing time");
		mov.mov3lets();
		Mov3letsUtils.getInstance().stopTimer("[2] >> Processing time");
//		System.out.println(inputFile);
		
		// End Date:
		Mov3letsUtils.trace(new Date().toString());
	}
	
	public static void showUsage(HashMap<String, Object> params, String errorMessage) {
		Mov3letsUtils.trace(errorMessage + " [ERROR]");
		Mov3letsUtils.trace("Usage: java -jar -jar MASTERMov3lets.jar [args...]");
		Mov3letsUtils.trace(printParams(params));
	}

	public static String printParams(HashMap<String, Object> params) {
		String str = "Configurations:" + System.getProperty("line.separator");

		str += "   -curpath\tDatasets directory:\t" + params.get("curpath") + System.getProperty("line.separator");
		str += "   -respath\tResults directory:\t" + (params.containsKey("result_dir_path")? params.get("result_dir_path") : params.get("respath")) + System.getProperty("line.separator");
		str += "   -descfile\tDescription file :\t" + params.get("descfile") + System.getProperty("line.separator");
		str += "   -nt\t\tAllowed Threads:\t" + params.get("nthreads") + System.getProperty("line.separator");
		str += "   -ms\t\tMin size:\t\t" + params.get("min_size") + System.getProperty("line.separator");
		str += "   -Ms\t\tMax size:\t\t" + params.get("max_size") + "\t[(Any positive) OR (all sizes, -1) OR *(deprecated, -2)* OR (log, -3)]" + System.getProperty("line.separator");
		str += "   -mnf\t\tMax # of Features:\t" + params.get("max_number_of_features") + "\t[(Any positive) OR (explore dimensions, -1) OR (log, -2)]" + System.getProperty("line.separator");
		str += "   -ed\t\tExplore dimensions:\t" + params.get("explore_dimensions") + "\t[Same as -mnf -1]" + System.getProperty("line.separator");
		str += "   -samples\tSamples:\t\t" + params.get("samples") + System.getProperty("line.separator");
		str += "   -sampleSize\tSample Size:\t\t" + params.get("sample_size") + System.getProperty("line.separator");
		str += "   -q\t\tQuality Measure:\t" + params.get("str_quality_measure") + System.getProperty("line.separator");
		str += "   -medium\tMedium:\t\t\t" + params.get("medium") + System.getProperty("line.separator");
		str += "   -mpt\t\tMovelets Per Traj.:\t" + params.get("movelets_per_trajectory") + System.getProperty("line.separator");
		str += "   -output\tOutput:\t\t\t" + params.get("output") + System.getProperty("line.separator");
		str += "   -version\tMov. Discovery Impl.:\t" + params.get("version") + System.getProperty("line.separator");
		
		str += System.getProperty("line.separator") + "    Optimizations: ";
		str += System.getProperty("line.separator")
				+ "\t[Index: " + (params.containsKey("index")? (boolean)params.get("index") : false) + "], "
				+ "[Interning: " + (params.containsKey("interning")? (boolean)params.get("interning") : false) + "]"
				+ System.getProperty("line.separator");
		
		return str;
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
		params.put("version",				 	 "2.0");
		
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
			case "-d":
			case "-version":
				params.put("version", value);
				break;
			default:
				System.err.println("Parâmetro " + key + " inválido.");
				System.exit(1);
			}
		}
		
		if (params.containsKey("max_number_of_features") && (int) params.get("max_number_of_features") == -1)
			params.put("explore_dimensions", true);

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
		
		str += printParams(descriptor.getParams());
		
		if(descriptor.getFlag("last_prunning"))
			str += "\tWITH Last Prunning" + System.getProperty("line.separator");
		else
			str += "\tWITHOUT Last Prunning" + System.getProperty("line.separator");
		
		str += "\tAttributes:"+ System.getProperty("line.separator") 
			+ descriptor.toString() + System.getProperty("line.separator");
		
		return str;

	}
	
	public static String configRespath(String descFile, Descriptor descriptor) {
		
		String resultDirPath = "MasterMovelets" + System.getProperty("file.separator");
		
//		if (PIVOTS_FILE != null) {
//			resultDirPath += "_PivotsBOW";
//			outside_pivots = true;
//		}
//		else {
			if(descriptor.getFlag("pivots"))
				resultDirPath += "Pivots_"
							  +  "Porcentage_" + descriptor.getParamAsText("pivot_porcentage")
							  + "_";
			else {
				if(descriptor.getParamAsInt("max_size") == -3) {
					resultDirPath += "Log_";
				} // else resultDirPath = + "_MM"; // No need
			}
//		}

		String DESCRIPTION_FILE_NAME = FilenameUtils.removeExtension(
				new File(descFile).getName());
		
		if (descriptor.getFlag("explore_dimensions"))
			DESCRIPTION_FILE_NAME += "_ED"; 
		
		if (descriptor.getParamAsInt("max_number_of_features") > 0)
			DESCRIPTION_FILE_NAME += "_MNF-" + descriptor.getParamAsInt("max_number_of_features"); 
		
		if(descriptor.getFlag("last_prunning"))
			DESCRIPTION_FILE_NAME += "_With-Last-Prunning";
		else
			DESCRIPTION_FILE_NAME += "_Witout-Last-Prunning";

		return Paths.get(descriptor.getParamAsText("respath"), resultDirPath + DESCRIPTION_FILE_NAME).toString();
	}

}
