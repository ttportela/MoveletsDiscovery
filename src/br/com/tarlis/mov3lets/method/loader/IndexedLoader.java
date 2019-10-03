/**
 * 
 */
package br.com.tarlis.mov3lets.method.loader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import br.com.tarlis.mov3lets.model.mat.MAT;
import br.com.tarlis.mov3lets.model.mat.Point;
import br.com.tarlis.mov3lets.model.mat.aspect.Aspect;
import br.com.tarlis.mov3lets.model.mat.aspect.InterningAspect;
import br.com.tarlis.mov3lets.model.mat.aspect.Space2DAspect;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;
import br.com.tarlis.mov3lets.view.AttributeDescriptor;
import br.com.tarlis.mov3lets.view.Descriptor;

/**
 * @author tarlisportela
 *
 */
public class IndexedLoader<T extends MAT<?>> extends LoaderAdapter<T> {

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
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public Aspect<?> instantiateAspect(AttributeDescriptor attr, String value) {
		switch (attr.getType()) {
			case "numeric":
				return new InterningAspect<Double>(Double.parseDouble(value));
			case "space2d":
				return new Space2DAspect(value);
			case "time":
				return new InterningAspect<Integer>(Integer.parseInt(value));
			case "datetime":
				try {
					return new InterningAspect<Date>(formatter.parse(value));
				} catch (ParseException e) {
					Mov3letsUtils.trace("\tAtribute datetime '"+value+"' in wrong format, must be yyyy-MM-dd HH:mm:ss");
					return new InterningAspect<Date>(new Date());
				}
			case "localdate":
				return new InterningAspect<LocalDate>(LocalDate.parse(value));
			case "localtime":
				return new InterningAspect<LocalTime>(LocalTime.parse(value));
			case "foursquarevenue":
			case "gowallacheckin":
			case "nominal":
			default:
				return new InterningAspect<String>(value);
		}
	}

}
