/**
 * 
 */
package br.ufsc.mov3lets.method.feature.extraction;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.mov3lets.method.feature.extraction.point.AveragePointFeature;
import br.ufsc.mov3lets.method.feature.selection.CorrelationFeatureSelector;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;

/**
 * @author tarlis
 *
 */
public class CorrelationFeatureExtractor<MO> extends FeatureExtractor<MO> {

	@Override
	public List<AttributeDescriptor> updateTrajectories(Descriptor descriptor, List<AttributeDescriptor> attributes,
			List<MAT<MO>> train, List<MAT<MO>> test) throws Exception {
		
		CorrelationFeatureSelector<MO> fs = new CorrelationFeatureSelector<MO>();
		List<AttributeDescriptor> attrToRemove = fs.selectFeatures(train, attributes, descriptor);
		List<AttributeDescriptor> attrToAdd = new ArrayList<AttributeDescriptor>();
		
		for (int i = 0; i < attributes.size(); i++) 
			if (!attrToRemove.contains(attributes.get(i)) && attributes.get(i).isNumeric()) { // Indexes to analyze:
				PointFeature feat = new AveragePointFeature();
				feat.setIndex(i);
				AttributeDescriptor attr = descriptor.instantiateFeature(attributes.get(i), feat);
				attrToAdd.add(attr);
				feat.init(descriptor, attr);
				
				for (MAT<MO> mat : train)
					feat.fillPoints(mat);
				
				for (MAT<MO> mat : test)
					feat.fillPoints(mat);
			}
		
		return attrToAdd;
	}

}
