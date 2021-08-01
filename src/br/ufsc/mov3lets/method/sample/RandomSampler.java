/**
 * 
 */
package br.ufsc.mov3lets.method.sample;

import java.util.List;

import br.ufsc.mov3lets.model.MAT;

/**
 * Stratified Random Sampler.
 * 
 * @author tarlis
 *
 */
public class RandomSampler<MO> extends Sampler<MO> {

	public RandomSampler(List<MAT<MO>> data) {
		super(data);
	}

	@Override
	public MAT<MO> next() {
		// TODO Auto-generated method stub
		return null;
	}

}
