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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.method.discovery.DiscoveryAdapter;
import br.com.tarlis.mov3lets.method.discovery.MoveletsDiscovery;
import br.com.tarlis.mov3lets.method.discovery.MoveletsPivotsDiscovery;
import br.com.tarlis.mov3lets.method.loader.DefaultLoader;
import br.com.tarlis.mov3lets.method.loader.IndexedLoader;
import br.com.tarlis.mov3lets.method.loader.InterningLoader;
import br.com.tarlis.mov3lets.model.mat.MAT;
import br.com.tarlis.mov3lets.model.mat.Subtrajectory;
import br.com.tarlis.mov3lets.model.qualitymeasure.LeftSidePureCVLigth;
import br.com.tarlis.mov3lets.model.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;
import br.com.tarlis.mov3lets.utils.ProgressBar;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Mov3lets<MO> {
	
	// CONFIG:
	private Descriptor descriptor = null;
	
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

		// [1] - Input:
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
		Mov3letsUtils.getInstance().startTimer("[2] ==> Select Candidates");
		List<Subtrajectory> candidates = selectCandidates(train, new LeftSidePureCVLigth(train, 
	    		getDescriptor().getParamAsInt("samples"), 
	    		getDescriptor().getParamAsDouble("sampleSize"), 
	    		getDescriptor().getParamAsText("medium")));
		Mov3letsUtils.getInstance().startTimer("[2] ==> Select Candidates");
		
		// STEP 3 - Qualify Candidates:
		Mov3letsUtils.getInstance().startTimer("[3] ==> Qualify Candidates");
		Mov3letsUtils.trace("@@ TEST CANDIDATES: @@");
		for (Subtrajectory subtrajectory : candidates) {
			Mov3letsUtils.trace(subtrajectory.toString());
		}
		Mov3letsUtils.trace("@@ ---------------- @@");
		Mov3letsUtils.getInstance().startTimer("[3] ==> Qualify Candidates");
		
		// STEP 4 - Output:
		Mov3letsUtils.getInstance().startTimer("[4] ==> Output");

		Mov3letsUtils.getInstance().startTimer("[4] ==> Output");
	}
	
	/**
	 * STEP 2
	 * @param train 
	 * @param leftSidePureCVLigth 
	 */
	private List<Subtrajectory> selectCandidates(List<MAT<MO>> train, QualityMeasure qualityMeasure) {
		List<MO> classes = train.stream().map(e -> (MO) e.getMovingObject()).distinct().collect(Collectors.toList());
		
		List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();
		
		int N_THREADS = getDescriptor().getParamAsInt("nthreads");
		ThreadPoolExecutor executor = (ThreadPoolExecutor) 
				Executors.newFixedThreadPool(N_THREADS == 0? 1 : N_THREADS);
		List<Future<Integer>> resultList = new ArrayList<>();
		
		for (MO myclass : classes) {			
			// TODO: MoveletsRunUnit:304
//			if ( ! (new File(resultDirPath + myclass + "/test.csv").exists()) ) {
			
			/** STEP 2.1: It starts at discovering movelets */
			for (MAT<MO> trajectory : train) {
//				Mov3letsUtils.trace("\t>> Trajectory: "+trajectory.getTid()+". "
//						+ "Trajectory Size: "+trajectory.getPoints().size()+". Number of Candidates: 459. Total of Movelets: 13. Max Size: 17. Used Features: 2" + myclass + ". Discovering movelets.");

				DiscoveryAdapter<MO> moveletsDiscovery =  getDescriptor().getFlag("PIVOTS")?
						new MoveletsPivotsDiscovery<MO>(trajectory, train, candidates, qualityMeasure, getDescriptor()) : 
						new MoveletsDiscovery<MO>(trajectory, train, candidates, qualityMeasure, getDescriptor());
				resultList.add(executor.submit(moveletsDiscovery));	
			}
			/** STEP 2.1: --------------------------------- */
		}
		
		/* Keeping up with Progress output */
		trackProgress(train, resultList);
		
		return candidates;
	}
	
	public void writeShapelets(List<Subtrajectory> candidates, String filepath) {
		BufferedWriter writer;
		try {
			
			File file = new File(filepath);
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file));

			for (Subtrajectory subtrajectory : candidates) {
				writer.write(subtrajectory.toString() + System.getProperty("line.separator"));
			}

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param train
	 * @param result
	 */
	private void trackProgress(List<MAT<MO>> train, List<Future<Integer>> result) {
		ProgressBar progressBar = new ProgressBar("\tMovelet Discovery");
		int progress = 0;
		progressBar.update(progress, train.size());
		List<Integer> results = new ArrayList<>();
		for (Future<Integer> future : result) {
			try {
				results.add(future.get());
				progressBar.update(progress++, train.size());
				Executors.newCachedThreadPool();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}
	
}
