package br.ufsc.mov3lets.method.loader;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.aspect.Aspect;
import br.ufsc.mov3lets.model.aspect.Space2DAspect;
import br.ufsc.mov3lets.model.aspect.Space3DAspect;
import br.ufsc.mov3lets.utils.Mov3letsUtils;

/**
 * Class LoaderAdapter.
 *
 * @author Tarlis Portela <tarlis [at] tarlis.com.br>
 * @param <T> the generic type
 * @created 2020-07-01
 */
public interface LoaderAdapter<T extends MAT<?>> {
	
	/**
	 * Mehod loadTrajectories. 
	 * LoaderAdapter: List<T>.
	 *
	 * @param file the file
	 * @param descriptor the descriptor
	 * @return the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract List<T> loadTrajectories(String file, Descriptor descriptor) throws IOException;
	
	public static String getCurPath(Descriptor descriptor) {
		return descriptor.hasParam("curpath")? descriptor.getParamAsText("curpath") : "./";
	}
	
	public static String getFileName(String fileName, Descriptor descriptor) {
		return Paths.get(getCurPath(descriptor), 
					(descriptor.hasParam("input_file_prefix")? descriptor.getParamAsText("input_file_prefix") + "_" : "") + fileName
				).toString();
	}
	
	/**
	 * Load.
	 *
	 * @param file the file
	 * @param descriptor the descriptor
	 * @return the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public default List<T> load(String file, Descriptor descriptor) throws IOException {
		return loadTrajectories(getFileName(file, descriptor), descriptor);
	}
	
	/** The date formatter. */
	SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * Instantiate aspect.
	 *
	 * @param attr the attr
	 * @param value the value
	 * @return the aspect
	 */
	public default Aspect<?> instantiateAspect(AttributeDescriptor attr, String value) {

		value = value.trim();
		
		switch (attr.getType()) {
			case "numeric":
				if (isMissing(value)) return new Aspect<Double>(null);
				return new Aspect<Double>(Double.parseDouble(value));
			case "space2d":
			case "composite_space2d":
			case "composite2_space2d":
				if (isMissing(value)) return new Space2DAspect(null);
				return new Space2DAspect(value);
			case "space3d":
			case "composite3_space3d":
				if (isMissing(value)) return new Space3DAspect(null);
				return new Space3DAspect(value);
			case "time":
				if (isMissing(value)) return new Aspect<Integer>(null);
				return new Aspect<Integer>(Integer.parseInt(value));
			case "datetime":
				if (isMissing(value)) return new Aspect<Date>(null);
				try {
//					SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					return new Aspect<Date>(dateFormatter.parse(value));
				} catch (ParseException e) {
					Mov3letsUtils.trace("\tAtribute datetime '"+value+"' in wrong format, must be yyyy-MM-dd HH:mm:ss");
					return new Aspect<Date>(new Date());
				}
			case "localdate":
				if (isMissing(value)) return new Aspect<LocalDate>(null);
				return new Aspect<LocalDate>(LocalDate.parse(value));
			case "localtime":
				if (isMissing(value)) return new Aspect<LocalTime>(null);
				return new Aspect<LocalTime>(LocalTime.parse(value));
			case "foursquarevenue":
			case "gowallacheckin":
			case "text":
			case "nominal":
			default:
				if (isMissing(value)) return new Aspect<String>(null);
				return new Aspect<String>(value);
		}
	}
	
	public default boolean isMissing(String value) {
		return "?".equalsIgnoreCase(value) || "NaN".equalsIgnoreCase(value) || "".equalsIgnoreCase(value);
	}

}
