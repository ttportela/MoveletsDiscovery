/**
 * 
 */
package br.ufsc.mov3lets.method.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.model.MAT;

/**
 * Stratified Random Sampler.
 * 
 * Adapted from the UEA Time Series Machine Learning (TSML) toolbox.
 * 
 * @author tarlis
 *
 */
public class StratifiedRandomSampler<MO> extends Sampler<MO> {
	
	protected int numSamples;
	protected double trainProp = 0.7;    // Default: 70% train | 30% test 
	protected double stratifyProp = 1.0; // Default: 100% - no stratification

	public StratifiedRandomSampler(List<MAT<MO>> train, List<MAT<MO>> test, int numSamples) {
		super(train, test);
		this.numSamples = numSamples;
	}

	public StratifiedRandomSampler(List<MAT<MO>> train, List<MAT<MO>> test, int numSamples, double trainProp, double stratifyProp) {
		this(train, test, numSamples);
		this.trainProp = trainProp;
		this.stratifyProp = stratifyProp;
	}

	@Override
	public List<MAT<MO>>[] nextSample() {
		if (++sample <= numSamples) {
			Random rand = new Random(sample);
			List<MAT<MO>> outputTrain = new ArrayList<MAT<MO>>(), outputTest = new ArrayList<MAT<MO>>();
			
			Iterator<MO> keys = classBins.keySet().iterator();
	        while(keys.hasNext()){
	        	MO classVal = keys.next();
	            List<MAT<MO>> bin = classBins.get(classVal);
	            
	            int size      = (int) (bin.size() * this.stratifyProp);
	            int trainSize = (int) Math.round(size * this.trainProp);
	            
	            Collections.shuffle(bin, rand); //randomise the bin.

	            outputTrain.addAll(bin.subList(0,trainSize));//copy the first portion of the bin into the train set
	            outputTest.addAll(bin.subList(trainSize, size));//copy the remaining portion of the bin into the test set.
	        }
			
			return new List[]{ outputTrain, outputTest };
		} else
			return null;
	}
}
