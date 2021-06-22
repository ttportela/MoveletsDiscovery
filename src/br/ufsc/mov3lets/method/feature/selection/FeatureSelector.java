package br.ufsc.mov3lets.method.feature.selection;

import java.util.List;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;

public abstract class FeatureSelector<MO> {
	
	public abstract List<AttributeDescriptor> selectFeatures(List<MAT<MO>> train, 
			List<AttributeDescriptor> attributes, Descriptor descriptor) throws Exception;
	
	public List<AttributeDescriptor> updateTrajectories(Descriptor descriptor, 
			List<AttributeDescriptor> attributes, List<MAT<MO>> train, List<MAT<MO>> test) throws Exception {
			
		List<AttributeDescriptor> attrToRemove = selectFeatures(train, attributes, descriptor);	
		
		// Removes by inverse indexing to avoid reordering problems
		int[] idxToRemove = new int[attrToRemove.size()]; int k = 0;
		for (int i = attributes.size()-1; i >= 0; i--) 
			if (attrToRemove.contains(attributes.get(i))) 
				idxToRemove[k++] = i;

		for (MAT<MO> mat : train)
			for (Point p : mat.getPoints())
				for (int i : idxToRemove)
					p.getAspects().remove(i);
		
		for (MAT<MO> mat : test)
			for (Point p : mat.getPoints())
				for (int i : idxToRemove)
					p.getAspects().remove(i);
		
		return attrToRemove;
	}

	public int maxFeatures(int numOfFeatures, Descriptor descriptor) {
		int maxFeatures = numOfFeatures;
		int maxNumberOfFeatures = descriptor.getParamAsInt("max_number_of_features");
		switch (maxNumberOfFeatures) {
			// Use log in any negative case
			case -1: // All features
			case -3: // Learn feature limits (mode)
			case -4: // Learn feature limits (most frequent)
			case -2: maxFeatures = (int) Math.ceil(Math.log(maxFeatures))+1; break; 
			
			default: maxFeatures = maxNumberOfFeatures; // Fixed number of features
		}
		return maxFeatures;
	}

}