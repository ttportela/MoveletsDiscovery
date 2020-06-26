/**
 * Mov3lets - Multiple Aspect Trajectory (MASTER) Classification Version 3. 
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
package br.com.tarlis.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import br.com.tarlis.mov3lets.method.distancemeasure.DistanceMeasure;
import br.com.tarlis.mov3lets.method.output.CSVOutputter;
import br.com.tarlis.mov3lets.method.output.JSONOutputter;
import br.com.tarlis.mov3lets.method.output.OutputterAdapter;
import br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;
import br.com.tarlis.mov3lets.utils.ProgressBar;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public abstract class DiscoveryAdapter<MO> implements Callable<Integer> {

	protected Descriptor descriptor;
	protected ProgressBar progressBar;
	
//	protected MAT<MO> trajectory;
	protected List<MAT<MO>> trajsFromClass;
	protected List<MAT<MO>> train;
	protected List<MAT<MO>> test;
	protected List<MAT<MO>> data;

//	protected List<Subtrajectory> candidates;
	
	protected List<OutputterAdapter<MO>> outputers = new ArrayList<OutputterAdapter<MO>>();
	
	public double MAX_VALUE = DistanceMeasure.DEFAULT_MAX_VALUE;
	
	/**
	 * @param train
	 * @param candidates 
	 */
	public DiscoveryAdapter(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, 
			Descriptor descriptor) {
		init(trajsFromClass, data, train, test, descriptor);
	}
	
	public DiscoveryAdapter(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, 
			Descriptor descriptor, List<OutputterAdapter<MO>> outputers) {
		init(trajsFromClass, data, train, test, descriptor);
		this.outputers = outputers;
	}
	
	private void init(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, 
			Descriptor descriptor) {
		this.trajsFromClass = trajsFromClass;
		this.train = train;
		this.test = test;
//		this.candidates = candidates;
		this.descriptor = descriptor;
		this.data = data;
	}
	
	public abstract List<Subtrajectory> discover(); 

	@Override
	public Integer call() throws Exception {
		
		discover();
		
		free();
		
		return 0;
	}
	
	protected void free() {
		this.outputers = null;
		this.trajsFromClass = null;
		this.train = null;
		this.test = null;
		this.data = null;
		this.descriptor = null;

		System.gc();
	}

//	/**
//	 * @return the candidates
//	 */
//	public List<Subtrajectory> getCandidates() {
//		return candidates;
//	}
	
	/**
	 * @return the descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	public void output(String filename, List<MAT<MO>> trajectories, List<Subtrajectory> movelets, boolean delayOutput) {	
		// This sets the default outputters, otherwise use the configured ones	
		// By Default, it writes a JSON and a CSV in a attribute-value format
//		defaultOutputters();
		// It puts distances as trajectory attributes
		if (outputers != null)
			for (OutputterAdapter<MO> output : outputers) {
				output.write(filename, trajectories, movelets, delayOutput);			
			}
		
//		trajectories.forEach(e ->  e.getFeatures().clear()); // TODO needed?
//		trajectories.forEach(e ->  e.getAttributes().clear());
	}
	
	public boolean defaultOutputters() {
		if (this.outputers == null) {
			this.outputers = new ArrayList<OutputterAdapter<MO>>();
			this.outputers.add(new CSVOutputter<MO>(getDescriptor()));
			this.outputers.add(new JSONOutputter<MO>(getDescriptor()));
			return true;
		}
		return false;
	}
	
	/**
	 * @param outputers the outputers to set
	 */
	public void setOutputers(List<OutputterAdapter<MO>> outputers) {
		this.outputers = outputers;
	}
	
	/**
	 * @return the outputers
	 */
	public List<OutputterAdapter<MO>> getOutputers() {
		return outputers;
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}
	
	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

}
