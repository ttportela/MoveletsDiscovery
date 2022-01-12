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
public class RandomSampler<MO> extends Sampler<MO> {
	
	protected int numSamples;

	public RandomSampler(List<MAT<MO>> train, List<MAT<MO>> test, int numSamples) {
		super(train, test);
		this.numSamples = numSamples;
	}

	@Override
	public List<MAT<MO>>[] nextSample() {
		if (++sample <= numSamples) {
			Random rand = new Random(sample);
			List<MAT<MO>> outputTrain = new ArrayList<MAT<MO>>(), outputTest = new ArrayList<MAT<MO>>();
			
			Iterator<MO> keys = classBins.keySet().iterator();
	        while(keys.hasNext()){
	        	MO classVal = keys.next();
	            int occurences = trainDistribution.get(classVal);
	            List<MAT<MO>> bin = classBins.get(classVal);
	            
	            Collections.shuffle(bin, rand); //randomise the bin.

	            outputTrain.addAll(bin.subList(0,occurences));//copy the first portion of the bin into the train set
	            outputTest.addAll(bin.subList(occurences, bin.size()));//copy the remaining portion of the bin into the test set.
	        }
			
			return new List[]{ outputTrain, outputTest };
		} else
			return null;
	}
}
