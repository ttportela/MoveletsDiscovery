package br.com.tarlis.mov3lets.method.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.tarlis.mov3lets.model.mat.MAT;
import br.com.tarlis.mov3lets.view.Descriptor;

public abstract class LoaderAdapter<T extends MAT<?>> {
	
	public abstract List<T> loadTrajectories(String file, Descriptor descriptor) throws IOException;
	
	public List<T> load(Descriptor descriptor) throws IOException {
		List<T> train = new ArrayList<T>();
		for (String file : descriptor.getInputFiles()) {
			train.addAll(loadTrajectories(file, descriptor));
		}
		return train;
	}

}
