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
import java.util.Map;

import br.ufsc.mov3lets.method.Mov3lets;
import br.ufsc.mov3lets.method.discovery.structures.TimeContract;
import br.ufsc.mov3lets.method.loader.LoaderAdapter;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.utils.Mov3letsUtils;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

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
		Map<String, Object> params = configure(args);

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
				// e.printStackTrace();
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
			Mov3letsUtils.trace("Empty test dataset: " + e.getMessage() + " [continuing]");
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
			Mov3letsUtils.trace("[Warning] pre-processing: " + e.getMessage() + " [continuing]");
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
	 * @param params       the params
	 * @param errorMessage the error message
	 */
	public static void showUsage(Map<String, Object> params, String errorMessage) {
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
		params.put("nthreads", 1);
		params.put("min_size", 1);
		params.put("max_size", -1); // unlimited maxSize
		params.put("str_quality_measure", "LSP"); // LSP | PROP | LSPEA
//		params.put("cache",						 true); // Deprecated: JSON configuration.	
		params.put("explore_dimensions", true);
		params.put("max_number_of_features", -1);
		params.put("feature_limit", false);
		params.put("samples", 1);
		params.put("sample_size", 0.5);
		params.put("medium", "none"); // Other values minmax | sd | interquartil
		params.put("output", "discrete"); // Other values: numeric | discrete
		params.put("outputters", "CSV,JSON"); // OUTPUTTERs Styles
		params.put("delay_output", true);
		params.put("pivots", false);
		params.put("supervised", false);
		params.put("movelets_per_trajectory", -1); // Filtering
		params.put("lowm_memory", false);
		params.put("last_prunning", false);
		params.put("pivot_porcentage", 10);
		params.put("only_pivots", false);
		params.put("interning", true);
		params.put("verbose", true);
		params.put("version", "hiper-pivots");
//		params.put("tau",					 	 0.9);
		params.put("relative_tau", true);
//		params.put("bucket_slice",				 0.1);
//		params.put("filter_strategy",		 	 "none"); // Use Buckets
		params.put("LDM", false);

//		params.put("data_resample",				 false);
//		params.put("resample_prop",				 0.5);
//		params.put("resample_train",			 0.7);

		return params;
	}

	/**
	 * Configure.
	 *
	 * @param args the args
	 * @return the hash map
	 */
	public static Map<String, Object> configure(String[] args) {
		ArgumentParser parser = ArgumentParsers.newFor("Movelet Discovery").build()
                .defaultHelp(true)
                .description("Methods for discovering movelets in multiple aspect trajectories.");
		
        setArguments(parser);
        
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        
		Map<String, Object> params = ns.getAttrs();
		
		while (params.values().remove(null));
		
		if (params.containsKey("relative_tau")) {
			params.put("tau", (Double) params.get("relative_tau"));
			params.put("relative_tau", true);
		} else if (params.containsKey("fixed_tau")) {
			params.put("tau", (Double) params.get("fixed_tau"));
			params.put("relative_tau", false);
		} else
			params.put("relative_tau", true); // DEFAULT
		
		if (params.containsKey("max_number_of_features") && (int) params.get("max_number_of_features") < -2)
			params.put("feature_limit", true);

		if (params.containsKey("max_number_of_features") && (int) params.get("max_number_of_features") == -1)
			params.put("explore_dimensions", true);
		
		if (params.containsKey("time_contract"))
			params.put("time_contract", new TimeContract((String) params.get("time_contract")));
		
//		System.out.println(params);
		
		return params;
	}

	public static void setArguments(ArgumentParser parser) {
		parser.addArgument("-curpath", "--curpath")
				.required(true)
				.help("Working Data Path");
		parser.addArgument("-respath", "--respath")
				.required(true)
				.help("Results Path");
		parser.addArgument("-descfile", "--descfile")
				.required(true)
				.help("Descriptor File");

		parser.addArgument("-nt", "--nthreads")
				.type(Integer.class)
				.setDefault(1)
				.help("the number of threads to be created for running the experiment");

		parser.addArgument("-v", "--verbose")
				.type(Boolean.class)
//				.choices(true, false)
				.setDefault(true)
				.help("specify whether to compute distances (true) or similarities (false)");
		
		parser.addArgument("-vmem", "--memory-verbose")
				.type(Boolean.class)
				.setDefault(false)
				.help("specify whether to verbose memory use at each progress update.");

		parser.addArgument("-d", "-version", "--version")
			.type(String.class)
			.setDefault("hiper-pivots")
//			.choices("hiper", "hiper-pivots", "random", "ultra", "master", "pivots", "random")
			.help("specify the movelets discovery method (hiper[-pivots], random, ultra)");
		
		
		parser.addArgument("-inprefix", "--input-file-prefix")
			.help("Prefix of input files configured in the descriptor file.");

		parser.addArgument("-ms", "-minSize", "--min-size")
			.type(Integer.class)
			.setDefault(1)
			.help("specify the minimum subtrajectory size (Any positive | -1 | Log: -2)");
		parser.addArgument("-Ms", "-maxSize", "--max-size")
			.type(Integer.class)
			.setDefault(-3)
			.help("specify the maximum subtrajectory size (Any positive | All sizes: -1 | Log: -3 or -4)");

		parser.addArgument("-mnf", "-maxNumberOfFeatures", "--max-number-of-features")
			.type(Integer.class)
			.setDefault(-1)
			.help("specify the maximum number of attributes to combine (Any positive | Explore dim.: -1 | Log: -2 | Other: -3)");

		parser.addArgument("-feature_limit", "--feature-limit")
			.type(Boolean.class)
			.setDefault(false)
			.help("specify whether to limit the number of features");

		parser.addArgument("-q", "-Q", "-QM", "-strQualityMeasure", "--str-quality-measure")
			.type(String.class)
			.setDefault("LSP")
			.choices("LSP", "PROP", "PLSP", "LSPBS", "LSPEA")
			.help("specify the quality measure to evaluate movelet candidates quality");

		parser.addArgument("-ed", "-exploreDimensions", "--explore-dimensions")
			.type(Boolean.class)
			.setDefault(true)
			.help("specify whether to explore dimension combinations");

		parser.addArgument("-samples", "--samples")
			.type(Integer.class)
			.setDefault(1)
			.help("specify the number of samples to evaluate movelet quality");
		parser.addArgument("-sampleSize", "--sample-size")
			.type(Double.class)
			.setDefault(0.5)
			.help("specify the proportion samples to evaluate movelet quality");

		parser.addArgument("-medium", "--medium")
			.type(String.class)
			.choices("none", "minmax", "sd", "interquartil")
			.setDefault("none")
			.help("specify the output medium");
		parser.addArgument("-output", "--output")
			.type(String.class)
			.choices("discrete", "numeric")
			.setDefault("discrete")
			.help("specify the output type");
		parser.addArgument("-outputters", "-outstyle", "--outputters")
			.type(String.class)
			.setDefault("CSV,JSON")
			.help("specify the list of outputter adapters (CSV,JSON)");

		parser.addArgument("-delay-output", "-delay_output", "--delay-output")
			.type(Boolean.class)
			.setDefault(true)
			.help("specify whether to output movelets after all class threads finish");
	
		parser.addArgument("-mpt", "-moveletsPerTrajectory", "-movelets_per_traj", "-movelets_per_trajectory", "--movelets-per-trajectory")
			.type(Integer.class)
			.setDefault(-1)
			.help("specify the fixed filtering number of movelets per trajectory (Any positive | Auto: -1)");

		parser.addArgument("-lp", "-last_pruning", "--last-pruning")
			.type(Boolean.class)
			.setDefault(false)
			.help("specify whether to use last pruning filter. Extra filter for movelets");

		parser.addArgument("-interning", "--interning")
			.type(Boolean.class)
			.setDefault(true)
			.help("specify whether to use a unique copy on memory for all equal trajectory aspects");

		parser.addArgument("-TF", "-fixed_tau", "-fixed-tau", "--fixed-tau")
			.type(Double.class)
			.help("specify the τ parameter as a fixed proportion of movelet candidate quality");
		parser.addArgument("-T", "-TAU", "-tau", "-TR", "-relative_tau", "-relative-tau", "--relative-tau")
			.type(Double.class)
			.help("specify the τ parameter as a relative proportion from the quality value of the best qualified movelet candidate");
		
		parser.addArgument("-bu", "-BU", "-bucket_slice", "-bucket-slice", "--bucket-slice")
			.type(Double.class)
			.help("specify the proportion of movelet candidates to evaluate at each step");
		
		parser.addArgument("-fs", "-FS", "-filter_strategy", "-feature_selecion", "-filter-strategy", "-feature-selecion", "--filter-strategy")
			.type(String.class)
//			.choices("correlation", "gainratio", "infogain", "meanentropy", "stdeviation", "variance") 
			.help("specify the feature selection strategy");
		parser.addArgument("-fe", "-FE", "-feature_extraction_strategy", "-feature-extraction-strategy", "--feature-extraction-strategy")
			.type(String.class)
//			.choices("correlation", "variance") 
			.help("specify the feature extraction strategy");

		parser.addArgument("-k", "-mknn_k", "-mknn-k", "--mknn-k")
			.type(Integer.class)
			.help("specify to use Movelets KNN classifier");

		parser.addArgument("-r", "-seed", "-random_seed", "-random-seed", "--random-seed")
			.type(Integer.class)
			.help("specify the random seed for RandomMovelets and UltraMovelets");

		parser.addArgument("-tc", "-TC", "-time_contract", "-time-contract", "--time-contract")
			.type(String.class)
			.help("specify the time limit to run the method [Use: number + w(eeks), d, h, m, s(econds)]");

		parser.addArgument("-fold", "-resample", "-data_resample", "-data-resample", "--data-resample")
			.type(Integer.class)
			.help("specify the number of runs with random data re-sample");
		parser.addArgument("-resampletrain", "-resample_train", "-resample-train", "--resample-train")
			.type(Double.class)
			.help("specify the Train/Test hold-out proportion for each fold of data re-sample [0.7 is 70% train /30% test hold-out]");
		parser.addArgument("-resampleprop", "-resample_prop", "-resample-prop", "--resample-prop")
			.type(Double.class)
			.help("specify the sample proportion for each fold of data re-sample [data size reduction, 1.0 is 100%]");

		parser.addArgument("-datatrim", "-data_trim", "-data-trim", "--data-trim")
			.type(Double.class)
			.help("specify the proportion of trajectory points to ommit from the trajectory end");

		parser.addArgument("-th_temporal", "-temporal_threshold", "-temporal-threshold", "--temporal-threshold")
			.type(Integer.class)
			.help("specify the temporal threshold for indexed method [UNDER DEVELOPMENT]");
		parser.addArgument("-th_spatial", "-spatial_threshold", "-spatial-threshold", "--spatial-threshold")
			.type(Double.class)
			.help("specify the spatial threshold for indexed method [UNDER DEVELOPMENT]");
		parser.addArgument("-th_numeric", "-numeric_threshold", "-numeric-threshold", "--numeric-threshold")
			.type(Integer.class)
			.help("specify the numeric values threshold for indexed method [UNDER DEVELOPMENT]");
	}

}
