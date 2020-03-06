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
public class HiperMoveletsDiscovery<MO> extends SupervisedMoveletsDiscovery<MO> {

	/**
	 * @param trajectory
	 * @param train
	 * @param candidates
	 * @param qualityMeasure
	 * @param descriptor
	 */
	public HiperMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> train, List<Subtrajectory> candidates,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajectory, train, candidates, qualityMeasure, descriptor);
	}

}
