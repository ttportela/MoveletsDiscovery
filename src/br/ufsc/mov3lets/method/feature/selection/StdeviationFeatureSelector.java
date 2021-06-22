/**
 * 
 */
package br.ufsc.mov3lets.method.feature.selection;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * @author tarlis
 *
 */
public class StdeviationFeatureSelector<MO> extends StatisticalFeatureSelector<MO> {

	public double getScore(SummaryStatistics ds) {
		return ds.getStandardDeviation();
	}

}
