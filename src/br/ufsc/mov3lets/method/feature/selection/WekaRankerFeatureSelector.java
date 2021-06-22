/**
 * 
 */
package br.ufsc.mov3lets.method.feature.selection;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.utils.ParserUtils;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.Ranker;

/**
 * @author tarlis
 *
 */
public abstract class WekaRankerFeatureSelector<MO> extends FeatureSelector<MO> {
	
	public abstract ASEvaluation getEvaluator();
	public abstract Ranker getRanker();
	
	public List<AttributeDescriptor> selectFeatures(List<MAT<MO>> train, 
			List<AttributeDescriptor> attributes, Descriptor descriptor) throws Exception {
		
		List<AttributeDescriptor> attrToRemove = new ArrayList<AttributeDescriptor>();
		int maxFeatures = maxFeatures(attributes.size(), descriptor);
		
		// Weka feature selection
		AttributeSelection attSelection = new AttributeSelection();
	    attSelection.setEvaluator(getEvaluator());
	    attSelection.setSearch(getRanker());

		attSelection.SelectAttributes(ParserUtils.convertSet(descriptor, train));
		int[] attIndex = attSelection.selectedAttributes();
		
		// Limit to the max features:
		attIndex = ArrayUtils.subarray(attIndex, 0, maxFeatures);
		
		// Attributes selection
		for (int i = 0; i < attributes.size(); i++) {
			if(!ArrayUtils.contains(attIndex, i))
				attrToRemove.add(attributes.get(i));
		}
		
		return attrToRemove;
	}

}
