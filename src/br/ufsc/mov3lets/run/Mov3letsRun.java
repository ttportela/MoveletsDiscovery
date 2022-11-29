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
package br.ufsc.mov3lets.run;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import br.ufsc.mov3lets.method.Mov3lets;
import br.ufsc.mov3lets.method.discovery.structures.TimeContract;
import br.ufsc.mov3lets.method.loader.LoaderAdapter;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.utils.Mov3letsUtils;

/**
 * The Class Mov3letsRun.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 */
public class Mov3letsRun {
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		// 1.1 - PARAMS:
		HashMap<String, Object> params = configure(args);
		
//		try { System.in.read(); } catch (IOException e1) {} // TEMP

		// Config to - Show trace messages OR Ignore all
		if (params.containsKey("verbose") && (Boolean) params.get("verbose"))
			Mov3letsUtils.getInstance().configLogger();
		
		// Starting Date:
		Mov3letsUtils.trace(new Date().toString());

		// 1.2 - RUN Configuration
		Mov3lets<String> mov;
		if (!params.containsKey("descfile")) {
			mov = new Mov3lets<String>(params);
			
//			System.out.println(Paths.get(LoaderAdapter.getFileName("train.mat", mov.getDescriptor())).toString());
			if (!Paths.get(LoaderAdapter.getFileName("train.mat", mov.getDescriptor())).toFile().exists()) {
				showUsage(params, "-descfile\tDescription file must be set OR must provide .mat files at -curpath!");
				return;
			}

//			mov.getDescriptor().setParam("result_dir_path", mov.configRespath(mov.getDescriptor()));
		} else {
//			String descFile = params.get("descfile").toString();
			try {
				mov = new Mov3lets<String>(params.get("descfile").toString(), params);
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				showUsage(params, "-descfile\tDescription file not found: " + e.getMessage());
	//			e.printStackTrace();
				return;
			}
			
//			mov.getDescriptor().setParams(params);
//			mov.getDescriptor().setParam("result_dir_path", mov.configRespath(mov.getDescriptor()));
		}
		
		// STEP 1.3 - Input:
		Mov3letsUtils.getInstance().startTimer("[1] >> Load Input");
//				Mov3letsUtils.getInstance().printMemory();
		try {
			mov.loadTrain();
		} catch (IOException e) {
			showUsage(mov.getDescriptor().getParams(), "-curpath\tCould not load train dataset: " + e.getMessage());
			e.printStackTrace();
			return;
		} catch (Exception e) {
			error(e);
		}
		
		try {
			// Try loading the test dataset
			mov.loadTest();
		} catch (IOException e) {
			// Empty if can't
			Mov3letsUtils.trace("Empty test dataset: "+ e.getMessage() +" [continuing]");
//			e.printStackTrace();
			mov.setTest(new ArrayList<MAT<String>>());
		} catch (Exception e) {
			error(e);
		}

		// Set Result Dir:
		mov.getDescriptor().setParam("result_dir_path", mov.configRespath(mov.getDescriptor()));
		mov.setResultDirPath(mov.getDescriptor().getParamAsText("result_dir_path"));
		
		// Trace configurations:
		Mov3letsUtils.trace(mov.showConfiguration(mov.getDescriptor()));
		
		if (mov.getTrain().isEmpty()) { 
			showUsage(mov.getDescriptor().getParams(), "-curpath\tEmpty training set!");
			return;
		}		
		Mov3letsUtils.getInstance().stopTimer("[1] >> Load Input");
		
		// STEP 1.4 - Pre-processing (optional):
		try {
			mov.preProcessing();
		} catch (Exception e) {
			Mov3letsUtils.trace("[Warning] pre-processing: "+ e.getMessage() +" [continuing]");
			error(e);
		}

		Mov3letsUtils.trace("");
		Mov3letsUtils.trace(mov.printAttributes(mov.getDescriptor()));

		Mov3letsUtils.trace("");
		Mov3letsUtils.printMemory();
		
		// STEP 2 - RUN:
		try {
			if (params.containsKey("data_resample"))
				// If re-sampling data
				mov.sampler_mov3lets((int) params.get("data_resample"));
			else {
				// One hold-out run:
				Mov3letsUtils.getInstance().startTimer("[3] >> Processing time");
				mov.mov3lets();
				Mov3letsUtils.trace("");
				Mov3letsUtils.getInstance().stopTimer("[3] >> Processing time");
			}
		} catch (Exception e) {
			error(e);
		}
//		System.out.println(inputFile);
		
