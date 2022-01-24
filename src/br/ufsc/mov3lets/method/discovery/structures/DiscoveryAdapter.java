/**
 *  HIPERMovelets - Multiple Aspect Trajectory (MASTER) HIPER Classification. 
 *  Copyright (C) 2020 Big Data Lab UFSC, Florianópolis, Brazil
 *  Contact: Tarlis Portela
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
package br.ufsc.mov3lets.method.discovery.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.math3.util.Combinations;

import br.ufsc.mov3lets.method.distancemeasure.DistanceMeasure;
import br.ufsc.mov3lets.method.output.CSVOutputter;
import br.ufsc.mov3lets.method.output.JSONOutputter;
import br.ufsc.mov3lets.method.output.OutputterAdapter;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;
import br.ufsc.mov3lets.utils.log.ProgressBar;

/**
 * The Class DiscoveryAdapter.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 * @param <MO> the generic type
 */
public abstract class DiscoveryAdapter<MO> implements Callable<Integer> {

	/** The number of features. */
	protected int numberOfFeatures = 1;

	/** The max number of features. */
	protected int maxNumberOfFeatures = 2;
	
	/** The explore dimensions. */
	protected boolean exploreDimensions;

	/** The descriptor. */
	protected Descriptor descriptor;
	
	/** The progress bar. */
	protected ProgressBar progressBar;
	protected String stats = "";

//	protected MAT<MO> trajectory;
	/** The trajs from class. */
	protected List<MAT<MO>> trajsFromClass;
	
	/** The train. */
	protected List<MAT<MO>> train;
	
	/** The test. */
	protected List<MAT<MO>> test;
	
	/** The data. */
	protected List<MAT<MO>> data;
	
	/** The trajectory. */
	protected MAT<MO> trajectory;

//	protected List<Subtrajectory> candidates;

	/** The queue. */
	protected List<MAT<MO>> queue;
	
	/** The outputers. */
	protected List<OutputterAdapter<MO, ?>> outputers = new ArrayList<OutputterAdapter<MO, ?>>();
	
	/** The max value. */
	public double MAX_VALUE = DistanceMeasure.DEFAULT_MAX_VALUE;
	
	protected Lock lock; 
	
	/** The combinations. */
	protected int[][] combinations = null;
	
	/**
	 * Instantiates a new discovery adapter.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param descriptor the descriptor
	 */
	public DiscoveryAdapter(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, 
			Descriptor descriptor) {
		init(trajectory, trajsFromClass, data, train, test, descriptor);
	}
	
	/**
	 * Instantiates a new discovery adapter.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param descriptor the descriptor
	 * @param outputers the outputers
	 */
	public DiscoveryAdapter(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, 
			Descriptor descriptor, List<OutputterAdapter<MO, ?>> outputers) {
		init(trajectory, trajsFromClass, data, train, test, descriptor);
		this.outputers = outputers;
	}
	
	public void setLock(Lock lock) {
		this.lock = lock;
	}
	
	/**
	 * Inits the.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param descriptor the descriptor
	 */
	private void init(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, 
			List<MAT<MO>> train, List<MAT<MO>> test, Descriptor descriptor) {
		this.trajectory = trajectory;
		this.trajsFromClass = trajsFromClass;
		this.train = train;
		this.test = test;
//		this.candidates = candidates;
		this.descriptor = descriptor;
		this.data = data;
	}
	
	/**
	 * Discover.
	 *
	 * @return the list
	 */
	public abstract List<Subtrajectory> discover(); 

	/**
	 * Overridden method. 
	 * @see java.util.concurrent.Callable#call().
	 * 
	 * @return
	 * @throws Exception
	 */
	@Override
	public Integer call() throws Exception {
		
		discover();
		
		free();
		
		return 0;
	}
	
	/**
	 * Free.
	 */
	protected void free() {
		this.outputers = null;
		this.trajsFromClass = null;
		this.train = null;
		this.test = null;
		this.data = null;
		this.descriptor = null;

//		System.gc();
	}

	/**
	 * Inits the.
	 *
	 * @param qualityMeasure the quality measure
	 */
	protected void init() {
		this.numberOfFeatures = getDescriptor().numberOfFeatures();
		this.maxNumberOfFeatures = getDescriptor().getParamAsInt("max_number_of_features");
		this.exploreDimensions = getDescriptor().getFlag("explore_dimensions");
		
		switch (maxNumberOfFeatures) {
			case -1: // All features
			case -3: // Learn feature limits (mode)
			case -4: this.maxNumberOfFeatures = numberOfFeatures; break; // Learn feature limits (most frequent)
			
			case -2: this.maxNumberOfFeatures = (int) Math.ceil(Math.log(numberOfFeatures))+1; break;
			
			default: break; // Fixed number of features
		}
	}
	
