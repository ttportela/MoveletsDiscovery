/**
 * 
 */
package br.com.tarlis.mov3lets.method.output;

import java.util.List;

import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

/**
 * @author tarlis
 *
 */
public class DefaultOutputter<MO> extends OutputterAdapter<MO> {
	
	public DefaultOutputter(String filePath, Descriptor descriptor) {
		super(filePath, descriptor);
	}

	/**
	 * @param descriptor
	 */
	public DefaultOutputter(Descriptor descriptor) {
		super(descriptor);
	}

	@Override
	public void write(List<MAT<MO>> trajectories, List<Subtrajectory> movelets) {
		new JSONOutputter<MO>(filePath, descriptor).write(trajectories, movelets);
		new CSVOutputter<MO>(filePath, descriptor).write(trajectories, movelets);
	}

}
