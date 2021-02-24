package br.ufsc.mov3lets.method.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 * The Class CSVLoader.
 *
 * @param <T> the generic type
 */
public class ARFFInternLoader<T extends MAT<?>> implements InterningLoaderAdapter<T>  {
	
	protected static int tid = 1;

	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.loader.LoaderAdapter#loadTrajectories(java.lang.String, br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor).
	 * 
	 * @param file
	 * @param descriptor
	 * @return
	 * @throws IOException
	 */
	@Override
	public List<T> loadTrajectories(String file, Descriptor descriptor) throws IOException {
		
		List<MAT<String>> trajectories = new ArrayList<MAT<String>>();
		
		for (AttributeDescriptor attr : descriptor.getAttributes()) {
			readSequences( (file + "_" + attr.getText() + ".arff") , trajectories, attr, descriptor);
		}

		return (List<T>) trajectories;
	}

	/**
	 * Mehod readSequences. 
	 * 
	 * @param file
	 * @param attr
	 * @param trajectories
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void readSequences(String file, List<MAT<String>> trajectories, AttributeDescriptor attr, Descriptor descriptor)
			throws FileNotFoundException, IOException {
		MAT<String> mat;
		BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
		ArffReader arff = new ArffReader(reader, 1000);         
		Instances data = arff.getStructure();
		data.setClassIndex(data.numAttributes() - 1);
		
		boolean isNew = trajectories.isEmpty();
		
		Instance inst;
		int i = 0;
		while ((inst = arff.readInstance(data)) != null) { 
			
			int labelOrder = descriptor.getLabelFeature().getOrder()-1;
			
			if (isNew) {
				// IF MO type is String:
	//			MO mo = new MO();
				mat = new MAT<String>();
				mat.setTid(tid++);
				// Can use like this:
//				mo = (MO) new MovingObject<String>(label);
				// OR -- this for typing String:
				String label = inst.stringValue(labelOrder);
				mat.setMovingObject(label);
				
				trajectories.add(mat);
			} else {
				mat = trajectories.get(i++);
			}
			
		    // the last attribute is ignored because it is the class label
		    for(int index = 0 ; index < inst.numAttributes()-1 ; index++) { 
		    	
		    	// For each attribute of POI
				Point poi;
				if (isNew) {
					poi = new Point();	
					poi.setTrajectory(mat);
					mat.getPoints().add(poi);
				} else {
					poi = mat.getPoints().get(index);
				}
				
				poi.getAspects().add(this.instantiateAspect(attr, inst, index));
		    			        
		    }
		}
	}
	
	/**
	 * Overridden method. 
	 * @see br.ufsc.mov3lets.method.loader.LoaderAdapter#instantiateAspect(br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor, java.lang.String).
	 * 
	 * @param attr
	 * @param value
	 * @return
	 */
	public Aspect<?> instantiateAspect(AttributeDescriptor attr, Instance inst, int index) {
		// TODO Auto-generated method stub
		switch(inst.attribute(index).type()) {
        case Attribute.NUMERIC :
            return InterningLoaderAdapter.super.instantiateAspect(attr, String.valueOf(inst.value(index)));
        case Attribute.STRING :
        case Attribute.NOMINAL:
        default:
        	return InterningLoaderAdapter.super.instantiateAspect(attr, inst.stringValue(index));
        }
	}

}
