/**
 * 
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

import br.ufsc.mov3lets.method.Similarity;
import br.ufsc.mov3lets.method.loader.LoaderAdapter;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.utils.Mov3letsUtils;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * @author tarlisportela
 *
 */
public class SimilarityRun extends Mov3letsRun {
	
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
		Similarity<String> mov;
		if (!params.containsKey("descfile")) {
			mov = new Similarity<String>(params);
			
			if (!Paths.get(LoaderAdapter.getFileName("train.mat", mov.getDescriptor())).toFile().exists()) {
				showUsage(params, "-descfile\tDescription file must be set OR must provide .mat files at -curpath!");
				return;
			}

		} else {
			try {
				mov = new Similarity<String>(params.get("descfile").toString(), params);
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				showUsage(params, "-descfile\tDescription file not found: " + e.getMessage());
	//			e.printStackTrace();
				return;
			}
		}
		
		// STEP 1.3 - Input:
		Mov3letsUtils.getInstance().startTimer("[1] >> Load Input");

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
//		mov.getDescriptor().setParam("result_dir_path", Paths.get(mov.getDescriptor().getParamAsText("respath")).toString());
//		mov.setResultDirPath(mov.getDescriptor().getParamAsText("result_dir_path"));
		
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
			Mov3letsUtils.getInstance().startTimer("[3] >> Processing time");
			mov.similarity();
			Mov3letsUtils.trace("");
			Mov3letsUtils.getInstance().stopTimer("[3] >> Processing time");
		} catch (Exception e) {
			error(e);
		}
		
		// End Date:
		Mov3letsUtils.trace(new Date().toString());

	}

	/**
	 * Default params.
	 *
	 * @return the hash map
	 */
	public static HashMap<String, Object> defaultParams() {
		
		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put("nthreads",					 1);
		params.put("min_size",					 1);
		params.put("max_size",					 -1); // TODO unlimited maxSize
		params.put("outputters",				 "CSV"); // OUTPUTTERs Styles
		params.put("interning",  				 true);
		params.put("verbose",				 	 true);
		params.put("compute_distances",			 false);
		params.put("similarity",				 "MUITAS");
		
		return params;
	}
	
	/**
	 * Configure.
	 *
	 * @param args the args
	 * @return the hash map
	 */
	public static Map<String, Object> configure(String[] args) {
		
		ArgumentParser parser = ArgumentParsers.newFor("Trajectory Similarity").build()
                .defaultHelp(true)
                .description("Compute distances/similarities of trajectories.");
		
        setArguments(parser);
        
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        
        return ns.getAttrs();
		
//		HashMap<String, Object> params = defaultParams();

//		for (int i = 0; i < args.length; i = i + 2) {
//			String key = args[i];
//			String value = args[i + 1];
//			switch (key) {
//			case "-curpath":
//				params.put("curpath", value);
//				break;
//			case "-respath":
//				params.put("respath", value);
//				break;
//			case "-descfile":
//				params.put("descfile", value);
//				break;
//			case "-inprefix":
//			case "-input_file_prefix":
//				params.put("input_file_prefix", value);
//				break;
//			case "-nt":
//			case "-nthreads":
//				int N_THREADS = Integer.valueOf(value);
//				N_THREADS = N_THREADS == 0? 1 : N_THREADS;
//				params.put("nthreads", N_THREADS);
//				break;
//			case "-ms":
//			case "-minSize":
//			case "-min_size":
//				params.put("min_size", Integer.valueOf(value));
//				break;
//			case "-Ms":
//			case "-maxSize":
//			case "-max_size":
//				params.put("max_size", Integer.valueOf(value));
//				break;
//			case "-ed":
//			case "-exploreDimensions":
//			case "-explore_dimensions":
//				params.put("explore_dimensions", Boolean.valueOf(value));
//				break;					
//			case "-mnf":			
//			case "-maxNumberOfFeatures":			
//			case "-max_number_of_features":
//				params.put("max_number_of_features", Integer.valueOf(value));
//				if (Integer.valueOf(value) < -2)
//					params.put("feature_limit", true);
//				break;	
//			case "-outstyle":
//			case "-outputters":
//				params.put("outputters", value);			
//				break;	
//			case "-delay_output":
//				params.put("delay_output", Boolean.valueOf(value));
//				break;	
//			case "-interning":
//				params.put("interning", Boolean.valueOf(value));
//				break;
//			case "-v":
//			case "-verbose":
//				params.put("verbose", Boolean.valueOf(value));
//				break;
//			case "-filter":
//			case "-filter_strategy":
//			case "-feature_selecion":
//			case "-FS":
//			case "-fs":
//				params.put("filter_strategy", value);
//				break;
//			case "-feature_extraction_strategy":
//			case "-feature_extraction":
//			case "-FE":
//			case "-fe":
//				params.put("feature_extraction_strategy", value);
//				break;
//			case "-random_seed":
//			case "-seed":
//			case "-r":
//				params.put("random_seed", Integer.valueOf(value));		
//				break;
//			case "-TC":
//			case "-tc":
//			case "-time_contract":
//				params.put("time_contract", new TimeContract(value));	
//				break;
//			case "-fold":
//			case "-resample":
//			case "-data_resample":
//				params.put("data_resample", Integer.valueOf(value));	// 10
//				break;
//			case "-resampleprop":
//			case "-resample_prop":
//				params.put("resample_prop", Double.valueOf(value));		// 1.0 - 100%
//				break;
//			case "-resampletrain":
//			case "-resample_train":
//				params.put("resample_train", Double.valueOf(value));	// 0.7 - 70%/30% hold-out
//				break;
//			case "-datatrim":
//			case "-data_trim":
//				params.put("data_trim", Double.valueOf(value));	
//				break;
//			case "-d":
//			case "-compute-distances":
//				params.put("compute_distances", Boolean.valueOf(value));
//				break;
//			case "-s":
//			case "-similarity":
//				params.put("similarity", value);
//				break;
//			default:
//				System.err.println("Parâmetro " + key + " inválido.");
//				System.exit(1);
//			}
//		}
//		
//		if (params.containsKey("max_number_of_features") && (int) params.get("max_number_of_features") == -1)
//			params.put("explore_dimensions", true);
//
//		return params;
	}

	public static void setArguments(ArgumentParser parser) {
		Mov3letsRun.setArguments(parser);
        
        parser.addArgument("-s", "--similarity")
        		.setDefault("MUITAS")
                .help("specify the similarity measures to use. Comma separated: \"LCSS,EDR,MSM,MUITAS\".");

        parser.addArgument("-d", "--compute-distances")
				.type(Boolean.class)
		        .choices(true, false)
		        .setDefault(false)
		        .help("specify whether to compute distances (true) or similarities (false)");
        
	}


}
