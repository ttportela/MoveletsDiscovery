/**
 * 
 */
package br.com.tarlis.mov3lets.method.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

/**
 * @author tarlis
 *
 */
public class CSVOutputter<MO> extends OutputterAdapter<MO> {
	
	/**
	 * @param filePath
	 */
	public CSVOutputter(String filePath, Descriptor descriptor) {
		super(filePath, descriptor);
	}

	@Override
	public void write(List<MAT<MO>> trajectories, List<Subtrajectory> movelets) {
		BufferedWriter writer;

		try {
			File file = new File(getFilePath() + "train.csv");
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file));

			// TODO Features?
//			String header = (!trajectories.get(0).getFeatures().keySet().isEmpty()) ? 
//					trajectories.get(0).getFeatures().keySet().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
			String header = "";
			header += (!trajectories.get(0).getAttributes().keySet().isEmpty()) ?
					trajectories.get(0).getAttributes().keySet().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : ""; 
			
			header += "class" + System.getProperty("line.separator");
			
			writer.write(header);
			
			for (MAT<MO> trajectory : trajectories) {
				String line = "";
				// TODO Features?
//				String line = (!trajectory.getFeatures().values().isEmpty()) ?
//								trajectory.getFeatures().values().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
				line += (!trajectory.getAttributes().values().isEmpty()) ?
						trajectory.getAttributes().values().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
				line += "\"" + trajectory.getMovingObject() + "\""+ System.getProperty("line.separator");
				
				writer.write(line);
			}

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
