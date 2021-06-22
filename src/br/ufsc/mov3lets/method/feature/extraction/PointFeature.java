package br.ufsc.mov3lets.method.feature.extraction;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;

public abstract class PointFeature {
	
	protected int index = -1;
	
	public PointFeature() { }
	
	public PointFeature(int index) {
		this.index = index;
	}
	
	public abstract void init(Descriptor descriptor, AttributeDescriptor feature);
	
	public abstract void fillPoints(MAT<?> trajectory);
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}

}
