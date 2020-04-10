/**
 * 
 */
package br.com.tarlis.mov3lets.method.discovery;

import java.util.List;

import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.method.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

/**
 * @author tarlis
 * @param <MO>
 *
 */
public class HiperMoveletsDiscovery<MO> extends SuperMoveletsDiscovery<MO> {

	/**
	 * @param trajsFromClass
	 * @param train
	 * @param candidates
	 * @param qualityMeasure
	 * @param descriptor
	 */
	public HiperMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, List<Subtrajectory> candidates,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajsFromClass, data, train, test, candidates, qualityMeasure, descriptor);
	}

}
