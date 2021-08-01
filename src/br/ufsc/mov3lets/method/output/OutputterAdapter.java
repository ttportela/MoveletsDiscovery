/**
 * 
 */
package br.ufsc.mov3lets.method.output;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;

/**
 * The Class OutputterAdapter.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public abstract class OutputterAdapter<MO, D> {
	
	/** The file path. */
	protected String filePath;
	
	/** The file path. */
	protected String movingObject;
	
	/** The descriptor. */
	protected Descriptor descriptor;
	
	/** The subfolder classes. */
	protected boolean subfolderClasses = true;

	/** Number of calls to write before saving output. */
	protected int delayCount = 0;

	/**
	 * Instantiates a new outputter adapter.
	 *
	 * @param descriptor the descriptor
	 */
	public OutputterAdapter(Descriptor descriptor) {
		this(descriptor.getParamAsText("respath"), "", descriptor, false);
	}
	
	/**
	 * Instantiates a new outputter adapter.
	 *
	 * @param filePath the file path
	 * @param descriptor the descriptor
	 * @param subfolderClasses the subfolder classes
	 */
	public OutputterAdapter(String filePath, String movingObjectName, Descriptor descriptor, boolean subfolderClasses) {
		this.filePath = filePath;
		this.movingObject = movingObjectName;
		this.descriptor = descriptor;
		this.subfolderClasses = subfolderClasses;
	}
	
	/**
	 * Write movelets.
	 * (Overwrite this method as synchronized)
	 *
	 * @param filename the filename
	 * @param trajectories the trajectories
	 * @param movelets the movelets
	 * @param delayOutput the delay output
	 */
	public abstract void write(String filename, List<MAT<MO>> trajectories, D data, boolean delayOutput, Object... params);
	
	/**
	 * Gets the file path.
	 *
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * Sets the file path.
	 *
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * Gets the descriptor.
	 *
	 * @return the descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	/**
	 * Sets the descriptor.
	 *
	 * @param descriptor the descriptor to set
	 */
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	/**
	 * Sets the subfolder classes.
	 *
	 * @param subfolderClasses the new subfolder classes
	 */
	public void setSubfolderClasses(boolean subfolderClasses) {
		this.subfolderClasses = subfolderClasses;
	}
	
	/**
	 * Checks if is subfolder classes.
	 *
	 * @return true, if is subfolder classes
	 */
	public boolean isSubfolderClasses() {
		return subfolderClasses;
	}
	
	/**
	 * Gets the file.
	 *
	 * @param className the class name
	 * @param filename the filename
	 * @return the file
	 */
	public File getFile(String className, String filename) {
		return Paths.get(getFilePath(),
				(this.subfolderClasses?	className : ""),
				filename).toFile();
	}
	
	public String getMovingObject() {
		return movingObject;
	}
	
	public void setMovingObject(String movingObject) {
		this.movingObject = movingObject;
	}
	
	/**
	 * Getter for delayCount.
	 * 
	 * @return the delayCount.
	 */
	public int getDelayCount() {
		return delayCount;
	}
	
	/**
	 * Setter for delayCount.
	 * 
	 * @param delayCount the delayCount to set (as int instance).
	 */
	public void setDelayCount(int delayCount) {
		this.delayCount = delayCount;
	}
	
	/**
	 * Setter for delayCount.
	 * 
	 * @param delayCount the delayCount to set (as int instance).
	 */
	public void setDelay(int delayCount) {
		this.delayCount += delayCount;
	}
	
	/**
	 * Mehod decreaseDelayCount. 
	 * 
	 * @param filename
	 */
	protected void decreaseDelayCount(String filename) {
		if ("train".equals(filename))
			delayCount--;
	}

}
