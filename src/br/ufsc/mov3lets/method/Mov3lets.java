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
package br.ufsc.mov3lets.method;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import br.ufsc.mov3lets.method.discovery.DiscoveryAdapter;
import br.ufsc.mov3lets.method.discovery.PrecomputeMoveletsDiscovery;
import br.ufsc.mov3lets.method.loader.LoaderAdapter;
import br.ufsc.mov3lets.method.output.OutputterAdapter;
import br.ufsc.mov3lets.method.qualitymeasure.LeftSidePureCVLigth;
import br.ufsc.mov3lets.method.qualitymeasure.ProportionQualityMeasure;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.utils.ProgressBar;

/**
 * The Class Mov3lets.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 * @param <MO> the generic type
 */
public class Mov3lets<MO> {
	
	/** The descriptor. */
	// CONFIG:
	private Descriptor descriptor = null;
	
	/** The result dir path. */
	private String resultDirPath = "MasterMovelets";
	
	/** The data. */
	// TRAJS:
	private List<MAT<MO>> data = null;
	
	/** The train. */
	private List<MAT<MO>> train = null;
	
	/** The test. */
	private List<MAT<MO>> test = null;
	
	/** The progress bar. */
	public static ProgressBar progressBar = new ProgressBar();

	/**
	 * Instantiates a new mov 3 lets.
	 *
	 * @param descriptorFile the descriptor file
	 * @param params the params
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public Mov3lets(String descriptorFile, HashMap<String, Object> params) 
			throws UnsupportedEncodingException, FileNotFoundException {
		this.descriptor = Descriptor.load(descriptorFile, params);
	}

	/**
	 * Mov 3 lets.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public void mov3lets() throws Exception {
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
		
		List<DiscoveryAdapter<MO>> lsMDs = instantiate(classes, qualityMeasure, progressBar);	
		
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
					Executors.newCachedThreadPool();
					System.gc();
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
		
	}

	/**
	 * Instantiate.
	 *
	 * @param classes the classes
	 * @param qualityMeasure the quality measure
	 * @param progressBar the progress bar
	 * @return the list
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	private List<DiscoveryAdapter<MO>> instantiate(List<MO> classes, QualityMeasure qualityMeasure, ProgressBar progressBar)  
			throws Exception {
		List<DiscoveryAdapter<MO>> lsMDs = new ArrayList<DiscoveryAdapter<MO>>();
		
		/** STEP 2.1: Starts at discovering movelets */
		for (MO myclass : classes) {
			if ( ! Paths.get(resultDirPath, myclass.toString(), "test.csv").toFile().exists() ) {
				List<MAT<MO>> trajsFromClass = train.stream().filter(e-> myclass.equals(e.getMovingObject())).collect(Collectors.toList());

				List<MAT<MO>> sharedQueue = new ArrayList<MAT<MO>>();
				sharedQueue.addAll(trajsFromClass);
				
				DiscoveryAdapter<MO> moveletsDiscovery;
				List<OutputterAdapter<MO>> outs = configOutput(trajsFromClass.size(), myclass);
				
				if (getDescriptor().getParamAsText("version").startsWith("hiper")   ||
					getDescriptor().getParamAsText("version").equals("super-class")) {
					
					moveletsDiscovery = instantiateMoveletsDiscovery(qualityMeasure, trajsFromClass, outs);
					moveletsDiscovery.setQueue(sharedQueue);
					
					// Configure Outputs
					moveletsDiscovery.setOutputers(outs);
					lsMDs.add(moveletsDiscovery);

				} else
					for (MAT<MO> T : trajsFromClass) {
						moveletsDiscovery = instantiateMoveletsDiscovery(qualityMeasure, trajsFromClass, outs, T);
						moveletsDiscovery.setQueue(sharedQueue);
						
						// Configure Outputs
						moveletsDiscovery.setOutputers(outs);
						lsMDs.add(moveletsDiscovery);
					}
				
				// XXX - V2:
//				try {
//				
//					Class cdc = Class.forName("br.ufsc.mov3lets.method.discovery."+getDiscoveryClass());
//					moveletsDiscovery = (DiscoveryAdapter<MO>) 
//							cdc.getConstructor(List.class, List.class, List.class, List.class, QualityMeasure.class, Descriptor.class)
//							.newInstance(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				} catch (ClassNotFoundException e1) {
//
//					Mov3letsUtils.trace("Class not found: "+ "br.ufsc.mov3lets.method.discovery."+getDiscoveryClass() +" [continue with MASTERMovelets]");
//					
//					moveletsDiscovery = new MasterMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				}
//				
//				// Configure Outputs
//				moveletsDiscovery.setOutputers(configOutput());
//				lsMDs.add(moveletsDiscovery);
				
				
				
//				// XXX - Discovery by Class:
//				if (getDescriptor().getFlag("supervised") || getDescriptor().getParamAsText("version").equals("super")) {
//					
//					moveletsDiscovery = new SuperMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				} else if (getDescriptor().getParamAsText("version").equals("hiper")) {
//					
//					moveletsDiscovery = new HiperMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				} else if (getDescriptor().getParamAsText("version").equals("hiper-pvt")) {
//					
//					moveletsDiscovery = new HiperPivotsMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				} else if (getDescriptor().getParamAsText("version").equals("hiper-ce")) {
//					
//					moveletsDiscovery = new HiperCEMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				} else if (getDescriptor().getParamAsText("version").equals("hiper-pvt-ce")) {
//					
//					moveletsDiscovery = new HiperPivotsCEMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				} else if (getDescriptor().getParamAsText("version").equals("hiper-en")) {
//					
//					moveletsDiscovery = new HiperEntropyMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				} else if (getDescriptor().getParamAsText("version").equals("ultra")) {
//					
//					moveletsDiscovery = new UltraMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				} else if (getDescriptor().getFlag("pivots") || getDescriptor().getParamAsText("version").equals("pivots")) {
//					
//					moveletsDiscovery = new PivotsMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				} else if (getDescriptor().getParamAsText("version").equals("1.0")) {
//					
//					moveletsDiscovery = new MoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
////				} else if (getDescriptor().getParamAsText("version").equals("3.0")) {
////					
////					moveletsDiscovery = new PrecomputeMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
////				} else if (getDescriptor().getParamAsText("version").equals("4.0")) {
////					
////					moveletsDiscovery = new ProgressiveMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				} else {
//					
//					moveletsDiscovery = new MemMoveletsDiscovery<MO>(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//				
//				}
				
//				// Configure Outputs
//				moveletsDiscovery.setOutputers(configOutput());
//				lsMDs.add(moveletsDiscovery);
				
			} else {
				int trajsFromClass = (int) train.stream().filter(e-> myclass.equals(e.getMovingObject())).count();
				progressBar.plus(trajsFromClass, "[Class: " + myclass + "]: Movelets previously discovered.");
			}
		}
		
