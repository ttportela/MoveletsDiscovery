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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import br.ufsc.mov3lets.method.Mov3lets;
import br.ufsc.mov3lets.method.discovery.structures.TimeContract;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.utils.Mov3letsUtils;
import br.ufsc.mov3lets.utils.TableList;

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
		
		if (!params.containsKey("descfile")) {
			showUsage(params, "-descfile\tDescription file must be set!");
			return;
		}
		String descFile = params.get("descfile").toString();
				
		// 1.2 - RUN Configuration
		Mov3lets<String> mov;
		try {
			mov = new Mov3lets<String>(descFile, params);
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			showUsage(params, "-descfile\tDescription file not found: " + e.getMessage());
//			e.printStackTrace();
			return;
		}

//		mov.getDescriptor().setParams(params);
		mov.getDescriptor().setParam("result_dir_path", configRespath(descFile, mov.getDescriptor()));

		// Set Result Dir:
		mov.setResultDirPath(mov.getDescriptor().getParamAsText("result_dir_path"));
		// Trace configurations:
		Mov3letsUtils.trace(showConfiguration(mov.getDescriptor()));
		
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
			e.printStackTrace();
			mov.setTest(new ArrayList<MAT<String>>());
		} catch (Exception e) {
			error(e);
		}
		
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
		Mov3letsUtils.trace(printAttributes(mov.getDescriptor()));

		Mov3letsUtils.trace("");
		Mov3letsUtils.printMemory();
		
		// STEP 2 - RUN:
		Mov3letsUtils.getInstance().startTimer("[3] >> Processing time");
		try {
			mov.mov3lets();
		} catch (Exception e) {
			error(e);
		}
		Mov3letsUtils.trace("");
		Mov3letsUtils.getInstance().stopTimer("[3] >> Processing time");
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
		Mov3letsUtils.traceE("HIPERMovelets encoutered the following error.", e);
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
		Mov3letsUtils.trace("Usage: java -jar HIPERMovelets.jar [args...]");
