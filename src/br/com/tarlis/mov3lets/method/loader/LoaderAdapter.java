package br.com.tarlis.mov3lets.method.loader;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;

public abstract class LoaderAdapter<T extends MAT<?>> {
	
	public abstract List<T> loadTrajectories(String file, Descriptor descriptor) throws IOException;
	
	public List<T> load(String file, Descriptor descriptor) throws IOException {
		String curpath = descriptor.hasParam("curpath")? descriptor.getParamAsText("curpath") : "./";
		
		List<T> data = loadTrajectories(Paths.get(curpath, file).toString(), descriptor);
		return data;
	}
	
	public List<T> load(Descriptor descriptor) throws IOException {
		String curpath = descriptor.hasParam("curpath")? descriptor.getParamAsText("curpath") : "./";
		
		List<T> data = new ArrayList<T>();
		for (String file : descriptor.getInputFiles()) {
			data.addAll(loadTrajectories(Paths.get(curpath, file).toString(), descriptor));
		}
		return data;
	}

}
