package br.ufsc.mov3lets.method.feature.extraction;

import java.util.List;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;

public abstract class FeatureExtractor<MO> {
		
	public abstract List<AttributeDescriptor> updateTrajectories(Descriptor descriptor, 
			List<AttributeDescriptor> attributes, List<MAT<MO>> train, List<MAT<MO>> test) throws Exception;

}
