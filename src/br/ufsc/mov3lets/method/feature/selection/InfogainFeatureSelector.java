/**
 * 
 */
package br.ufsc.mov3lets.method.feature.selection;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;

/**
 * @author tarlis
 *
 */
public class InfogainFeatureSelector<MO> extends WekaRankerFeatureSelector<MO> {

	@Override
	public ASEvaluation getEvaluator() {
		return new InfoGainAttributeEval();
	}

	@Override
	public Ranker getRanker() {
		return new Ranker();
	}

}
