/**
 * 
 */
package br.com.tarlis.mov3lets.method.loader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import br.com.tarlis.mov3lets.model.mat.MAT;
import br.com.tarlis.mov3lets.model.mat.Point;
import br.com.tarlis.mov3lets.view.AttributeDescriptor;
import br.com.tarlis.mov3lets.view.Descriptor;

/**
 * @author tarlisportela
 *
 */
public class IndexedLoader<T extends MAT<?>> extends DefaultLoader<T> {

	// TODO: Alterar para usar index;
	@Override
	public List<T> loadTrajectories(String file, Descriptor descriptor) throws IOException {
		List<MAT<String>> trajectories = new ArrayList<MAT<String>>();
		// IF MO type is String:
		String mo = "";
		MAT<String> mat = null;
			
		CSVParser csvParser = CSVFormat.DEFAULT.parse(new InputStreamReader((new FileInputStream(file))));
		csvParser.iterator().next();
		for (CSVRecord line : csvParser) {
			int tid = Integer.parseInt(line.get(descriptor.getIdFeature().getOrder()-1));
			
			// Create a MO:
			String label = line.get(descriptor.getLabelFeature().getOrder()-1);
			if (!mo.equals(label)) {
				if (mat != null) 
				    trajectories.add(mat);
				// Can use like this:
//				mo = (MO) new MovingObject<String>(label);
//				mat = new MAT<MovingObject<String>>();
				// OR -- this for typing String:
				mo = label;
				mat = new MAT<String>();
				mat.setMovingObject(mo);
				mat.setTid(tid);
			}
			
			// For each attribute of POI
			Point poi = new Point();	
			poi.setTrajectory(mat);
			for (AttributeDescriptor attr : descriptor.getAttributes()) {
				poi.getAspects().put(attr.getText(), instantiateAspect(attr, line.get(attr.getOrder()-1)));
				mat.getPoints().add(poi);
			}
		}
		csvParser.close();

		return (List<T>) trajectories;
	}

}
