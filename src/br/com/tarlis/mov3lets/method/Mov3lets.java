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
package br.com.tarlis.mov3lets.method;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.tarlis.mov3lets.method.discovery.DiscoveryAdapter;
import br.com.tarlis.mov3lets.method.discovery.HiperMoveletsDiscovery;
import br.com.tarlis.mov3lets.method.discovery.HiperPivotsMoveletsDiscovery;
import br.com.tarlis.mov3lets.method.discovery.HiperenMoveletsDiscovery;
import br.com.tarlis.mov3lets.method.discovery.MemMoveletsDiscovery;
import br.com.tarlis.mov3lets.method.discovery.MoveletsDiscovery;
import br.com.tarlis.mov3lets.method.discovery.PivotsMoveletsDiscovery;
import br.com.tarlis.mov3lets.method.discovery.PrecomputeMoveletsDiscovery;
import br.com.tarlis.mov3lets.method.discovery.ProgressiveMoveletsDiscovery;
import br.com.tarlis.mov3lets.method.discovery.SuperMoveletsDiscovery;
import br.com.tarlis.mov3lets.method.loader.CSVInternLoader;
import br.com.tarlis.mov3lets.method.loader.CSVLoader;
import br.com.tarlis.mov3lets.method.loader.LoaderAdapter;
import br.com.tarlis.mov3lets.method.loader.ZippedInternLoader;
import br.com.tarlis.mov3lets.method.loader.ZippedLoader;
import br.com.tarlis.mov3lets.method.output.CSVOutputter;
import br.com.tarlis.mov3lets.method.output.JSONOutputter;
import br.com.tarlis.mov3lets.method.output.OutputterAdapter;
import br.com.tarlis.mov3lets.method.qualitymeasure.LeftSidePureCVLigth;
import br.com.tarlis.mov3lets.method.qualitymeasure.ProportionQualityMeasure;
import br.com.tarlis.mov3lets.method.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;
import br.com.tarlis.mov3lets.utils.ProgressBar;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Mov3lets<MO> {
	
	// CONFIG:
	private Descriptor descriptor = null;
	private String resultDirPath = "MasterMovelets";
	
	// TRAJS:
	private List<MAT<MO>> data = null;
	private List<MAT<MO>> train = null;
	private List<MAT<MO>> test = null;
	
	public static ProgressBar progressBar = new ProgressBar();

	/**
	 * @param params 
	 * @param descFile
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public Mov3lets(String descriptorFile, HashMap<String, Object> params) throws UnsupportedEncodingException, FileNotFoundException {
		this.descriptor = Descriptor.load(descriptorFile, params);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public void mov3lets() {
		int N_THREADS = getDescriptor().getParamAsInt("nthreads");

		// STEP 1 - Load Trajectories: is done before this method starts.
		this.data = Stream.concat(train.stream(), test.stream()).collect(Collectors.toList());
		
		// When using precompute version:
		if (getDescriptor().getParamAsText("version").equals("3.0"))
			PrecomputeMoveletsDiscovery.initBaseCases(data,	N_THREADS, getDescriptor());
				
		// STEP 2 - Select Candidates:
//		Mov3letsUtils.getInstance().startTimer("[2.1] >> Extracting Movelets");
		List<MO> classes = train.stream().map(e -> (MO) e.getMovingObject()).distinct().collect(Collectors.toList());
		
		QualityMeasure qualityMeasure;
		if (getDescriptor().getParamAsText("str_quality_measure").equalsIgnoreCase("LSP"))
			qualityMeasure = new LeftSidePureCVLigth<MO>(train, 
									    		getDescriptor().getParamAsInt("samples"), 
									    		getDescriptor().getParamAsDouble("sample_size"), 
									    		getDescriptor().getParamAsText("medium"));
		else 
			qualityMeasure = new ProportionQualityMeasure<MO>(this.train,
									    		getDescriptor().getParamAsInt("samples"), 
									    		getDescriptor().getParamAsDouble("sample_size"), 
									    		getDescriptor().getParamAsText("medium"));

//		List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();	
		
		/* Keeping up with Progress output */
		progressBar.setPrefix("[2] >> Movelet Discovery");
		progressBar.setTotal(train.size());
//		progressBar.setInline(false);
		int progress = 0;
		progressBar.update(progress, train.size());
		
		List<DiscoveryAdapter<MO>> lsMDs = instantiate(classes, null, qualityMeasure, progressBar);	
		
		if (N_THREADS > 1) {
			ExecutorService executor = (ExecutorService) 
					Executors.newFixedThreadPool(N_THREADS);
			List<Future<Integer>> resultList = new ArrayList<>();
			

			/** STEP 2.1: Starts at discovering movelets */
			for (DiscoveryAdapter<MO> moveletsDiscovery : lsMDs) {
				moveletsDiscovery.setProgressBar(progressBar);
				resultList.add(executor.submit(moveletsDiscovery));
			}
			
			/** STEP 2.1: --------------------------------- */
			for (Future<Integer> future : resultList) {
				try {
					future.get();
//					progressBar.update(progress++, train.size());
					System.gc();
					Executors.newCachedThreadPool();
				} catch (InterruptedException | ExecutionException e) {
					e.getCause().printStackTrace();
				}
			}
			executor.shutdown();
		} else {
			
			/** STEP 2.1: Starts at discovering movelets */
			for (DiscoveryAdapter<MO> moveletsDiscovery : lsMDs) {
				moveletsDiscovery.setProgressBar(progressBar);
				moveletsDiscovery.discover();
//				progressBar.update(progress++, train.size());
				System.gc();
			}
			
		}
		
		
		
//		List<Subtrajectory> candidates = 
//		selectCandidates(train, test, new LeftSidePureCVLigth(train, 
//	    		getDescriptor().getParamAsInt("samples"), 
//	    		getDescriptor().getParamAsDouble("sampleSize"), 
//	    		getDescriptor().getParamAsText("medium")));
//		Mov3letsUtils.getInstance().startTimer("[2.1] >> Extracting Movelets");
	}

	private List<DiscoveryAdapter<MO>> instantiate(List<MO> classes, List<Subtrajectory> candidates, QualityMeasure qualityMeasure, ProgressBar progressBar) {
		List<DiscoveryAdapter<MO>> lsMDs = new ArrayList<DiscoveryAdapter<MO>>();
		
		/** STEP 2.1: Starts at discovering movelets */
		for (MO myclass : classes) {
			if ( ! Paths.get(resultDirPath, myclass.toString(), "test.csv").toFile().exists() ) {
				List<MAT<MO>> trajsFromClass = train.stream().filter(e-> myclass.equals(e.getMovingObject())).collect(Collectors.toList());

				DiscoveryAdapter<MO> moveletsDiscovery;
				
				// Discovery by Class:
				if (getDescriptor().getFlag("supervised") || getDescriptor().getParamAsText("version").equals("super")) {
					
					moveletsDiscovery = new SuperMoveletsDiscovery<MO>(trajsFromClass, data, train, test, candidates, qualityMeasure, getDescriptor());
				
				} else if (getDescriptor().getParamAsText("version").equals("hiper")) {
					
					moveletsDiscovery = new HiperMoveletsDiscovery<MO>(trajsFromClass, data, train, test, candidates, qualityMeasure, getDescriptor());
				
				} else if (getDescriptor().getParamAsText("version").equals("hpivots")) {
					
					moveletsDiscovery = new HiperPivotsMoveletsDiscovery<MO>(trajsFromClass, data, train, test, candidates, qualityMeasure, getDescriptor());
				
				} else if (getDescriptor().getParamAsText("version").equals("hiperen")) {
					
					moveletsDiscovery = new HiperenMoveletsDiscovery<MO>(trajsFromClass, data, train, test, candidates, qualityMeasure, getDescriptor());
				
				} else if (getDescriptor().getFlag("pivots") || getDescriptor().getParamAsText("version").equals("pivots")) {
					
					moveletsDiscovery = new PivotsMoveletsDiscovery<MO>(trajsFromClass, data, train, test, candidates, qualityMeasure, getDescriptor());
				
				} else if (getDescriptor().getParamAsText("version").equals("1.0")) {
					
					moveletsDiscovery = new MoveletsDiscovery<MO>(trajsFromClass, data, train, test, candidates, qualityMeasure, getDescriptor());
				
				} else if (getDescriptor().getParamAsText("version").equals("3.0")) {
					
					moveletsDiscovery = new PrecomputeMoveletsDiscovery<MO>(trajsFromClass, data, train, test, candidates, qualityMeasure, getDescriptor());
				
				} else if (getDescriptor().getParamAsText("version").equals("4.0")) {
					
					moveletsDiscovery = new ProgressiveMoveletsDiscovery<MO>(trajsFromClass, data, train, test, candidates, qualityMeasure, getDescriptor());
				
				} else {
					
					moveletsDiscovery = new MemMoveletsDiscovery<MO>(trajsFromClass, data, train, test, candidates, qualityMeasure, getDescriptor());
				
				}
				
				// Configure Outputs
				moveletsDiscovery.setOutputers(configOutput());
				lsMDs.add(moveletsDiscovery);
				
			} else {
				int trajsFromClass = (int) train.stream().filter(e-> myclass.equals(e.getMovingObject())).count();
				progressBar.plus(trajsFromClass, "[Class: " + myclass + "]: Movelets previously discovered.");
			}
		}
		
		return lsMDs;
	}

	public List<OutputterAdapter<MO>> configOutput() {
		List<OutputterAdapter<MO>> outs = new ArrayList<OutputterAdapter<MO>>();
		outs.add(new JSONOutputter<MO>(resultDirPath, getDescriptor()));
		outs.add(new CSVOutputter<MO>(resultDirPath, getDescriptor()));
//		out.add(new CSVOutputter<MO>(resultDirPath, getDescriptor(), false));
		return outs;
	}

	public void loadTrain() throws IOException {
		LoaderAdapter loader = instantiateLoader();
		
		if (getDescriptor().getInput() != null && getDescriptor().getInput().getTrain() != null) {
			setTrain(new ArrayList<MAT<MO>>());
			for (String file : getDescriptor().getInput().getTrain()) {
				getTrain().addAll(loader.load(file, getDescriptor()));
			}
		} else {
			setTrain(loader.load("train", getDescriptor()));
		}
	}

	public void loadTest() throws IOException {
		LoaderAdapter loader = instantiateLoader();
		
		if (getDescriptor().getInput() != null && getDescriptor().getInput().getTest() != null) {
			setTest(new ArrayList<MAT<MO>>());
			for (String file : getDescriptor().getInput().getTest()) {
				getTest().addAll(loader.load(file, getDescriptor()));
			}
		} else {
			setTest(loader.load("test", getDescriptor()));
		}
	}

	/**
	 * STEP 1
	 * @return
	 * @throws IOException
	 */
	public LoaderAdapter instantiateLoader() {
		LoaderAdapter loader;
//		if (getDescriptor().getFlag("indexed")) { // For future implementations
//			data = new IndexedLoaderAdapter<MAT<MO>>().load(file, getDescriptor());
//		} else 
		if (getDescriptor().getFlag("interning")) {

			if ("CSV".equals(getDescriptor().getParamAsText("data_format")))			
				loader = new CSVInternLoader<MAT<MO>>();
			else
				loader = new ZippedInternLoader<MAT<MO>>(); // DEFAULT
			
		} else {

			if ("CSV".equals(getDescriptor().getParamAsText("data_format")))			
				loader = new CSVLoader<MAT<MO>>();
			else
				loader = new ZippedLoader<MAT<MO>>();
			
		}
		
		return loader;
	}
	
	/**
	 * STEP 2
	 * @param train 
	 * @param test 
	 * @param qualityMeasure 
	 */
