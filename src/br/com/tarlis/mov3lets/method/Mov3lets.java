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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import br.com.tarlis.mov3lets.method.discovery.DiscoveryAdapter;
import br.com.tarlis.mov3lets.method.discovery.MoveletsDiscovery;
import br.com.tarlis.mov3lets.method.discovery.MoveletsPivotsDiscovery;
import br.com.tarlis.mov3lets.model.mat.MAT;
import br.com.tarlis.mov3lets.model.mat.Point;
import br.com.tarlis.mov3lets.model.mat.Subtrajectory;
import br.com.tarlis.mov3lets.model.mat.aspect.Aspect;
import br.com.tarlis.mov3lets.model.mat.aspect.Space2DAspect;
import br.com.tarlis.mov3lets.model.qualitymeasure.LeftSidePureCVLigth;
import br.com.tarlis.mov3lets.model.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;
import br.com.tarlis.mov3lets.utils.ProgressBar;
import br.com.tarlis.mov3lets.view.AttributeDescriptor;
import br.com.tarlis.mov3lets.view.Descriptor;

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
		Mov3letsUtils.getInstance().startTimer("[1] ==> LOADING INPUT");
		List<MAT<MO>> train = new ArrayList<MAT<MO>>();
		for (String file : descriptor.getInputFiles()) {
			train.addAll(loadTrajectories(file));
		}
		
		if (train.isEmpty()) { Mov3letsUtils.traceW("empty training set"); return; }
		Mov3letsUtils.getInstance().stopTimer("[1] ==> LOADING INPUT");
		
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
			Mov3letsUtils.trace("\tClass: " + myclass + ". Discovering movelets."); // Might be saved in HD
			
			Mov3letsUtils.getInstance().startTimer("\tClass >> " + myclass);
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
			Mov3letsUtils.getInstance().stopTimer("\tClass >> " + myclass);
		}
		
		/* Keeping up with Progress output */
		trackProgress(train, resultList);
		
		return candidates;
	}

	public List<MAT<MO>> loadTrajectories(String inputFile) throws IOException {
		List<MAT<MO>> trajectories = new ArrayList<MAT<MO>>();
		// IF MO type is String:
		MO mo = (MO) "";
		MAT<MO> mat = null;
			
		CSVParser csvParser = CSVFormat.DEFAULT.parse(new InputStreamReader((new FileInputStream(inputFile))));
		csvParser.iterator().next();
		for (CSVRecord line : csvParser) {
			int tid = Integer.parseInt(line.get(getDescriptor().getIdFeature().getOrder()-1));
			
			// Create a MO:
			String label = line.get(getDescriptor().getLabelFeature().getOrder()-1);
			if (!mo.equals(label)) {
				if (mat != null) 
				    trajectories.add(mat);
				// Can use like this:
//				mo = (MO) new MovingObject<String>(label);
//				mat = new MAT<MovingObject<String>>();
				// OR -- this for typing String:
				mo = (MO) label;
				mat = (MAT<MO>) new MAT<String>();
				mat.setMovingObject(mo);
				mat.setTid(tid);
			}
			
			// For each attribute of POI
			Point poi = new Point();	
			poi.setTrajectory(mat);
			for (AttributeDescriptor attr : getDescriptor().getAttributes()) {
				poi.getAspects().put(attr.getText(), instantiateAspect(attr, line.get(attr.getOrder()-1)));
				mat.getPoints().add(poi);
			}
		}
		csvParser.close();

		return trajectories;
	}
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public Aspect<?> instantiateAspect(AttributeDescriptor attr, String value) {
		switch (attr.getType()) {
			case "numeric":
				return new Aspect<Double>(Double.parseDouble(value));
			case "space2d":
				return new Space2DAspect(value);
			case "time":
				return new Aspect<Integer>(Integer.parseInt(value));
			case "datetime":
				try {
					return new Aspect<Date>(formatter.parse(value));
				} catch (ParseException e) {
					Mov3letsUtils.trace("\tAtribute datetime '"+value+"' in wrong format, must be yyyy-MM-dd HH:mm:ss");
					return new Aspect<Date>(new Date());
				}
			case "localdate":
				return new Aspect<LocalDate>(LocalDate.parse(value));
			case "localtime":
				return new Aspect<LocalTime>(LocalTime.parse(value));
			case "foursquarevenue":
			case "gowallacheckin":
			case "nominal":
			default:
				return new Aspect<String>(value);
		}
	}
	
//	public Class<?> aspectClass(AttributeDescriptor attr) {
//		switch (attr.getType()) {
//			case "numeric":
//				return Class.forName(Aspect.class.getCanonicalName() + "<Double>");
//			case "space2d":
//				return new Space2DAspect(value);
//			case "time":
//				return new Aspect<Integer>(Integer.parseInt(value));
//			case "datetime":
//				try {
//					return new Aspect<Date>(formatter.parse(value));
//				} catch (ParseException e) {
//					trace("Atribute datetime '"+value+"' in wrong format, must be yyyy/MM/dd HH:mm:ss");
//					return new Aspect<Date>(new Date());
//				}
//			case "localdate":
//				return new Aspect<LocalDate>(LocalDate.parse(value));
//			case "localtime":
//				return new Aspect<LocalTime>(LocalTime.parse(value));
//			case "foursquarevenue":
//			case "gowallacheckin":
//			case "nominal":
//			default:
//				return new Aspect<String>(value);
//		}
//	}
	
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
