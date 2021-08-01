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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import br.ufsc.mov3lets.method.discovery.structures.ClassDiscovery;
import br.ufsc.mov3lets.method.discovery.structures.DiscoveryAdapter;
import br.ufsc.mov3lets.method.discovery.structures.GlobalDiscovery;
import br.ufsc.mov3lets.method.discovery.structures.TimeContract;
import br.ufsc.mov3lets.method.discovery.structures.TrajectoryDiscovery;
import br.ufsc.mov3lets.method.feature.extraction.FeatureExtractor;
import br.ufsc.mov3lets.method.feature.extraction.PointFeature;
import br.ufsc.mov3lets.method.feature.selection.FeatureSelector;
import br.ufsc.mov3lets.method.loader.LoaderAdapter;
import br.ufsc.mov3lets.method.output.CSVIndexOutputter;
import br.ufsc.mov3lets.method.output.MKNMOutputter;
import br.ufsc.mov3lets.method.output.MKNTOutputter;
import br.ufsc.mov3lets.method.output.MSVMOutputter;
import br.ufsc.mov3lets.method.output.OutputterAdapter;
import br.ufsc.mov3lets.method.qualitymeasure.LeftSidePureCVLigth;
import br.ufsc.mov3lets.method.qualitymeasure.LeftSidePureCVLigthBS;
import br.ufsc.mov3lets.method.qualitymeasure.LeftSidePureCVLigthEA;
import br.ufsc.mov3lets.method.qualitymeasure.ProportionQualityMeasure;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.utils.Mov3letsUtils;
import br.ufsc.mov3lets.utils.ProgressBar;
import br.ufsc.mov3lets.utils.SimpleOutput;

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
	public static ProgressBar progressBar = new SimpleOutput();

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
		this.data = Collections.unmodifiableList(
						Stream.concat(train.stream(), test.stream()).collect(Collectors.toList())
					);
		this.train = Collections.unmodifiableList(this.train);
		this.test  = Collections.unmodifiableList(this.test);
		
//		// When using precompute version:
//		if (getDescriptor().getParamAsText("version").equals("3.0"))
//			PrecomputeMoveletsDiscovery.initBaseCases(data,	N_THREADS, getDescriptor());
				
		// STEP 2 - Select Candidates:
//		Mov3letsUtils.getInstance().startTimer("[2.1] >> Extracting Movelets");
		List<MO> classes = train.stream().map(e -> (MO) e.getMovingObject()).distinct().collect(Collectors.toList());
		
//		List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();	
		
		/* Keeping up with Progress output */
		progressBar.setPrefix("[2] >> Movelet Discovery");
		progressBar.setTotal(train.size());