//		Mov3letsUtils.trace(printParams(params, null));
	}

	/**
	 * Prints the params.
	 *
	 * @param params the params
	 * @param descriptor the descriptor
	 * @return the string
	 */
	public static String printParams(HashMap<String, Object> params, Descriptor descriptor) {
		String str = "Configurations:" + System.getProperty("line.separator");
		
//		String[] columns = {"Option", "Description", "Value", "Help"};
		Object[][] data = {
//				{"-curpath", 			"Datasets directory", 		params.get("curpath"), 						""},
//				{"-respath", 			"Results directory", 		(params.containsKey("result_dir_path")? params.get("result_dir_path") : params.get("respath")), 				""},
//				{"-descfile", 			"Description file", 		params.get("descfile"), 					""},
				{"-nt", 				"Allowed Threads", 			params.get("nthreads"), 					""},
				{"-ms", 				"Min size", 				params.get("min_size"), 					"Any positive | -1 | Log: -2"},
				{"-Ms", 				"Max size", 				params.get("max_size"), 					"Any | All sizes: -1 | Log: -3 or -4"},
//				{"", 					"", 						"",						 					"All sizes: -1,"},
//				{"", 					"", 						"", 										"Log: -3"},
				{"-mnf", 				"Max. Dimensions",			params.get("max_number_of_features"), 		"Any | Explore dim.: -1 | Log: -2 | Other: -3"},
//				{"", 					"", 						"",						 					"Explore dim.: -1,"},
//				{"", 					"", 						"", 										"Log: -3"},
//				{"-ed", 				"Explore dimensions", 		params.get("explore_dimensions"), 			"Same as -mnf -1"},
				{"-samples", 			"Samples", 					params.get("samples"), 						""},
				{"-sampleSize", 		"Sample Size", 				params.get("sample_size"), 					""},
				{"-q", 					"Quality Measure", 			params.get("str_quality_measure"), 			""},
				{"-medium", 			"Medium", 					params.get("medium"), 						""},
				{"-mpt", 				"Movelets Per Traj.", 		params.get("movelets_per_trajectory"), 		"Any | Auto: -1"},
				{"-output", 			"Output", 					params.get("output")+
																	" ("+params.get("outputters")+")", 			""},
				{"", 					"", 						"", 										""},
				{"-version", 			"Version Impl.", 			params.get("version"), 						"master, super, hiper[-pivots], random, ultra"},
				{"", 					"-- Last Prunning",			params.get("last_prunning"), 				""},
			};
		
		str += "   -curpath		Datasets directory:	" + params.get("curpath") + System.getProperty("line.separator");
		str += "   -respath		Results directory: 	" + (params.containsKey("result_dir_path")? params.get("result_dir_path") : params.get("respath")) + System.getProperty("line.separator");
		str += "   -descfile 		Description file : 	" + params.get("descfile") + System.getProperty("line.separator");
//		str += "   -nt 						Allowed Threads: 				" + params.get("nthreads") + System.getProperty("line.separator");
//		str += "   -ms						Min size: 						" + params.get("min_size") + System.getProperty("line.separator");
//		str += "   -Ms						Max size 						" + params.get("max_size")  + System.getProperty("line.separator")
//		    	+ "							[(Any positive) OR (all sizes, -1) OR (log, -3)]" + System.getProperty("line.separator");
//		str += "   -mnf 					Max # of Features: 				" + params.get("max_number_of_features")  + System.getProperty("line.separator")
//				+ "							[(Any positive) OR (explore dimensions, -1) OR (log, -2)]" + System.getProperty("line.separator");
//		str += "   -ed 					Explore dimensions: 			" + params.get("explore_dimensions") + System.getProperty("line.separator")
//				+ " 							[Same as -mnf -1]" + System.getProperty("line.separator");
//		str += "   -samples 				Samples: 					" + params.get("samples") + System.getProperty("line.separator");
//		str += "   -sampleSize  			Sample Size: 						" + params.get("sample_size") + System.getProperty("line.separator");
//		str += "   -q						Quality Measure: 				" + params.get("str_quality_measure") + System.getProperty("line.separator");
//		str += "   -medium 				Medium:						" + params.get("medium") + System.getProperty("line.separator");
//		str += "   -mpt 					Movelets Per Traj.: 				" + params.get("movelets_per_trajectory") + System.getProperty("line.separator");
//		str += "   -output 				Output:						" + params.get("output") + System.getProperty("line.separator");
//		str += "   -version 				Mov. Discovery Impl.:			" + params.get("version") + System.getProperty("line.separator");
		
		TableList at = new TableList("Option", "Description", "Value", "Help");
//		at.addRule();
//		at.addRow("Option", "Description", "Value", "Help");
//		at.addRule();
		for (Object[] row : data) {
			at.addRow(row);
		}
//		at.addRule();
		
		if (params.containsKey("tau"))
			if (params.get("relative_tau").equals(true)) 
				at.addRow(new Object[] {"", "-- TAU (relative)", params.get("tau"), ""});
			else
				at.addRow(new Object[] {"", "-- TAU (absolute)", params.get("tau"), ""});

		if (params.containsKey("bucket_slice"))
			at.addRow(new Object[] {"", "-- % Limit", params.get("bucket_slice"), ""});

		if (params.containsKey("random_seed"))
			at.addRow(new Object[] {"", "-- Random Seed", params.get("random_seed"), ""});
		
		if (params.containsKey("feature_extraction_strategy"))
			at.addRow(new Object[] {"", "-- Feature Extraction", params.get("feature_extraction_strategy"), ""});
		
		if (params.containsKey("filter_strategy"))
			at.addRow(new Object[] {"", "-- Feature Selection", params.get("filter_strategy"), ""});
		
		if (params.containsKey("time_contract"))
			at.addRow(new Object[] {"-TC", "Time Contract", params.get("time_contract"), "Use: w(eeks), d, h, m, s(econds)"});
		
//		at.addRow("Optimizations:", "", "", "");
//		at.addRow("", "Interning", 	(params.containsKey("interning")? (boolean)params.get("interning") : false), 	"");
//		at.addRow("", "Index", 		(params.containsKey("index")? 	  (boolean)params.get("index") 	   : false), 	"");
//		at.addRule();
//		
//		if (descriptor != null) {
//			at.addRow("Attributes:", "", "Type:", "Comparrator:");
//			int i = 1;
//			for (AttributeDescriptor attr : descriptor.getAttributes())
//				at.addRow(i++, attr.getOrder() + " - " + attr.getText(), attr.getType(), attr.getComparator().toString());
//		}
		
		str += at.print();
		
//		str += System.getProperty("line.separator") + "    Optimizations: ";
//		str += System.getProperty("line.separator")
//				+ "\t[Index: " + (params.containsKey("index")? (boolean)params.get("index") : false) + "], "
//				+ "[Interning: " + (params.containsKey("interning")? (boolean)params.get("interning") : false) + "]"
//				+ System.getProperty("line.separator");
		
		return str;
	}
	
	public static String printAttributes(Descriptor descriptor) {
		String str = "Attributes and Features:" + System.getProperty("line.separator");
		
		TableList at = new TableList("#", "Attribute", "Type", "Comparator");
//		at.addRule();
		
		if (descriptor != null) {
			if (descriptor.getFeatures() == null || descriptor.getFeatures().isEmpty()) {
				int i = 1;
				for (AttributeDescriptor attr : descriptor.getAttributes())
					at.addRow(i++, attr.getOrder() + " - " + attr.getText(), attr.getType(), attr.getComparator().toString());
			} else {
				for (int i = 0; i < descriptor.getAttributes().size(); i++) {
					AttributeDescriptor attr = descriptor.getAttributes().get(i);
					
					if (i == (descriptor.getAttributes().size() - descriptor.getFeatures().size())) {
						at.addRule();
						at.addRow("Features:", "", "", "");
					}
					
					at.addRow(i+1, attr.getOrder() + " - " + attr.getText(), attr.getType(), attr.getComparator().toString());
				}
			}
		}
		
		str += at.print();
		
		return str;
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
		params.put("output",					 "numeric"); // Other values: numeric | discrete	
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
		params.put("version",				 	 "2.0");
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
			default:
				System.err.println("Parâmetro " + key + " inválido.");
				System.exit(1);
			}
		}
		
		if (params.containsKey("max_number_of_features") && (int) params.get("max_number_of_features") == -1)
			params.put("explore_dimensions", true);

		return params;
	}
	
	
	/**
	 * Show configuration.
	 *
	 * @param descriptor the descriptor
	 * @return the string
	 */
	public static String showConfiguration(Descriptor descriptor) {

		String str = new String();
		
		if(descriptor.getFlag("pivots"))
			str += "Starting Movelets +Pivots extractor (10%) " + System.getProperty("line.separator");
		else if(descriptor.getParamAsInt("max_size")==-3)
			str += "Starting Movelets +Log extractor " + System.getProperty("line.separator");
		else
			str += "Starting Movelets extractor " + System.getProperty("line.separator");
		
		if (descriptor.hasParam("outside_pivots"))
			str += "Getting pivots from outside file:" + descriptor.getParam("outside_pivots") + System.getProperty("line.separator");
		
		str += printParams(descriptor.getParams(), descriptor);
		
//		if(descriptor.getFlag("last_prunning"))
//			str += "\tWITH Last Prunning" + System.getProperty("line.separator");
//		else
//			str += "\tWITHOUT Last Prunning" + System.getProperty("line.separator");
//		
//		str += "\tAttributes:"+ System.getProperty("line.separator") 
//			+ descriptor.toString() + System.getProperty("line.separator");
		
		return str;

	}
	
	/**
	 * Config respath.
	 *
	 * @param descFile the desc file
	 * @param descriptor the descriptor
	 * @return the string
	 */
	public static String configRespath(String descFile, Descriptor descriptor) {
		
		String resultDirPath = "MASTERMovelets" + System.getProperty("file.separator");
		
		if (descriptor.getFlag("supervised") || descriptor.getParamAsText("version").equals("super")) {
			resultDirPath += "Super_";
		} else if (descriptor.getParamAsText("version").equals("hiper") || descriptor.getParamAsText("version").equals("hpivots")) {
			resultDirPath += "Hiper_";
		} else if (descriptor.getFlag("pivots") || descriptor.getParamAsText("version").equals("pivots")) {			
			resultDirPath += "Pivots_";		
		} else if (descriptor.getParamAsText("version").equals("1.0")) {
			resultDirPath += "V1_";
		} else if (descriptor.getParamAsText("version").equals("3.0")) {
			resultDirPath += "V3_";
		} else if (descriptor.getParamAsText("version").equals("4.0")) {
			resultDirPath += "V4_";
		}
		
//		if (PIVOTS_FILE != null) {
//			resultDirPath += "_PivotsBOW";
//			outside_pivots = true;
//		}
//		else {
			if(descriptor.getFlag("pivots"))
//				resultDirPath += "Pivots_"
				resultDirPath +=  "Porcentage_" + descriptor.getParamAsText("pivot_porcentage")
							  + "_";
			else {
				if(descriptor.getParamAsInt("max_size") == -3) {
					resultDirPath += "Log_";
				} // else resultDirPath = + "_MM"; // No need
			}
//		}

		String DESCRIPTION_FILE_NAME = FilenameUtils.removeExtension(
				new File(descFile).getName());
		
		DESCRIPTION_FILE_NAME += "_" + descriptor.getParamAsText("str_quality_measure"); 
				
		if (descriptor.getFlag("explore_dimensions"))
			DESCRIPTION_FILE_NAME += "_ED"; 
		
		if (descriptor.getParamAsInt("max_number_of_features") > 0)
			DESCRIPTION_FILE_NAME += "_MNF-" + descriptor.getParamAsInt("max_number_of_features"); 
		
		if(descriptor.getFlag("last_prunning"))
			DESCRIPTION_FILE_NAME += "_WithLastPrunning";
//		else
//			DESCRIPTION_FILE_NAME += "_Witout-Last-Prunning";

		return Paths.get(descriptor.getParamAsText("respath"), resultDirPath + DESCRIPTION_FILE_NAME).toString();
	}

}
