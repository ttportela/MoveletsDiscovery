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

import br.com.tarlis.mov3lets.method.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Point;
import br.com.tarlis.mov3lets.model.aspect.Aspect;
import br.com.tarlis.mov3lets.model.aspect.Space2DAspect;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;

public class DefaultLoader<T extends MAT<?>> extends LoaderAdapter<T> {

	@Override
	public List<T> loadTrajectories(String file, Descriptor descriptor) throws IOException {
		
		List<MAT<String>> trajectories = new ArrayList<MAT<String>>();
		// IF MO type is String:
//		MO mo = new MO();
		MAT<String> mat = new MAT<String>();

		file += ".csv";
		CSVParser csvParser = CSVFormat.DEFAULT.parse(new InputStreamReader((new FileInputStream(file))));
		csvParser.iterator().next();
		for (CSVRecord line : csvParser) {
			int tid = Integer.parseInt(line.get(descriptor.getIdFeature().getOrder()-1));
			
			if (mat.getTid() != tid) {
				mat = new MAT<String>();
				mat.setTid(tid);
				trajectories.add(mat);

				// Can use like this:
//				mo = (MO) new MovingObject<String>(label);
				// OR -- this for typing String:
				String label = line.get(descriptor.getLabelFeature().getOrder()-1);
				mat.setMovingObject(label);
			}
			
			// For each attribute of POI
			Point poi = new Point();	
			poi.setTrajectory(mat);
			for (AttributeDescriptor attr : descriptor.getAttributes()) {
//				poi.getAspects().put(attr.getText(), instantiateAspect(attr, line.get(attr.getOrder()-1)));
				poi.getAspects().add(instantiateAspect(attr, line.get(attr.getOrder()-1)));
			}
			mat.getPoints().add(poi);
		}
		csvParser.close();

		return (List<T>) trajectories;
	}
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public Aspect<?> instantiateAspect(AttributeDescriptor attr, String value) {
		Aspect<?> val = null;
		switch (attr.getType()) {
			case "numeric":
				return new Aspect<Double>(Double.parseDouble(value));
			case "space2d":
				return new Space2DAspect(value);
			case "time":
				return new Aspect<Integer>(Integer.parseInt(value));
			case "datetime":
				try {
					return new Aspect<Date>(formatter.parse(value));
				} catch (ParseException e) {
					Mov3letsUtils.trace("\tAtribute datetime '"+value+"' in wrong format, must be yyyy-MM-dd HH:mm:ss");
					return new Aspect<Date>(new Date());
				}
			case "localdate":
				return new Aspect<LocalDate>(LocalDate.parse(value));
			case "localtime":
				return new Aspect<LocalTime>(LocalTime.parse(value));
			case "foursquarevenue":
			case "gowallacheckin":
			case "nominal":
			default:
				return new Aspect<String>(value);
		}
	}

}