	/**
	 * Make combinations.
	 *
	 * @param exploreDimensions the explore dimensions
	 * @param numberOfFeatures the number of features
	 * @param maxNumberOfFeatures the max number of features
	 * @return the int[][]
	 */
	public int[][] makeCombinations(boolean exploreDimensions, int numberOfFeatures, int maxNumberOfFeatures) {
		
		if (combinations != null)
			return combinations;
		
		int currentFeatures;
		if (exploreDimensions){
			currentFeatures = 1;
		} else {
			currentFeatures = numberOfFeatures;
		}
				
//		combinations = new int[(int) (Math.pow(2, numberOfFeatures) - 1)][];
		ArrayList<int[]> combaux = new ArrayList<int[]>();
//		int k = 0;
		// For each possible NumberOfFeatures and each combination of those: 
		for (;currentFeatures <= maxNumberOfFeatures; currentFeatures++) {
			for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) {					
				
//				combinations[k++] = comb;
				combaux.add(comb);
				
			} // for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) 					
		} // for (int i = 0; i < train.size(); i++

//		combinations = Arrays.stream(combinations).filter(Objects::nonNull).toArray(int[][]::new);
		combinations = combaux.stream().toArray(int[][]::new);
		
		return combinations;
	}

//	/**
//	 * @return the candidates
//	 */
//	public List<Subtrajectory> getCandidates() {
//		return candidates;
//	}
	
	/**
 * Gets the descriptor.
 *
 * @return the descriptor
 */
	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	/**
	 * Output.
	 *
	 * @param filename the filename
	 * @param trajectories the trajectories
	 * @param movelets the movelets
	 * @param delayOutput the delay output
	 */
	public void output(String filename, List<MAT<MO>> trajectories, List<Subtrajectory> movelets, boolean delayOutput) {
//		synchronized (DiscoveryAdapter.class) {
			// This sets the default outputters, otherwise use the configured ones	
			// By Default, it writes a JSON and a CSV in a attribute-value format
	//		defaultOutputters();
			// It puts distances as trajectory attributes
			if (outputers != null)
				for (OutputterAdapter output : outputers) {
					output.write(filename, trajectories, movelets, delayOutput);			
				}
			
	//		trajectories.forEach(e ->  e.getFeatures().clear()); // TODO needed?
	//		trajectories.forEach(e ->  e.getAttributes().clear());
//		}
	}
	
	/**
	 * Default outputters.
	 *
	 * @return true, if successful
	 */
	public boolean defaultOutputters() {
		if (this.outputers == null) {
			this.outputers = new ArrayList<OutputterAdapter<MO,?>>();
			this.outputers.add(new CSVOutputter<MO>(getDescriptor()));
			this.outputers.add(new JSONOutputter<MO>(getDescriptor()));
			return true;
		}
		return false;
	}
	
	/**
	 * Sets the outputers.
	 *
	 * @param outputers the outputers to set
	 */
	public void setOutputers(List<OutputterAdapter<MO,?>> outputers) {
		this.outputers = outputers;
	}
	
	/**
	 * Gets the outputers.
	 *
	 * @return the outputers
	 */
	public List<OutputterAdapter<MO,?>> getOutputers() {
		return outputers;
	}
	
	/**
	 * Gets the progress bar.
	 *
	 * @return the progress bar
	 */
	public ProgressBar getProgressBar() {
		return progressBar;
	}
	
	/**
	 * Sets the progress bar.
	 *
	 * @param progressBar the new progress bar
	 */
	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}
	
	/**
	 * Getter for data.
	 * 
	 * @return the data.
	 */
	public List<MAT<MO>> getData() {
		return data;
	}
	
	/**
	 * Getter for train.
	 * 
	 * @return the train.
	 */
	public List<MAT<MO>> getTrain() {
		return train;
	}
	
	/**
	 * Getter for test.
	 * 
	 * @return the test.
	 */
	public List<MAT<MO>> getTest() {
		return test;
	}
	
	/**
	 * Getter for trajectory.
	 * 
	 * @return the trajectory.
	 */
	public MAT<MO> getTrajectory() {
		return trajectory;
	}
	
	/**
	 * Getter for trajsFromClass.
	 * 
	 * @return the trajsFromClass.
	 */
	public List<MAT<MO>> getTrajsFromClass() {
		return trajsFromClass;
	}
	
	/**
	 * Getter for queue.
	 * 
	 * @return the queue.
	 */
	public List<MAT<MO>> getQueue() {
		return queue;
	}
	
	/**
	 * Setter for queue.
	 * 
	 * @param queue the queue to set (as List<MAT<MO>> instance).
	 */
	public void setQueue(List<MAT<MO>> queue) {
		this.queue = queue;
	}
	
	public String getStats() {
		return stats;
	}
	
	public void setStats(String stats) {
		this.stats = stats;
	}
	
	public void addStats(String str, Object stat) {
		this.stats = this.stats + str + ": " + stat + ". ";
	}

}
