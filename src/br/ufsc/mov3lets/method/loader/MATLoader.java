package br.ufsc.mov3lets.method.loader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.util.Pair;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;

/**
 * The Class CSVLoader.
 *
 * @param <T> the generic type
 */
public class MATLoader<T extends MAT<?>> implements LoaderAdapter<T> {

    public final String PROBLEM_NAME = "@problemName";
    public final String ASPECT_NAMES = "@aspectNames";
    public final String TRAJECTORY_NAMES = "@trajectoryAspectNames";
    public final String ASPECT_DESC = "@aspectDescriptor";
    public final String DATA = "@data";
    
    public final String TRAJECTORY = "@trajectory";
    public final String TRAJECTORY_DATA = "@trajectoryAspects";
    public final String POINT_DATA = "@trajectoryPoints";
    
	protected HashMap<String, String> variables;
	
	protected List<AttributeDescriptor> trajAttributes;
	protected List<AttributeDescriptor> pointAttributes;

	/**
	 * Overridden method.
	 * 
	 * @see br.com.tarlis.mov3lets.method.loader.LoaderAdapter#loadTrajectories(java.lang.String,
	 *      br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor).
	 * 
	 * @param file
	 * @param descriptor
	 * @return
	 * @throws IOException
	 */
	@Override
	public List<T> loadTrajectories(String file, Descriptor descriptor) throws IOException {

		List<MAT<String>> trajectories = new ArrayList<MAT<String>>();

		file += ".mat";
		// Open the file
		FileInputStream fstream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		// Read HEADER Line By Line
		variables = readHeader(br);
		
		// Prepare descriptors:
		if (descriptor.notSet()) {
			readDescriptors();
		} else {
			pointAttributes = descriptor.getAttributes();
			trajAttributes = descriptor.getTrajectoryAttributes();
		}
		
		if (variables.containsKey(PROBLEM_NAME)) {
			descriptor.setParam("problemName", variables.get(PROBLEM_NAME));
		}
		
		// Read TRAJECTORY Line By Line
		String line;
		if ((line = br.readLine()) != null && line.startsWith(TRAJECTORY)) {
			Pair<MAT, String> o;
			do {
				o = readMAT(br);
				trajectories.add(o.getFirst());
			} while (o.getSecond() != null && o.getSecond().startsWith(TRAJECTORY));
		}

		// Close the input stream
		fstream.close();

		return (List<T>) trajectories;
	}

	public HashMap<String, String> readHeader(BufferedReader br) throws IOException {
		HashMap<String, String> variables = new HashMap<String, String>();
		
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#"))
				continue;
			else if (line.startsWith(DATA))
				break;
			else {
				String[] tokens = line.split(" ", 2);
				variables.put(tokens[0], tokens[1]);
			}
		}
		
