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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
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
import br.com.tarlis.mov3lets.model.mat.MovingObject;
import br.com.tarlis.mov3lets.model.mat.aspect.Aspect;
import br.com.tarlis.mov3lets.model.mat.aspect.Space2DAspect;
import br.com.tarlis.mov3lets.utils.ProgressBar;
import br.com.tarlis.mov3lets.utils.TimerUtils;
import br.com.tarlis.mov3lets.view.AttributeDescriptor;
import br.com.tarlis.mov3lets.view.Descriptor;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Mov3lets<MO> {
	
	// CONFIG:
	private static Descriptor descriptor = null;
	
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
		Instant begin = Instant.now();
		Instant start = begin;

		List<MAT> train = new ArrayList<MAT>();
		for (String file : descriptor.getInputFiles()) {
			train.addAll(loadTrajectories(file));
		}
		
		if (train.isEmpty()) { traceW("empty training set"); return; }
		Instant end = Instant.now();
	    Mov3lets.trace("STEP 1 - runned in: " + Duration.between(start, end));
		
		// STEP 2 - Select Candidates:
		start = Instant.now();
	    selectCandidates(train);
	    end = Instant.now();
	    Mov3lets.trace("STEP 2 - runned in: " + Duration.between(start, end));
		
		// STEP 3 - Qualify Candidates:
		start = Instant.now();
	    
	    end = Instant.now();
	    Mov3lets.trace("STEP 3 - runned in: " + Duration.between(start, end));
		
		// STEP 4 - Output:
		start = Instant.now();
	    
	    end = Instant.now();
	    Mov3lets.trace("STEP 4 - runned in: " + Duration.between(start, end));
	    Mov3lets.trace("Mov3lets - runned in: " + Duration.between(begin, end));
	}
	
	/**
	 * STEP 2
	 * @param train 
	 */
	private void selectCandidates(List<MAT> train) {
		List<MO> classes = train.stream().map(e -> (MO) e.getMovingObject()).distinct().collect(Collectors.toList());
		
		int N_THREADS = this.getDescriptor().getParamAsInt("nthreads");
		ThreadPoolExecutor executor = (ThreadPoolExecutor) 
				Executors.newFixedThreadPool(N_THREADS == 0? 1 : N_THREADS);
		List<Future<Integer>> resultList = new ArrayList<>();
		
		for (MO myclass : classes) {			
			// TODO: MoveletsRunUnit:304
//			if ( ! (new File(resultDirPath + myclass + "/test.csv").exists()) ) {
			trace("\tClass: " + myclass + ". Discovering movelets."); // Might be saved in HD
			
			TimerUtils.getInstance().startTimer("Class >> " + myclass);
			/** STEP 2.1: It starts at discovering movelets */
			for (MAT trajectory : train) {
				DiscoveryAdapter moveletsDiscovery =  this.getDescriptor().getFlag("PIVOTS")?
						new MoveletsPivotsDiscovery(trajectory, train) : 
						new MoveletsDiscovery(trajectory, train);
				resultList.add(executor.submit(moveletsDiscovery));
			}
			/** STEP 2.1: --------------------------------- */
			TimerUtils.getInstance().stopTimer("Class >> " + myclass);
			
			
		}
		
		/* Keeping up with Progress output */
		ProgressBar progressBar = new ProgressBar("Movelet Discovery");
		int progress = 0;
		progressBar.update(progress, train.size());
		List<Integer> results = new ArrayList<>();
		for (Future<Integer> future : resultList) {
			try {
				results.add(future.get());
				progressBar.update(progress++, train.size());
				Executors.newCachedThreadPool();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	public List<MAT> loadTrajectories(String inputFile) throws IOException {
		List<MAT> trajectories = new ArrayList<MAT>();
		MO mo = instantiateMovingObject("");
			
		CSVParser csvParser = CSVFormat.DEFAULT.parse(new InputStreamReader((new FileInputStream(inputFile))));
		csvParser.iterator().next();
		for (CSVRecord line : csvParser) {
			
			// Create a MO:
			String label = line.get(this.descriptor.getLabelFeature().getOrder()-1);
			if (!mo.equals(label)) {
				mo = instantiateMovingObject(label);
			}
			
			MAT mat = instantiateMAT(line);
			mat.setMovingObject(mo);
		    trajectories.add(mat);
		}
		csvParser.close();

		return trajectories;
	}
	
	public MO instantiateMovingObject(String label) {
		return (MO) new MovingObject<String>(label);
	}
	
	public MAT instantiateMAT(CSVRecord line) {
		MAT mat = new MAT<MovingObject<String>>();
		mat.setTid(Integer.parseInt(line.get(this.descriptor.getIdFeature().getOrder()-1)));
		mat.setAspects(new ArrayList<Aspect<?>>());
		
		for (AttributeDescriptor attr : this.descriptor.getAttributes()) {
			mat.getAspects().add(instantiateAspect(attr, line.get(attr.getOrder()-1)));
		}
		
		return mat;
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
					trace("Atribute datetime '"+value+"' in wrong format, must be yyyy-MM-dd HH:mm:ss");
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
	
	public static void trace(String s) {
		System.out.println(s);
	}
	
	public static void traceW(String s) {
		trace("Warning: " + s);
	}
	
	public static void traceE(String s, Exception e) {
		System.err.println("Error: " + s);
		e.printStackTrace();
	}

	/**
	 * @return the descriptor
	 */
	public static Descriptor getDescriptor() {
		return descriptor;
	}
	
}