//		progressBar.setInline(false);
		int progress = 0;
		progressBar.update(progress, train.size());
		
		scheduleIfTimeContractable();
		
		List<DiscoveryAdapter<MO>> lsMDs = instantiate(classes, progressBar);	

		if (N_THREADS > 1) {
			ExecutorService executor = (ExecutorService) 
					Executors.newFixedThreadPool(N_THREADS);
			List<Future<Integer>> resultList = new ArrayList<>();

			/** STEP 2.1: Starts at discovering movelets */
			for (DiscoveryAdapter<MO> moveletsDiscovery : lsMDs) {
				moveletsDiscovery.setProgressBar(progressBar);
				resultList.add(executor.submit(moveletsDiscovery));
			}
			lsMDs = null;
			
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
//			for (DiscoveryAdapter<MO> moveletsDiscovery : lsMDs) {
//				moveletsDiscovery.setProgressBar(progressBar);
//				moveletsDiscovery.discover();
////				progressBar.update(progress++, train.size());
////				System.gc();
//			}
			// With the iterator we can remove completed instances from memory:
			Iterator<DiscoveryAdapter<MO>> i = lsMDs.iterator();
			while (i.hasNext()) {
				DiscoveryAdapter<MO> moveletsDiscovery = i.next();
				moveletsDiscovery.setProgressBar(progressBar);
				moveletsDiscovery.discover();
				i.remove();
			}
			
		}
		
		stopIfTimeContractable();
	}

	public void scheduleIfTimeContractable() {
		if (getDescriptor().hasParam("time_contract")) {
			((TimeContract) getDescriptor().getParam("time_contract")).start();
		}
	}

	public void stopIfTimeContractable() {
		if (getDescriptor().hasParam("time_contract")) {
			((TimeContract) getDescriptor().getParam("time_contract")).stop();
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
	private List<DiscoveryAdapter<MO>> instantiate(List<MO> classes, ProgressBar progressBar)  
			throws Exception {
		List<DiscoveryAdapter<MO>> lsMDs = new ArrayList<DiscoveryAdapter<MO>>();
		
		Class cdc = getDiscoveryClass();
//		QualityMeasure qualityMeasure = instatiateQuality();
		
		/** STEP 2.1: Starts at discovering movelets */
		if (GlobalDiscovery.class.equals(getTypeOf(cdc))) { // Special case
			DiscoveryAdapter<MO> moveletsDiscovery = 
//					new IndexedMoveletsDiscovery<MO>(data, train, test, qualityMeasure, descriptor);
					(DiscoveryAdapter<MO>) cdc.getConstructor(List.class, List.class, List.class, QualityMeasure.class, Descriptor.class)
					.newInstance(data, train, test, instatiateQuality(), descriptor);
			
			// TODO Configure Outputs:
			moveletsDiscovery.getOutputers().add(
					new CSVIndexOutputter<MO>(descriptor.getParamAsText("respath"), null, descriptor, false));
			lsMDs.add(moveletsDiscovery);

			return lsMDs;
		}
		
		for (MO myclass : classes) {
			if ( ! Paths.get(resultDirPath, myclass.toString(), "test.csv").toFile().exists() ) {
				List<MAT<MO>> trajsFromClass = train.stream().filter(e-> myclass.equals(e.getMovingObject())).collect(Collectors.toList());

				List<MAT<MO>> sharedQueue = new ArrayList<MAT<MO>>();
				sharedQueue.addAll(trajsFromClass);
				
				DiscoveryAdapter<MO> moveletsDiscovery;
				
				if (ClassDiscovery.class.equals(getTypeOf(cdc))) {
//				if (getDescriptor().getParamAsText("version").startsWith("hiper")   ||
//					getDescriptor().getParamAsText("version").equals("super-class")) {
					
					List<OutputterAdapter<MO,?>> outs = configOutput(1, myclass); // For classes, it calls once.
					
					moveletsDiscovery = instantiateMoveletsDiscovery(cdc, instatiateQuality(), trajsFromClass, outs);
					moveletsDiscovery.setQueue(sharedQueue);
					
					// Configure Outputs
					moveletsDiscovery.setOutputers(outs);
					lsMDs.add(moveletsDiscovery);

				} else {
					List<OutputterAdapter<MO,?>> outs = configOutput(trajsFromClass.size(), myclass);
					
					for (MAT<MO> T : trajsFromClass) {
						moveletsDiscovery = instantiateMoveletsDiscovery(cdc, instatiateQuality(), trajsFromClass, outs, T);
						moveletsDiscovery.setQueue(sharedQueue);
						
						// Configure Outputs
						moveletsDiscovery.setOutputers(outs);
						lsMDs.add(moveletsDiscovery);
					}
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

	protected QualityMeasure instatiateQuality() {
		QualityMeasure qualityMeasure;
		switch (getDescriptor().getParamAsText("str_quality_measure")) {
		case "PROP":
			qualityMeasure = new ProportionQualityMeasure<MO>(this.train,
		    		getDescriptor().getParamAsInt("samples"), 
		    		getDescriptor().getParamAsDouble("sample_size"), 
		    		getDescriptor().getParamAsText("medium"));
			break;
			
		case "LSPBS":
			qualityMeasure = new LeftSidePureCVLigthBS<MO>(this.train, 
		    		getDescriptor().getParamAsInt("samples"), 
		    		getDescriptor().getParamAsDouble("sample_size"), 
		    		getDescriptor().getParamAsText("medium"));
			break;
			
		case "LSPEA":
			qualityMeasure = new LeftSidePureCVLigthEA<MO>(this.train, 
		    		getDescriptor().getParamAsInt("samples"), 
		    		getDescriptor().getParamAsDouble("sample_size"), 
		    		getDescriptor().getParamAsText("medium"));
			break;

		case "LSP":
		default:
			qualityMeasure = new LeftSidePureCVLigth<MO>(this.train, 
		    		getDescriptor().getParamAsInt("samples"), 
		    		getDescriptor().getParamAsDouble("sample_size"), 
		    		getDescriptor().getParamAsText("medium"));
			break;
		}
		return qualityMeasure;
	}

	protected DiscoveryAdapter<MO> instantiateMoveletsDiscovery(Class cdc, QualityMeasure qualityMeasure,
			List<MAT<MO>> trajsFromClass, List<OutputterAdapter<MO,?>> outs, MAT<MO> T)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		DiscoveryAdapter<MO> moveletsDiscovery;
//		try {
		
//			Class cdc = Class.forName("br.ufsc.mov3lets.method.discovery."+getDiscoveryClass());
			moveletsDiscovery = (DiscoveryAdapter<MO>) 
					cdc.getConstructor(MAT.class, List.class, List.class, List.class, List.class, QualityMeasure.class, Descriptor.class)
					.newInstance(T, Collections.unmodifiableList(trajsFromClass), data, train, test, qualityMeasure, getDescriptor());
//					.newInstance(T, trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
		
//		} catch (ClassNotFoundException e1) {
//
//			Mov3letsUtils.trace("Class not found: "+ "br.ufsc.mov3lets.method.discovery."+getDiscoveryClass() +" [continue with MASTERMovelets]");
//			
//			moveletsDiscovery = new MasterMoveletsDiscovery<MO>(T, trajsFromClass, data, train, test, qualityMeasure, getDescriptor());
//		
//		}
		
		return moveletsDiscovery;
	}

	protected DiscoveryAdapter<MO> instantiateMoveletsDiscovery(Class cdc, QualityMeasure qualityMeasure,
			List<MAT<MO>> trajsFromClass, List<OutputterAdapter<MO,?>> outs)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		DiscoveryAdapter<MO> moveletsDiscovery;
			
//		Class cdc = Class.forName("br.ufsc.mov3lets.method.discovery."+getDiscoveryClass();
		moveletsDiscovery = (DiscoveryAdapter<MO>) 
				cdc.getConstructor(List.class, List.class, List.class, List.class, QualityMeasure.class, Descriptor.class)
				.newInstance(Collections.unmodifiableList(trajsFromClass), data, train, test, qualityMeasure, getDescriptor());
		
		return moveletsDiscovery;
	}

	/**
	 * Mehod getDiscoveryClass. 
	 * 
	 * @return
	 * @throws Exception 
	 */
	protected Class<?> getDiscoveryClass() throws Exception {
		return Class.forName("br.ufsc.mov3lets.method.discovery." + 
				Arrays.asList(getDescriptor().getParamAsText("version").split("-"))
				.stream().map(StringUtils::capitalize).collect(Collectors.joining("")) + "MoveletsDiscovery");
	}
	
	protected static  Class<?> getTypeOf(Class<?> clazz) {
        if (clazz != null) {
        	if (Arrays.asList(clazz.getInterfaces()).contains(TrajectoryDiscovery.class)) {
        		return TrajectoryDiscovery.class;
        	} else if (Arrays.asList(clazz.getInterfaces()).contains(ClassDiscovery.class)) {
        		return ClassDiscovery.class;
        	} else { 
	            clazz = clazz.getSuperclass();
	            return getTypeOf(clazz);
        	}
        } else {
            return null;
        }
	}

	/**
	 * Config output.
	 * @param n Number of class trajectories.
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
	public List<OutputterAdapter<MO,?>> configOutput(int n, MO myclass) throws Exception {
		List<OutputterAdapter<MO,?>> outs = new ArrayList<OutputterAdapter<MO,?>>();
		
		String[] outputters = getDescriptor().getParamAsText("outputters").split(",");
		
		for (String outx : outputters) {
			OutputterAdapter o = instantiateOutputter(myclass, outx);
			
			o.setDelay(n);
			
			outs.add(o);
		}
		
//		outs.add(new JSONOutputter<MO>(resultDirPath, getDescriptor()));
//		outs.add(new CSVOutputter<MO>(resultDirPath, getDescriptor()));
//		out.add(new CSVOutputter<MO>(resultDirPath, getDescriptor(), false));
		return outs;
	}

	private OutputterAdapter instantiateOutputter(MO myclass, String outx) throws Exception {
		
		OutputterAdapter o;
		
		// For Singleton outputters:
		if ("MSVM".equalsIgnoreCase(outx)) {
			
			o = MSVMOutputter.getInstance(resultDirPath, getDescriptor());
			
		} else if ("MKNM".equalsIgnoreCase(outx)) {
			
			o = MKNMOutputter.getInstance(resultDirPath, getDescriptor());
			
		} else if ("MKNT".equalsIgnoreCase(outx)) {
			
			o = MKNTOutputter.getInstance(resultDirPath, getDescriptor());
			
		} else {
		
			o = (OutputterAdapter) Class.forName("br.ufsc.mov3lets.method.output."+outx+"Outputter")
				.getDeclaredConstructor(String.class, String.class, Descriptor.class)
				.newInstance(resultDirPath, String.valueOf(myclass), getDescriptor());
		}
			
		return o;
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

	public void preProcessing() throws Exception {
		ArrayList<AttributeDescriptor> attributes = new ArrayList<AttributeDescriptor>(getDescriptor().getAttributes());
//		ArrayList<PointFeature> features = null;

		List<AttributeDescriptor> newAttr = new ArrayList<AttributeDescriptor>();
		
		// Extract Features
		if (getDescriptor().getFeatures() != null && !getDescriptor().getFeatures().isEmpty()) {
			Mov3letsUtils.getInstance().startTimer("[1.1] >> Extract Features");
			newAttr.addAll( pointFeatureExtraction(attributes) );
			Mov3letsUtils.getInstance().stopTimer( "[1.1] >> Extract Features");
		}

		// Feature Extraction (Automatic)
		if (getDescriptor().hasParam("feature_extraction_strategy")) {
			Mov3letsUtils.getInstance().startTimer("[1.2] >> Automatic Feature Extraction");
			newAttr.addAll( featureExtraction(attributes) );
			Mov3letsUtils.getInstance().stopTimer( "[1.2] >> Automatic Feature Extraction");
		}
		
		// Feature Selection
		if (getDescriptor().hasParam("filter_strategy")) {
			Mov3letsUtils.getInstance().startTimer("[1.3] >> Feature Selection");
			featureSelection(attributes);
			Mov3letsUtils.getInstance().stopTimer( "[1.3] >> Feature Selection");
		}
		
		// Update Attributes at the end
		if (!newAttr.isEmpty())
			getDescriptor().getAttributes().addAll(newAttr);
		
	}
	
	public List<AttributeDescriptor> pointFeatureExtraction(ArrayList<AttributeDescriptor> attributes) throws Exception {
		
		ArrayList<PointFeature> features = instantiatePointFeatures();	
		extractPointFeatures(features, getTrain());
		extractPointFeatures(features, getTest());
		
		List<AttributeDescriptor> newAttr = new ArrayList<AttributeDescriptor>();
		// Add Features to attributes:
		for (int i = 0; i < features.size(); i++) {
			newAttr.add(getDescriptor().instantiateFeature(
					getDescriptor().getFeatures().get(i), features.get(i)));			
		}
		
		return newAttr;
		
	}

	public ArrayList<PointFeature> instantiatePointFeatures() throws Exception {
		ArrayList<PointFeature> features = new ArrayList<PointFeature>();
		
		// Load Features:
		for (AttributeDescriptor attr : getDescriptor().getFeatures()) {
			String className = attr.getType();
			
			className = "br.ufsc.mov3lets.method.feature.extraction.point." 
					+ className.substring(0, 1).toUpperCase() + className.substring(1).toLowerCase();
			className += "PointFeature";
			
			// Throw exception if class not found
			PointFeature feat = (PointFeature) Class.forName(className).getConstructor().newInstance();
			feat.init(getDescriptor(), attr);
						
			features.add(feat);
		}
		
		return features;
	}

	/**
	 * Load test.
	 * @param trajectories 
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void extractPointFeatures(List<PointFeature> features, List<MAT<MO>> trajectories) throws Exception {
	
		// Fill trajectories:
		for (PointFeature feat : features) {
			for (MAT<MO> T : trajectories) {
				feat.fillPoints(T);
			}
		}
		
	}

	public List<AttributeDescriptor> featureExtraction(ArrayList<AttributeDescriptor> attributes) throws Exception {
		
		// Load Instance:
		String className = getDescriptor().getParamAsText("feature_extraction_strategy");
		
		className = "br.ufsc.mov3lets.method.feature.extraction." 
				+ className.substring(0, 1).toUpperCase() + className.substring(1).toLowerCase();
		className += "FeatureExtractor";
			
		// Throw exception if class not found
		FeatureExtractor feat = (FeatureExtractor) Class.forName(className).getConstructor().newInstance();
		List<AttributeDescriptor> newAttr = feat.updateTrajectories(
				getDescriptor(), attributes, getTrain(), getTest());
						
		// Update features references:
		if (getDescriptor().getFeatures() == null)
			getDescriptor().setFeatures( newAttr );
		else
			getDescriptor().getFeatures().addAll(newAttr);
		
		return newAttr;
	}

	public List<AttributeDescriptor> featureSelection(ArrayList<AttributeDescriptor> attributes) throws Exception {
		
		// Load Instance:
		String className = getDescriptor().getParamAsText("filter_strategy");
		
		className = "br.ufsc.mov3lets.method.feature.selection." 
				+ className.substring(0, 1).toUpperCase() + className.substring(1).toLowerCase();
		className += "FeatureSelector";
			
		// Throw exception if class not found
		FeatureSelector feat = (FeatureSelector) Class.forName(className).getConstructor().newInstance();
		List<AttributeDescriptor> unusedAttr = feat.updateTrajectories(
				getDescriptor(), attributes, getTrain(), getTest());
						
		// Update attributes:
		getDescriptor().getAttributes().removeAll(unusedAttr);
		
		return unusedAttr;
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