		// End Date:
		Mov3letsUtils.trace(new Date().toString());
		
//		while (true); // TEMP
	}

	/**
	 * Mehod error. 
	 * 
	 * @param e
	 */
	public static void error(Exception e) {
		// Empty if can't
		Mov3letsUtils.traceE("Movelets encoutered the following error.", e);
		Mov3letsUtils.getInstance().stopTimer("[3] >> Processing time");
		e.printStackTrace();
		System.exit(1);
	}
	
	/**
	 * Show usage.
	 *
	 * @param params the params
	 * @param errorMessage the error message
	 */
	public static void showUsage(HashMap<String, Object> params, String errorMessage) {
		Mov3letsUtils.trace(errorMessage + " [ERROR]");
		Mov3letsUtils.trace("Usage: java -jar Movelets.jar [args...]");
//		Mov3letsUtils.trace(printParams(params, null));
	}

	/**
	 * Default params.
	 *
	 * @return the hash map
	 */
	public static HashMap<String, Object> defaultParams() {
		
		HashMap<String, Object> params = new HashMap<String, Object>();

//		params.put("curpath", 					 null);
//		params.put("respath",					 null);
//		params.put("descfile",					 null);
//		params.put("outside_pivots",			 null);
		params.put("nthreads",					 1);
		params.put("min_size",					 1);
		params.put("max_size",					 -1); // unlimited maxSize
		params.put("str_quality_measure",		 "LSP"); // LSP | PROP | LSPEA
//		params.put("cache",						 true); // Deprecated: JSON configuration.	
		params.put("explore_dimensions",		 false);
		params.put("max_number_of_features",	 -1);					
		params.put("feature_limit",				 false);
		params.put("samples",					 1);			
		params.put("sample_size",				 0.5);			
		params.put("medium",					 "none"); // Other values minmax | sd | interquartil
		params.put("output",					 "discrete"); // Other values: numeric | discrete	
		params.put("outputters",				 "CSV,JSON"); // OUTPUTTERs Styles							
		params.put("delay_output",				 true);			
		params.put("pivots",					 false);						
		params.put("supervised",				 false);	
		params.put("movelets_per_trajectory",	 -1);	// Filtering		
		params.put("lowm_memory",				 false);		
		params.put("last_prunning",				 false);		
		params.put("pivot_porcentage",			 10);
		params.put("only_pivots",				 false);
		params.put("interning",  				 true);
		params.put("verbose",				 	 true);
		params.put("version",				 	 "hiper-pivots");
//		params.put("tau",					 	 0.9);
		params.put("relative_tau",			 	 true);
//		params.put("bucket_slice",				 0.1);
//		params.put("filter_strategy",		 	 "none"); // Use Buckets
		params.put("LDM",					 	 false);
		
		return params;
	}
	
	/**
	 * Configure.
	 *
	 * @param args the args
	 * @return the hash map
	 */
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
			case "-inprefix":
			case "-input_file_prefix":
				params.put("input_file_prefix", value);
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
			case "-Q":
			case "-QM":
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
				if (Integer.valueOf(value) < -2)
					params.put("feature_limit", true);
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
			case "-outstyle":
			case "-outputters":
				params.put("outputters", value);			
				break;	
			case "-delay_output":
				params.put("delay_output", Boolean.valueOf(value));
				break;		
			case "-pvt":
			case "-pivots":
				if (Boolean.valueOf(value)) {
					params.put("pivots",  true);
					params.put("version", "pivots");
				} else
					params.put("pivots", false);				
				break;		
//			case "-sup":
//			case "-supervised":
//				params.put("supervised", Boolean.valueOf(value));				
//				break;		
//			case "-Al":	
//			case "-al":	
//			case "-AL":
//			case "-feature_limit":
//				params.put("feature_limit", Boolean.valueOf(value));				
//				break;
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
//			case "-index":
//				params.put("index", Boolean.valueOf(value));
//				break;
			case "-interning":
				params.put("interning", Boolean.valueOf(value));
				break;
			case "-v":
			case "-verbose":
				params.put("verbose", Boolean.valueOf(value));
				break;
			case "-d":
			case "-version":
				params.put("version", value.toLowerCase());
				if ("pivots".equalsIgnoreCase(value)) params.put("pivots",  true);
				break;		
			case "-tau":	
			case "-fixed_tau":
			case "-TAU":	
			case "-T":	
			case "-TF":
				params.put("tau", Double.valueOf(value));	
				params.put("relative_tau", false);		
				break;	
			case "-relative_tau":
			case "-TR":
				params.put("tau", Double.valueOf(value));	
				params.put("relative_tau", true);		
				break;	
			case "-bucket_slice":	
			case "-BU":
				params.put("bucket_slice", Double.valueOf(value));			
				break;	
			case "-filter":
			case "-filter_strategy":
			case "-feature_selecion":
			case "-FS":
			case "-fs":
				params.put("filter_strategy", value);
				break;
			case "-feature_extraction_strategy":
			case "-feature_extraction":
			case "-FE":
			case "-fe":
				params.put("feature_extraction_strategy", value);
				break;
//			case "-LDM":	
//			case "-ldm":
//				params.put("LDM", Boolean.valueOf(value));			
//				break;	
			case "-mknn_k":
			case "-k":
				params.put("mknn_k", Integer.valueOf(value));		
				break;	
			case "-random_seed":
			case "-seed":
			case "-s":
				params.put("random_seed", Integer.valueOf(value));		
				break;		
			case "-th_temporal":
			case "-temporal_threshold":
				params.put("temporal_threshold", Integer.valueOf(value));	
				break;	
			case "-th_spatial":
			case "-spatial_threshold":
				params.put("spatial_threshold", Double.valueOf(value));		
				break;
			case "-th_numeric":
			case "-numeric_threshold":
				params.put("numeric_threshold", Integer.valueOf(value));	
				break;
			case "-TC":
			case "-tc":
			case "-time_contract":
				params.put("time_contract", new TimeContract(value));	
				break;
			case "-fold":
			case "-resample":
			case "-data_resample":
				params.put("data_resample", Integer.valueOf(value));	
				break;
			case "-data_trim":
				params.put("data_trim", Double.valueOf(value));	
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

}
