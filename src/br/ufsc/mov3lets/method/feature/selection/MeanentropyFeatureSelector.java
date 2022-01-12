/**
 * 
 */
package br.ufsc.mov3lets.method.feature.selection;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;

/**
 * @author tarlis
 *
 */
public class MeanentropyFeatureSelector<MO> extends StatisticalFeatureSelector<MO> {

	public double score(int index, AttributeDescriptor attr, List<MAT<MO>> train) {
		
		long total = 0;
//		HashMap<Object, HashMap<Object, Long>> c_counter = new HashMap<Object, HashMap<Object, Long>>();
		HashMap<Object, Long> counter = new HashMap<Object, Long>();
//		HashMap<Object, Long> tot_counter = new HashMap<Object, Long>();
		for (MAT<MO> mat : train) {
//			HashMap<Object, Long> counter = c_counter.getOrDefault(mat.getMovingObject(), new HashMap<Object, Long>());
			for (Point p : mat.getPoints()) {
				Object key = p.getAspects().get(index).getValue();
				if (key != null) {
					Long value = counter.getOrDefault(key, 0L);
					counter.put(key, value+1);
//					value = tot_counter.getOrDefault(key, 0L);
//					counter.put(key, value+1);
				}
			}
			total += mat.getPoints().size();
//			c_counter.put(mat.getMovingObject(), counter);
		}
		
		double entropy = 0.0;		
		for (Entry<Object, Long> e : counter.entrySet()) {
			double p = e.getValue()*1.0 / total;
			if (p > 0.0)
				entropy += -(p * Math.log(p) / Math.log(2));
		}
		
		return entropy / counter.size();
	}

}
