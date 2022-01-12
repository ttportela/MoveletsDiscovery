/**
 * 
 */
package br.ufsc.mov3lets.method.feature.selection;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.Ranker;

/**
 * @author tarlis
 *
 */
public class CorrelationFeatureSelector<MO> extends WekaRankerFeatureSelector<MO> {

	@Override
	public ASEvaluation getEvaluator() {
		return new CorrelationAttributeEval();
	}

	@Override
	public Ranker getRanker() {
		return new Ranker();
	}

}
