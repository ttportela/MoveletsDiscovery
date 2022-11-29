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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import br.ufsc.mov3lets.method.discovery.structures.ClassDiscovery;
import br.ufsc.mov3lets.method.discovery.structures.DiscoveryAdapter;
import br.ufsc.mov3lets.method.discovery.structures.GlobalDiscovery;
import br.ufsc.mov3lets.method.discovery.structures.Lock;
import br.ufsc.mov3lets.method.discovery.structures.TimeContract;
import br.ufsc.mov3lets.method.discovery.structures.TrajectoryDiscovery;
import br.ufsc.mov3lets.method.feature.extraction.FeatureExtractor;
import br.ufsc.mov3lets.method.feature.extraction.PointFeature;
import br.ufsc.mov3lets.method.feature.selection.FeatureSelector;
import br.ufsc.mov3lets.method.loader.LoaderAdapter;
import br.ufsc.mov3lets.method.loader.MATLoader;
import br.ufsc.mov3lets.method.output.OutputterAdapter;
import br.ufsc.mov3lets.method.output.classifiers.MKNMOutputter;
import br.ufsc.mov3lets.method.output.classifiers.MKNTOutputter;
import br.ufsc.mov3lets.method.output.classifiers.MSVMOutputter;
import br.ufsc.mov3lets.method.qualitymeasure.FrequentQualityMeasure;
import br.ufsc.mov3lets.method.qualitymeasure.LeftSidePureCVLigth;
import br.ufsc.mov3lets.method.qualitymeasure.LeftSidePureCVLigthBS;
import br.ufsc.mov3lets.method.qualitymeasure.LeftSidePureCVLigthEA;
import br.ufsc.mov3lets.method.qualitymeasure.PLSPQualityMeasure;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.sample.RandomSampler;
import br.ufsc.mov3lets.method.sample.Sampler;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.utils.Mov3letsUtils;
import br.ufsc.mov3lets.utils.TableList;
import br.ufsc.mov3lets.utils.log.FileLogger;
import br.ufsc.mov3lets.utils.log.LoggerAdapter;
import br.ufsc.mov3lets.utils.log.ProgressBar;
import br.ufsc.mov3lets.utils.log.SimpleOutput;

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

	public Mov3lets(HashMap<String, Object> params) {
		this.descriptor = new Descriptor();

        descriptor.setParams(params);
		descriptor.setParam("data_format", "MAT");
		descriptor.setFlag("interning", true);
		
		descriptor.setAttributes(new ArrayList<AttributeDescriptor>());
   
//        descriptor.configure();
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
		
		Class cdc = getDiscoveryClass();
		List<DiscoveryAdapter<MO>> lsMDs = instantiate(cdc, classes, progressBar);	

		if (N_THREADS > 1 && !GlobalDiscovery.class.equals(getTypeOf(cdc))) {
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
			lsMDs = null;
			while (i.hasNext()) {
				DiscoveryAdapter<MO> moveletsDiscovery = i.next();
				moveletsDiscovery.setProgressBar(progressBar);
				moveletsDiscovery.discover();
				i.remove();
				System.gc();
			}
			
		}
		
		stopIfTimeContractable();
	}

	/**
	 * Mov 3 lets with samples.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public void sampler_mov3lets(int nsamples) throws Exception {
		
		LoggerAdapter defaultLogger = Mov3letsUtils.getInstance().getLogger();
		String path = Paths.get(descriptor.getParamAsText("respath")).getParent().toString();
		String folder = Paths.get(descriptor.getParamAsText("respath")).getFileName().toString();
		
		// STEP 1 - SAMPLES:
		Sampler<MO> sampler = new RandomSampler<MO>(train, test, nsamples);
		List<MAT<MO>>[] inst;
		while ((inst = sampler.nextSample()) != null) {
			// STEP 2 - Configure path and output:
			File fl = Paths.get(path, "run"+sampler.getSample(), folder, folder+".txt").toFile();
			if (fl.exists()) {
				defaultLogger.trace("[RE-SAMPLING] >> Movelets Discovery on Sample #" + sampler.getSample() + " done.");
				continue;
			}
			defaultLogger.trace("[RE-SAMPLING] >> Running Movelets Discovery on Sample #" + sampler.getSample());
			FileLogger logger = new FileLogger(fl);
			Mov3letsUtils.getInstance().setLogger(logger);

			getDescriptor().setParam("respath", Paths.get(path, "run"+sampler.getSample(), folder).toString());
			resultDirPath = configRespath(getDescriptor());
			getDescriptor().setParam("result_dir_path", resultDirPath);
			Mov3letsUtils.trace(new Date().toString());
			
			// STEP 3 - Prepare data sample:
			this.train = inst[0];
			this.test = inst[1];
			inst = null;
			
			// STEP 4 - Run
			Mov3letsUtils.trace(showConfiguration(getDescriptor()));
			Mov3letsUtils.trace("");
			Mov3letsUtils.trace(printAttributes(getDescriptor()));
			Mov3letsUtils.trace("");
			Mov3letsUtils.getInstance().startTimer("[3] >> Processing time");
			mov3lets();
			Mov3letsUtils.trace("");
			Mov3letsUtils.getInstance().stopTimer("[3] >> Processing time");
			Mov3letsUtils.trace(new Date().toString());
			logger.end();
		}
		Mov3letsUtils.getInstance().setLogger(defaultLogger);
	}

	public void scheduleIfTimeContractable(ExecutorService executor) {
		if (getDescriptor().hasParam("time_contract")) {
			((TimeContract) getDescriptor().getParam("time_contract")).start(executor);
		}
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
	 * @param cdc2 
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
	private List<DiscoveryAdapter<MO>> instantiate(Class cdc, List<MO> classes, ProgressBar progressBar)  
			throws Exception {
		List<DiscoveryAdapter<MO>> lsMDs = new ArrayList<DiscoveryAdapter<MO>>();
		
//		Class cdc = getDiscoveryClass();
//		QualityMeasure qualityMeasure = instatiateQuality();
		
		/** STEP 2.1: Starts at discovering movelets */
		if (GlobalDiscovery.class.equals(getTypeOf(cdc))) { // Special case
			DiscoveryAdapter<MO> moveletsDiscovery = 
//					new IndexedMoveletsDiscovery<MO>(data, train, test, qualityMeasure, descriptor);
					(DiscoveryAdapter<MO>) cdc.getConstructor(List.class, List.class, List.class, QualityMeasure.class, Descriptor.class)
					.newInstance(data, train, test, instatiateQuality(), descriptor);
			
			// TODO Configure Outputs:
			moveletsDiscovery.setLock(newLock());
//			moveletsDiscovery.getOutputers().add(
//					new CSVIndexOutputter<MO>(descriptor.getParamAsText("respath"), null, descriptor, false));
			
			moveletsDiscovery.setOutputers(configOutput(1, null));
			
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

					Lock lock = newLock();
					List<OutputterAdapter<MO,?>> outs = configOutput(1, myclass); // For classes, it calls once.
					
					moveletsDiscovery = instantiateMoveletsDiscovery(cdc, instatiateQuality(), trajsFromClass, outs);
					moveletsDiscovery.setQueue(sharedQueue);
					
					// Configure Outputs
					moveletsDiscovery.setLock(lock);
					moveletsDiscovery.setOutputers(outs);
					lsMDs.add(moveletsDiscovery);

				} else {
					Lock lock = newLock();
					List<OutputterAdapter<MO,?>> outs = configOutput(trajsFromClass.size(), myclass);
					
					for (MAT<MO> T : trajsFromClass) {
						moveletsDiscovery = instantiateMoveletsDiscovery(cdc, instatiateQuality(), trajsFromClass, outs, T);
						moveletsDiscovery.setQueue(sharedQueue);
						
						// Configure Outputs
						moveletsDiscovery.setLock(lock);
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

	private Lock newLock() {
		return new Lock();
	}

	protected QualityMeasure instatiateQuality() {
		QualityMeasure qualityMeasure;
		switch (getDescriptor().getParamAsText("str_quality_measure")) {
		case "PROP":
			qualityMeasure = new FrequentQualityMeasure<MO>(this.train,
		    		getDescriptor().getParamAsInt("samples"), 
		    		getDescriptor().getParamAsDouble("sample_size"), 
		    		getDescriptor().getParamAsText("medium"));
			break;
			
		case "PLSP":
			qualityMeasure = new PLSPQualityMeasure<MO>(this.train, 
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
        	} else if (Arrays.asList(clazz.getInterfaces()).contains(GlobalDiscovery.class)) {
        		return GlobalDiscovery.class;
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
		
		switch (outx) {
		// For Singleton CLASSIFIER outputters:
		case "MSVM":
			o = MSVMOutputter.getInstance(resultDirPath, getDescriptor());
			break;
		case "MKNM":
			o = MKNMOutputter.getInstance(resultDirPath, getDescriptor());
			break;
		case "MKNT":
			o = MKNTOutputter.getInstance(resultDirPath, getDescriptor());
			break;

		// For Movelets and Features Output:
		default:
			o = (OutputterAdapter) Class.forName("br.ufsc.mov3lets.method.output."+outx+"Outputter")
					.getDeclaredConstructor(String.class, String.class, Descriptor.class)
					.newInstance(resultDirPath, (myclass == null? null : myclass.toString()), getDescriptor());
			break;
		}
		
		// For Singleton outputters:
//		if ("MSVM".equalsIgnoreCase(outx)) {
//			
//			o = MSVMOutputter.getInstance(resultDirPath, getDescriptor());
//			
//		} else if ("MKNM".equalsIgnoreCase(outx)) {
//			
//			o = MKNMOutputter.getInstance(resultDirPath, getDescriptor());
//			
//		} else if ("MKNT".equalsIgnoreCase(outx)) {
//			
//			o = MKNTOutputter.getInstance(resultDirPath, getDescriptor());
//			
//		} else {
//		
//			o = (OutputterAdapter) Class.forName("br.ufsc.mov3lets.method.output."+outx+"Outputter")
//				.getDeclaredConstructor(String.class, String.class, Descriptor.class)
//				.newInstance(resultDirPath, (myclass == null? null : myclass.toString()), getDescriptor());
//		}
			
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
		} else if (getDescriptor().notSet()) {
			setTrain(loader.load("train", getDescriptor()));
			
			getDescriptor().setAttributes(((MATLoader<MAT<?>>) loader).getAttributes());
			getDescriptor().setTrajectoryAttributes(((MATLoader<MAT<?>>) loader).getTrajectoryAttributes());
			getDescriptor().configure();
		} else {
			throw new RuntimeException("Provide a valid train input file.");
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
		
		// Trajectory Trimming
		if (getDescriptor().hasParam("data_trim")) {
			Mov3letsUtils.getInstance().startTimer("[1.4] >> Triming Data");
			trimTrajectoryEnd(this.train);
			trimTrajectoryEnd(this.test);
			Mov3letsUtils.getInstance().stopTimer( "[1.4] >> Triming Data");
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
	
	public void trimTrajectoryEnd(List<MAT<MO>> trajectories) {
		
		double p = getDescriptor().getParamAsDouble("data_trim");
		
		// Trim trajectories:
		for (MAT<MO> T : trajectories) {
			int ns = (int) ( T.getPoints().size() * p) + 1;
			T.setPoints(T.getPoints().subList(0, ns));
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
		
		return loader;
	}
	
	/**
	 * Show configuration.
	 *
	 * @param descriptor the descriptor
	 * @return the string
	 */
	public String showConfiguration(Descriptor descriptor) {

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
	 * Prints the params.
	 *
	 * @param params the params
	 * @param descriptor the descriptor
	 * @return the string
	 */
	public String printParams(HashMap<String, Object> params, Descriptor descriptor) {
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
		
		if (params.containsKey("data_resample"))
			at.addRow(new Object[] {"", "-- Data Resample", params.get("data_resample"), ""});
		
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
		
	public String printAttributes(Descriptor descriptor) {
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
	 * Config respath.
	 *
	 * @param descFile the desc file
	 * @param descriptor the descriptor
	 * @return the string
	 */
	public String configRespath(Descriptor descriptor) {
		
		String resultDirPath = "MASTERMovelets" + System.getProperty("file.separator");
		
		if (descriptor.getFlag("supervised") || descriptor.getParamAsText("version").equals("super")) {
			resultDirPath += "Super_";
		} else if (descriptor.getFlag("pivots") || descriptor.getParamAsText("version").equals("pivots")) {		
			resultDirPath += "Pivots_";		
		} else
			resultDirPath += descriptor.getParamAsText("version").toUpperCase() + "_";
		
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

		String DESCRIPTION_FILE_NAME = descriptor.hasParam("descfile")? FilenameUtils.removeExtension(
				new File(descriptor.getParamAsText("descfile")).getName()) : descriptor.getParamAsText("problemName").toString();
		
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
