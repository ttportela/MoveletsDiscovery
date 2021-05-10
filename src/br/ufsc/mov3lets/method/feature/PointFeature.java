package br.ufsc.mov3lets.method.feature;

import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;

public abstract class PointFeature {
	
	public abstract void init(Descriptor descriptor);
	
	public abstract void fillPoints(MAT<?> trajectory);

}
