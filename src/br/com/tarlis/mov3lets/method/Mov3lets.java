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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.method.discovery.DiscoveryAdapter;
import br.com.tarlis.mov3lets.method.discovery.MoveletsDiscovery_old;
import br.com.tarlis.mov3lets.method.discovery.PivotsMoveletsDiscovery;
import br.com.tarlis.mov3lets.method.discovery.SupervisedMoveletsDiscovery;
import br.com.tarlis.mov3lets.method.loader.DefaultLoader;
import br.com.tarlis.mov3lets.method.loader.IndexedLoader;
import br.com.tarlis.mov3lets.method.loader.InterningLoader;
import br.com.tarlis.mov3lets.method.output.CSVOutputter;
import br.com.tarlis.mov3lets.method.output.JSONOutputter;
import br.com.tarlis.mov3lets.method.output.OutputterAdapter;
import br.com.tarlis.mov3lets.method.qualitymeasure.LeftSidePureCVLigth;
import br.com.tarlis.mov3lets.method.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;
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
	//	private List<MAT> train = null;

	/**
	 * @param descFile
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public Mov3lets(String descriptorFile) throws UnsupportedEncodingException, FileNotFoundException {
		this.descriptor = Descriptor.load(descriptorFile);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public void mov3lets() throws IOException {

		// STEP 1 - Input:
		Mov3letsUtils.getInstance().startTimer("[1] ==> LOAD INPUT");
//		Mov3letsUtils.getInstance().printMemory();
		List<MAT<MO>> train;
		if (getDescriptor().getFlag("indexed")) {
			train = new IndexedLoader<MAT<MO>>().load(getDescriptor());
		} else if (getDescriptor().getFlag("interning")) {
			train = new InterningLoader<MAT<MO>>().load(getDescriptor());
		} else {
			train = new DefaultLoader<MAT<MO>>().load(getDescriptor());
		}
		Mov3letsUtils.getInstance().printMemory();
		
		if (train.isEmpty()) { Mov3letsUtils.traceW("empty training set"); return; }
		Mov3letsUtils.getInstance().stopTimer("[1] ==> LOAD INPUT");
		
		// STEP 2 - Select Candidates:
		Mov3letsUtils.getInstance().startTimer("[2] ==> Extracting Movelets");
//		List<Subtrajectory> candidates = 
		selectCandidates(train, new LeftSidePureCVLigth(train, 
	    		getDescriptor().getParamAsInt("samples"), 
	    		getDescriptor().getParamAsDouble("sampleSize"), 
	    		getDescriptor().getParamAsText("medium")));
		Mov3letsUtils.getInstance().startTimer("[2] ==> Extracting Movelets");
		
		// STEP 3 - Output:
//		Mov3letsUtils.getInstance().startTimer("[3] ==> Output");
//		Mov3letsUtils.getInstance().startTimer("[3] ==> Output");
	}
	
	/**
	 * STEP 2
	 * @param train 
	 * @param qualityMeasure 
	 */
	private List<Subtrajectory> selectCandidates(List<MAT<MO>> train, QualityMeasure qualityMeasure) {
		List<MO> classes = train.stream().map(e -> (MO) e.getMovingObject()).distinct().collect(Collectors.toList());
		
		List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();
		
		int N_THREADS = getDescriptor().getParamAsInt("nthreads");
//		ThreadPoolExecutor executor = (ThreadPoolExecutor) 
//				Executors.newFixedThreadPool(N_THREADS == 0? 1 : N_THREADS);
//		List<Future<Integer>> resultList = new ArrayList<>();
		
		ProgressBar progressBar = new ProgressBar("Movelet Discovery");
		int progress = 0;
//		progressBar.update(progress, train.size());
		
		/** STEP 2.1: Starts at discovering movelets */
		for (MO myclass : classes) {
			if ( ! (new File(resultDirPath + myclass + "/test.csv").exists()) ) {
				for (MAT<MO> trajectory : train) {
					DiscoveryAdapter<MO> moveletsDiscovery;
					if (getDescriptor().getFlag("pivots")) {
						moveletsDiscovery = new PivotsMoveletsDiscovery<MO>(trajectory, train, candidates, qualityMeasure, 
								getDescriptor());
					} else if (getDescriptor().getFlag("supervised")) {
						moveletsDiscovery = new SupervisedMoveletsDiscovery<MO>(trajectory, train, candidates, qualityMeasure, 
								getDescriptor());
					} else {
						moveletsDiscovery = new MoveletsDiscovery_old<MO>(trajectory, train, candidates, qualityMeasure, 
								getDescriptor());
					}
					
					/** Configuring outputs: */
					moveletsDiscovery.setOutputers(new ArrayList<OutputterAdapter<MO>>());
					moveletsDiscovery.getOutputers().add(new JSONOutputter<MO>(resultDirPath, getDescriptor()));
					moveletsDiscovery.getOutputers().add(new CSVOutputter<MO>(resultDirPath, getDescriptor()));

					progressBar.update(progress++, train.size());
//					resultList.add(executor.submit(moveletsDiscovery));
					moveletsDiscovery.discover();
				}
			} else {
				Mov3letsUtils.trace("\t[Class: " + myclass + "] >> Movelets previously discovered.");
			}
		}
		/** STEP 2.1: --------------------------------- */
		
		/* Keeping up with Progress output */
//		ProgressBar progressBar = new ProgressBar("\tMovelet Discovery");
//		int progress = 0;
//		progressBar.update(progress, train.size());
//		List<Integer> results = new ArrayList<Integer>();
//		for (Future<Integer> future : resultList) {
//			try {
//				results.add(future.get());
//				progressBar.update(progress++, train.size());
//				Executors.newCachedThreadPool();
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
//			}
//		}
		
		return candidates;
	}

//	public void writeShapelets(List<Subtrajectory> candidates, String filepath) {
//		BufferedWriter writer;
//		try {
//			
//			File file = new File(filepath);
//			file.getParentFile().mkdirs();
//			writer = new BufferedWriter(new FileWriter(file));
//
//			for (Subtrajectory subtrajectory : candidates) {
//				writer.write(subtrajectory.toString() + System.getProperty("line.separator"));
//			}
//
//			writer.close();
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	/**
	 * @return the descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	/**
	 * @param resultDirPath the resultDirPath to set
	 */
	public void setResultDirPath(String resultDirPath) {
		this.resultDirPath = resultDirPath;
	}
	
}
