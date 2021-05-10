/**
 * 
 */
package br.ufsc.mov3lets.method.discovery.deprecated;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.discovery.RandomMoveletsDiscovery;
import br.ufsc.mov3lets.method.qualitymeasure.ProportionQualityMeasure;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class HiperMoveletsDiscovery.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class SparseMoveletsDiscovery<MO> extends RandomMoveletsDiscovery<MO> {

	/**
	 * Instantiates a new hiper random movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data           the data
	 * @param train          the train
	 * @param test           the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor     the descriptor
	 */
	public SparseMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train,
			List<MAT<MO>> test, QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajectory, trajsFromClass, data, train, test, qualityMeasure, descriptor);
	}

	protected void printStart() {
		progressBar.trace("Sparse Movelets Discovery for Class: " + trajectory.getMovingObject());
	}
	
	/**
	 * Gets the distances.
	 *
	 * @param a the a
	 * @param b the b
	 * @param comb the comb
	 * @return the distances
	 */
	public double[] getDistances(Point a, Point b, int[] comb) {

//		double[] distances = new double[this.descriptor.getAttributes().size()];
		double[] distances = new double[comb.length];
		
		int i = 0;
		for (int k : comb) {
			AttributeDescriptor attr = this.descriptor.getAttributes().get(k);
			
			distances[i++] = attr.getDistanceComparator().calculateDistance(
					a.getAspects().get(k), 
					b.getAspects().get(k), 
					attr); // This also enhance distances
		}
		
		return distances;
	}		

}