//	private List<Subtrajectory> selectCandidates(List<MAT<MO>> train, List<MAT<MO>> test, QualityMeasure qualityMeasure) {
//		List<MO> classes = train.stream().map(e -> (MO) e.getMovingObject()).distinct().collect(Collectors.toList());
//		
//		List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();
//		
//		int N_THREADS = getDescriptor().getParamAsInt("nthreads");
//		
////		PrecomputeMoveletsDiscovery.initBaseCases(
////				Stream.concat(train.stream(), test.stream()).collect(Collectors.toList()), 
////				N_THREADS, getDescriptor());
//				
//		ExecutorService executor = (ExecutorService) 
//				Executors.newFixedThreadPool(N_THREADS);
//		List<Future<Integer>> resultList = new ArrayList<>();
//		
//		/* Keeping up with Progress output */
//		ProgressBar progressBar = new ProgressBar("[2.2] >> Movelet Discovery", train.size());
////		progressBar.setInline(false);
//		int progress = 0;
//		progressBar.update(progress, train.size());
//		
//		/** STEP 2.1: Starts at discovering movelets */
//		for (MO myclass : classes) {
//			if ( ! Paths.get(resultDirPath, myclass.toString(), "test.csv").toFile().exists() ) {
//				List<MAT<MO>> trajsFromClass = train.stream().filter(e-> myclass.equals(e.getMovingObject())).collect(Collectors.toList());
//				
//				for (MAT<MO> trajectory : trajsFromClass) {
//					DiscoveryAdapter<MO> moveletsDiscovery;
//					if (getDescriptor().getFlag("pivots")) {
//						moveletsDiscovery = new PivotsMoveletsDiscovery<MO>(trajectory, train, test, candidates, qualityMeasure, 
//								getDescriptor());
//					} else if (getDescriptor().getFlag("supervised")) {
//						moveletsDiscovery = new SupervisedMoveletsDiscovery<MO>(trajectory, train, test, candidates, qualityMeasure, 
//								getDescriptor());
//					} else {
////						moveletsDiscovery = new BaseCaseMoveletsDiscovery<MO>(trajectory, train, test, candidates, qualityMeasure, getDescriptor());
//						moveletsDiscovery = new MoveletsDiscovery<MO>(trajectory, train, test, candidates, qualityMeasure, getDescriptor());
////						moveletsDiscovery = new PrecomputeMoveletsDiscovery<MO>(trajectory, train, test, candidates, qualityMeasure, getDescriptor());
//					}
//					
//					/** Configuring outputs: */
//					moveletsDiscovery.setProgressBar(progressBar);
//					moveletsDiscovery.setOutputers(new ArrayList<OutputterAdapter<MO>>());
//					moveletsDiscovery.getOutputers().add(new JSONOutputter<MO>(resultDirPath, getDescriptor()));
//					moveletsDiscovery.getOutputers().add(new CSVOutputter<MO>(resultDirPath, getDescriptor()));
////					moveletsDiscovery.getOutputers().add(new CSVOutputter<MO>(resultDirPath, getDescriptor(), false));
//
////					progressBar.update(progress++, train.size());
//					resultList.add(executor.submit(moveletsDiscovery));
////					moveletsDiscovery.discover();
//				}
//			} else {
//				Mov3letsUtils.trace("\t[Class: " + myclass + "] >> Movelets previously discovered.");
//			}
//		}
//		/** STEP 2.1: --------------------------------- */
//		for (Future<Integer> future : resultList) {
//			try {
//				future.get();
//				progressBar.update(progress++, train.size());
//				System.gc();
//				Executors.newCachedThreadPool();
//			} catch (InterruptedException | ExecutionException e) {
//				e.getCause().printStackTrace();
//			}
//		}
//		executor.shutdown();
//		
//		return candidates;
//	}

	/**
	 * @return the descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	public List<MAT<MO>> getTrain() {
		return train;
	}
	
	public void setTrain(List<MAT<MO>> train) {
		this.train = train;
	}
	
	public List<MAT<MO>> getTest() {
		return test;
	}
	
	public void setTest(List<MAT<MO>> test) {
		this.test = test;
	}
	
	/**
	 * @param resultDirPath the resultDirPath to set
	 */
	public void setResultDirPath(String resultDirPath) {
		this.resultDirPath = resultDirPath;
	}
	
}