		return variables;
	}

	public Pair<MAT, String> readMAT(BufferedReader br) throws IOException {

		String line, next = null;
		if ((line = br.readLine()) != null) {
			CSVParser csvParser = CSVParser.parse(line, CSVFormat.DEFAULT);
			CSVRecord rect = csvParser.iterator().next();
			
			// IF MO type is String:
//			MO mo = new MO();
			MAT<String> mat = new MAT<String>();
			mat.setTid(Integer.valueOf(rect.get(0)));

			// Can use like this:
//			mo = (MO) new MovingObject<String>(label);
			// OR -- this for typing String:
			if (rect.size() > 1)
				mat.setMovingObject(rect.get(1));
			
			// Read MAT data
			String lines = "";
			while ((line = br.readLine()) != null) {
				
				if (line.startsWith(TRAJECTORY_DATA))
					readMATAspects(br, mat);
				else if (line.startsWith(POINT_DATA))
					continue;
				else if (line.startsWith(TRAJECTORY)) {
					next = line;
					break;
				} else {
					lines += line + "\n";
				}
			}
			
			csvParser = CSVParser.parse(lines, CSVFormat.DEFAULT);
			for (CSVRecord recp : csvParser) {
				// For each attribute of POI
				Point poi = new Point();
				poi.setTrajectory(mat);
				readAspects(recp, poi.getAspects(), pointAttributes);
				mat.getPoints().add(poi);	
			}
			
			return new Pair<MAT, String>(mat, next);
			
		} else {
			throw new RuntimeException("Invalid format while reading .MAT file at " +TRAJECTORY);
		}
	}

	protected void readDescriptors() {
		pointAttributes = new ArrayList<AttributeDescriptor>();
		trajAttributes = new ArrayList<AttributeDescriptor>();
		
		if (variables.containsKey(ASPECT_NAMES)) {
			
			HashMap<String, String> fields;
			if (variables.containsKey(ASPECT_DESC))
				fields = (HashMap<String, String>) Arrays.asList(variables.get(ASPECT_DESC).split(","))
					.stream().map(s -> s.split(":")).collect(Collectors.toMap(e -> e[0], e -> e[1]));
			else
				fields = new HashMap<String, String>();
			
			if (variables.containsKey(TRAJECTORY_NAMES)) {
				List<String> trajAspectNames = (List<String>) Arrays.asList(variables.get(TRAJECTORY_NAMES).split(","));
				for (int i = 0; i < trajAspectNames.size(); i++) {
					String name = trajAspectNames.get(i);
					trajAttributes.add(instantiateAttributeDesc(i+1, name, fields.getOrDefault(name, "nominal")));
				}
			}
			
			List<String> aspectNames = (List<String>) Arrays.asList(variables.get(ASPECT_NAMES).split(","));
			for (int i = 0; i < aspectNames.size(); i++) {
				String name = aspectNames.get(i);
				pointAttributes.add(instantiateAttributeDesc(i+1, name, fields.getOrDefault(name, "nominal")));
			}
			
			
		} else {
			throw new RuntimeException("Invalid format while reading .MAT file at " +ASPECT_NAMES +
					". Either inform the aspect names or provide a descriptor file");
		}
	}

	protected AttributeDescriptor instantiateAttributeDesc(int order, String name, String type) {
		
		String comparator = "equals";
		switch (type) {
		case "numeric":
		case "time":
		case "datetime":
		case "localdate":
		case "localtime":
			comparator = "difference";
			break;
		case "space2d":
		case "composite_space2d":
		case "composite2_space2d":
		case "composite3_space3d":
			comparator = "euclidean";
			break;
		case "nominalday":
			comparator = "weekday";
			break;
		case "nominal":
		default:
			comparator = "equalsignorecase";
		}
		
		return new AttributeDescriptor(
				order, type, name, comparator, -1.0);
	}

	protected void readMATAspects(BufferedReader br, MAT<String> mat) throws IOException {
		String line;
		if ((line = br.readLine()) != null) {
			CSVParser csvParser = CSVParser.parse(line, CSVFormat.DEFAULT);
			CSVRecord rec = csvParser.iterator().next();
			
			mat.setAspects(new ArrayList<Aspect<?>>());
			readAspects(rec, mat.getAspects(), trajAttributes);
		}
	}

	protected void readAspects(CSVRecord rec, List<Aspect<?>> list, List<AttributeDescriptor> attributes) {
		for (AttributeDescriptor attr : attributes) {
			
			if (attr.getType().startsWith("composite_") || attr.getType().startsWith("composite2_")) {
				String value = rec.get(attr.getOrder() - 1) + " " + rec.get(attr.getOrder());
				list.add(instantiateAspect(attr, value));
				
			} else if (attr.getType().startsWith("composite3_")) {
				String value = rec.get(attr.getOrder() - 1) + " " + rec.get(attr.getOrder()) + " "
						+ rec.get(attr.getOrder() + 1);
				list.add(instantiateAspect(attr, value));
				
			} else
				list.add(instantiateAspect(attr, rec.get(attr.getOrder() - 1)));
			
		}
	}
	
	public List<AttributeDescriptor> getAttributes() {
		return pointAttributes;
	}
	
	public List<AttributeDescriptor> getTrajectoryAttributes() {
		return trajAttributes;
	}

}
