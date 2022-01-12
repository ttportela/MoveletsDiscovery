/**
 * 
 */
package br.ufsc.mov3lets.method.feature.selection;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.Ranker;

/**
 * @author tarlis
 *
 */
public class GainratioFeatureSelector<MO> extends WekaRankerFeatureSelector<MO> {

	@Override
	public ASEvaluation getEvaluator() {
		return new GainRatioAttributeEval();
	}

	@Override
	public Ranker getRanker() {
		return new Ranker();
	}

}
