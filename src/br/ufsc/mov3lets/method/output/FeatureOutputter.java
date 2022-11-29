package br.ufsc.mov3lets.method.output;

import java.util.List;

import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.Feature;
import br.ufsc.mov3lets.model.MAT;

public abstract class FeatureOutputter<MO> extends OutputterAdapter<MO, List<Feature>> {
	
	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param filePath the file path
	 * @param descriptor the descriptor
	 * @param subfolderClasses the subfolder classes
	 */
	public FeatureOutputter(String filePath, String movingObjectName, Descriptor descriptor, boolean subfolderClasses) {
		super(filePath, movingObjectName, descriptor, subfolderClasses);
	}
	
	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param descriptor the descriptor
	 */
	public FeatureOutputter(Descriptor descriptor) {
		super(descriptor);
	}

	public abstract void writeMovelet(String filename, List<MAT<MO>> trajectories, Feature movelet, Object... params);
	
}