		return lsMDs;
	}

	private DiscoveryAdapter<MO> instantiateMoveletsDiscovery(QualityMeasure qualityMeasure,
			List<MAT<MO>> trajsFromClass, List<OutputterAdapter<MO>> outs, MAT<MO> T)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		DiscoveryAdapter<MO> moveletsDiscovery;
//		try {
			
			Class cdc = Class.forName("br.ufsc.mov3lets.method.discovery."+getDiscoveryClass());
			moveletsDiscovery = (DiscoveryAdapter<MO>) 
					cdc.getConstructor(MAT.class, List.class, List.class, List.class, List.class, QualityMeasure.class, Descriptor.class)
					.newInstance(T, trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
		
//		} catch (ClassNotFoundException e1) {
//
//			Mov3letsUtils.trace("Class not found: "+ "br.ufsc.mov3lets.method.discovery."+getDiscoveryClass() +" [continue with MASTERMovelets]");
//			
//			moveletsDiscovery = new MasterMoveletsDiscovery<MO>(T, trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//		
//		}
		
		return moveletsDiscovery;
	}

	private DiscoveryAdapter<MO> instantiateMoveletsDiscovery(QualityMeasure qualityMeasure,
			List<MAT<MO>> trajsFromClass, List<OutputterAdapter<MO>> outs)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		DiscoveryAdapter<MO> moveletsDiscovery;
			
		Class cdc = Class.forName("br.ufsc.mov3lets.method.discovery."+getDiscoveryClass());
		moveletsDiscovery = (DiscoveryAdapter<MO>) 
				cdc.getConstructor(List.class, List.class, List.class, List.class, QualityMeasure.class, Descriptor.class)
				.newInstance(trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
		
		return moveletsDiscovery;
	}

	/**
	 * Mehod getDiscoveryClass. 
	 * 
	 * @return
	 */
	private String getDiscoveryClass() {
		return Arrays.asList(getDescriptor().getParamAsText("version").split("-"))
				.stream().map(StringUtils::capitalize).collect(Collectors.joining("")) + "MoveletsDiscovery";
	}

	/**
	 * Config output.
	 * @param nT Number of class trajectories.
	 * @param myclass 
	 *
	 * @return the list
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public List<OutputterAdapter<MO>> configOutput(int nT, MO myclass) throws Exception {
		List<OutputterAdapter<MO>> outs = new ArrayList<OutputterAdapter<MO>>();
		
		String[] outputters = getDescriptor().getParamAsText("outputters").split(",");
		
		for (String outx : outputters) {
			OutputterAdapter o = (OutputterAdapter) Class.forName("br.ufsc.mov3lets.method.output."+outx+"Outputter")
				.getDeclaredConstructor(String.class, String.class, Descriptor.class)
				.newInstance(resultDirPath, myclass.toString(), getDescriptor());
			
			o.setDelayCount(nT);
			
			outs.add(o);
		}
		
//		outs.add(new JSONOutputter<MO>(resultDirPath, getDescriptor()));
//		outs.add(new CSVOutputter<MO>(resultDirPath, getDescriptor()));
//		out.add(new CSVOutputter<MO>(resultDirPath, getDescriptor(), false));
		return outs;
	}

	/**
	 * Load train.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void loadTrain() throws Exception {
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

	/**
	 * Load test.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void loadTest() throws Exception {
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
	 * STEP 1.
	 *
	 * @return the loader adapter
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public LoaderAdapter instantiateLoader() throws Exception {

		String cname = getDescriptor().getParamAsText("data_format").toUpperCase();
		
		if (getDescriptor().getFlag("interning")) {
			cname += "Intern";
		}
		
		LoaderAdapter loader = (LoaderAdapter) Class.forName("br.ufsc.mov3lets.method.loader."+cname+"Loader")
				.getDeclaredConstructor().newInstance();
		
		
//		if (getDescriptor().getFlag("indexed")) { // For future implementations
//			data = new IndexedLoaderAdapter<MAT<MO>>().load(file, getDescriptor());
//		} else 
//		if (getDescriptor().getFlag("interning")) {
//
//			if ("CSV".equals(getDescriptor().getParamAsText("data_format")))			
//				loader = new CSVInternLoader<MAT<MO>>();
//			else
//				loader = new ZippedInternLoader<MAT<MO>>(); // DEFAULT
//			
//		} else {
//
//			if ("CSV".equals(getDescriptor().getParamAsText("data_format")))			
//				loader = new CSVLoader<MAT<MO>>();
//			else
//				loader = new ZippedLoader<MAT<MO>>();
//			
//		}
		
		return loader;
	}
	
	/**
	 * STEP 2.
	 *
	 * @return the descriptor
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
	
	/**
	 * Gets the train.
	 *
	 * @return the train
	 */
	public List<MAT<MO>> getTrain() {
		return train;
	}
	
	/**
	 * Sets the train.
	 *
	 * @param train the new train
	 */
	public void setTrain(List<MAT<MO>> train) {
		this.train = train;
	}
	
	/**
	 * Gets the test.
	 *
	 * @return the test
	 */
	public List<MAT<MO>> getTest() {
		return test;
	}
	
	/**
	 * Sets the test.
	 *
	 * @param test the new test
	 */
	public void setTest(List<MAT<MO>> test) {
		this.test = test;
	}
	
	/**
	 * Sets the result dir path.
	 *
	 * @param resultDirPath the resultDirPath to set
	 */
	public void setResultDirPath(String resultDirPath) {
		this.resultDirPath = resultDirPath;
	}
	
}
