/**
 * 
 */
package br.ufsc.mov3lets.method.feature.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.RankingAlgorithm;
import org.apache.commons.math3.stat.ranking.TiesStrategy;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;

/**
 * @author tarlis
 *
 */
public abstract class StatisticalFeatureSelector<MO> extends FeatureSelector<MO> {
	
	public List<AttributeDescriptor> selectFeatures(List<MAT<MO>> train, 
			List<AttributeDescriptor> attributes, Descriptor descriptor) {
		List<AttributeDescriptor> attrToRemove = new ArrayList<AttributeDescriptor>();
		
		int maxFeatures = maxFeatures(attributes.size(), descriptor);
		
		double scores[] = new double[attributes.size()];
		for (int i = 0; i < attributes.size(); i++) {
			AttributeDescriptor attr = attributes.get(i);
			scores[i] = score(i, attr, train);
		}
		
		RankingAlgorithm rankingAlgorithm = new NaturalRanking(TiesStrategy.SEQUENTIAL);
		double[] ranks = rankingAlgorithm.rank(scores);
		for (int i = 0; i < attributes.size(); i++) {
			if (ranks[i] <= (attributes.size() - maxFeatures)) {
				attrToRemove.add(attributes.get(i));
			}
		}
		
		return attrToRemove;
	}

	public double score(int index, AttributeDescriptor attr, List<MAT<MO>> train) {
		
		SummaryStatistics ds = new SummaryStatistics();
		
		if (attr.isNumeric()) {
			
			for (MAT<MO> mat : train) {
				for (Point p : mat.getPoints()) {
					Double value = p.getAspects().get(index).toDouble();
					if (value != null)
						ds.addValue(value);
				}
			}
			
		} else {
			
			HashMap<Object, Integer> counter = new HashMap<Object, Integer>();
			for (MAT<MO> mat : train) {
				for (Point p : mat.getPoints()) {
					Object key = p.getAspects().get(index).getValue();
					if (key != null) {
						Integer value = counter.getOrDefault(key, 0);
						counter.put(key, value+1);
					}
				}
			}
			
			for (Entry<Object, Integer> e : counter.entrySet()) {
				ds.addValue(e.getValue());
			}

		}
		
		return getScore(ds);
	}
	
	/*
	 * Override this method to use another statistic.
	 * 
	 * @param ds
	 * @return
	 */
	public double getScore(SummaryStatistics ds) {
		return ds.getMax() - ds.getMin();
	}

}
