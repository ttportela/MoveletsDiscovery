/**
 * 
 */
package br.com.tarlis.mov3lets.method.output;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

/**
 * @author tarlis
 *
 */
public abstract class OutputterAdapter<MO> {
	
	protected String filePath;
	protected Descriptor descriptor;
	
	protected boolean subfolderClasses = true;
	

	public OutputterAdapter(Descriptor descriptor) {
		this(descriptor.getParamAsText("respath"), descriptor, false);
	}
	
	/**
	 * 
	 */
	public OutputterAdapter(String filePath, Descriptor descriptor, boolean subfolderClasses) {
		this.filePath = filePath;
		this.descriptor = descriptor;
		this.subfolderClasses = subfolderClasses;
	}
	
	public abstract void write(String filename, List<MAT<MO>> trajectories, List<Subtrajectory> movelets, boolean delayOutput);
	
	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * @return the descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	/**
	 * @param descriptor the descriptor to set
	 */
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	public void setSubfolderClasses(boolean subfolderClasses) {
		this.subfolderClasses = subfolderClasses;
	}
	
	public boolean isSubfolderClasses() {
		return subfolderClasses;
	}
	
	public File getFile(String className, String filename) {
		return Paths.get(getFilePath(),
				(this.subfolderClasses?	className : ""),
				filename).toFile();
	}
	
}
