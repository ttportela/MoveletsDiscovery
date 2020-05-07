package br.com.tarlis.mov3lets.method.loader;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import br.com.tarlis.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.aspect.Aspect;
import br.com.tarlis.mov3lets.model.aspect.Space2DAspect;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;

public abstract class LoaderAdapter<T extends MAT<?>> {
	
	public abstract List<T> loadTrajectories(String file, Descriptor descriptor) throws IOException;
	
	public List<T> load(String file, Descriptor descriptor) throws IOException {
		String curpath = descriptor.hasParam("curpath")? descriptor.getParamAsText("curpath") : "./";
		
		List<T> data = loadTrajectories(Paths.get(curpath, file).toString(), descriptor);
		return data;
	}
	
	protected SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public Aspect<?> instantiateAspect(AttributeDescriptor attr, String value) {

		switch (attr.getType()) {
			case "numeric":
				return new Aspect<Double>(Double.parseDouble(value));
			case "space2d":
				return new Space2DAspect(value);
			case "time":
				return new Aspect<Integer>(Integer.parseInt(value));
			case "datetime":
				try {
					return new Aspect<Date>(dateFormatter.parse(value));
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
