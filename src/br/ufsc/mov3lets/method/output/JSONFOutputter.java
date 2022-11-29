/**
 * 
 */
package br.ufsc.mov3lets.method.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.ufsc.mov3lets.method.output.json.SubtrajectoryGSON;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.Feature;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class JSONOutputter.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class JSONFOutputter<MO> extends FeatureOutputter<MO> {
	
	protected FileWriter fileWriter;
	protected Gson gson;

	/**
	 * Instantiates a new JSON outputter.
	 *
	 * @param filePath the file path
	 * @param descriptor the descriptor
	 * @param subfolderClasses the subfolder classes
	 */
	public JSONFOutputter(String filePath, String movingObjectName, Descriptor descriptor, boolean subfolderClasses) {
		super(filePath, movingObjectName, descriptor, subfolderClasses);
	}

	/**
	 * Instantiates a new JSON outputter.
	 *
	 * @param descriptor the descriptor
	 */
	public JSONFOutputter(Descriptor descriptor) {
		super(descriptor);
	}

	/**
	 * Instantiates a new JSON outputter.
	 *
	 * @param resultDirPath the result dir path
	 * @param descriptor the descriptor
	 */
	public JSONFOutputter(String resultDirPath, String movingObjectName, Descriptor descriptor) {
		super(resultDirPath, movingObjectName, descriptor, true);
	}

	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.output.OutputterAdapter#write(java.lang.String, java.util.List, java.util.List, boolean).
	 * 
	 * @param filename
	 * @param trajectories
	 * @param movelets
	 * @param delayOutput
	 */
	@Override
	public synchronized void write(String filename, List<MAT<MO>> trajectories, List<Feature> moveletsToAdd, 
			boolean delayOutput, Object... params) {

		if ("test".equals(filename))
			return;
		
		if (delayOutput) { // Do nothing
			decreaseDelayCount(filename);
			if (delayCount > 0)
				return;
		}
		
		try {
			
			fileWriter.write( "\n  ]\n}" );
			fileWriter.close();
			fileWriter = null;
			gson = null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.getLocalizedMessage();
			e.printStackTrace();
		}  
	}

	@Override
	public synchronized void writeMovelet(String filename, List<MAT<MO>> trajectories, Feature movelet, Object... params) {
		if ("test".equals(filename))
			return;

		
		try {
			String s = "";
			
			if (fileWriter == null) 
				init(filename, trajectories);
			else
				s = ",\n    " + s;
			
			s += gson.toJson( new SubtrajectoryGSON((Subtrajectory) movelet, getDescriptor()) ).replace("\n", "\n    ");
			
			fileWriter.write(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected synchronized void init(String filename, List<MAT<MO>> trajectories) {
		
		try {
			gson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting().create();
			
			File file = getFile(getMovingObject().toString(), 
					"moveletsOn"+ StringUtils.capitalize(filename) + ".json");
			file.getParentFile().mkdirs();
			
			fileWriter = new FileWriter(file);

			List<Map<String,Object>> classOfTrajectories = new ArrayList<>();
			for (MAT<MO> t : trajectories) {			
				Map<String, Object> classOfT = new HashMap<>();
				classOfT.put("tid", t.getTid());
				classOfT.put("label", t.getMovingObject());			
				classOfTrajectories.add(classOfT);
			}
			
			fileWriter.write("{\n  \"classes\": " + gson.toJson(classOfTrajectories) + ",\n  \"movelets\": [\n    ");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.getLocalizedMessage();
			e.printStackTrace();
		}  
	}

}
