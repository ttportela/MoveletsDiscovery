/**
 * 
 */
package br.ufsc.mov3lets.method.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.method.structures.indexed.IndexPoint;
import br.ufsc.mov3lets.model.MAT;

/**
 * The Class CSVOutputter.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class CSVIndexOutputter<MO> extends OutputterAdapter<MO,List<IndexPoint>> {

	/** The output. */
	protected String output = "numeric"; // Other values: normalized?? and discrete
	
	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param filePath the file path
	 * @param descriptor the descriptor
	 * @param subfolderClasses the subfolder classes
	 */
	public CSVIndexOutputter(String filePath, String movingObjectName, Descriptor descriptor, boolean subfolderClasses) {
		super(filePath, movingObjectName, descriptor, subfolderClasses);
		init();
	}
	
	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param descriptor the descriptor
	 */
	public CSVIndexOutputter(Descriptor descriptor) {
		super(descriptor);
		init();
	}

	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param resultDirPath the result dir path
	 * @param descriptor the descriptor
	 */
	public CSVIndexOutputter(String resultDirPath, String movingObjectName, Descriptor descriptor) {
		super(resultDirPath, movingObjectName, descriptor, true);
		init();
	}
	
	public void init() {
		output = getDescriptor().hasParam("output")? getDescriptor().getParamAsText("output") : output;
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
	public void write(String filename, List<MAT<MO>> trajectories, List<IndexPoint> data, boolean delayOutput, Object... params) {
		BufferedWriter writer;

		int startId = (int) params[0];
		Map<String, BitSet> mMATIndex = (Map<String, BitSet>) params[1];
		
		try {
			File file = getFile(getMovingObject(), filename + ".csv");
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file));
			
			String header = "";
			long k = 1;
			for (Entry<String, BitSet> e : mMATIndex.entrySet()) {
				header += (k++) + ",";
			}
			header += "class" + System.getProperty("line.separator");
			writer.write(header);
			
			for (int i = 0; i < trajectories.size(); i++) {
				String line = "";
				for (Entry<String, BitSet> e : mMATIndex.entrySet()) {
					line += (e.getValue().get(i+startId)? 2.0 : 0.0) + ",";
				}
				writer.write(line + "\"" + trajectories.get(i).getMovingObject() + "\"" + System.getProperty("line.separator"));
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (startId == 0)
			writeKeys(mMATIndex);
	}
	
	protected void writeKeys(Map<String, BitSet> mMATIndex) {
		try {
			File file = getFile(getMovingObject(), "featuresOnTrain.csv");
			file.getParentFile().mkdirs();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			long k = 1;
			for (Entry<String, BitSet> e : mMATIndex.entrySet()) {
				writer.write((k++) + "," + e.getKey() + System.getProperty("line.separator"));
			}
			
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write_BKP(String filename, List<MAT<MO>> trajectories, List<IndexPoint> data, boolean delayOutput, Object... params) {
		BufferedWriter writer;

		int startId = (int) params[0];
		Map<Integer, int[]> mMATIndex = (Map<Integer, int[]>) params[1];
		
		try {
			File file = getFile(getMovingObject(), filename + ".csv");
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file));
			
			String header = "";
			for (Entry<Integer, int[]> e : mMATIndex.entrySet()) {
				header += e.getKey() + ",";
			}
			header += "class" + System.getProperty("line.separator");
			writer.write(header);
			
			for (int i = 0; i < trajectories.size(); i++) {
				String line = "";
				for (Entry<Integer, int[]> e : mMATIndex.entrySet()) {
					line += discretize(e.getValue()[i+startId]) + ",";
				}
				writer.write(line + "\"" + trajectories.get(i).getMovingObject() + "\"" + System.getProperty("line.separator"));
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public void write_BKP(String filename, List<MAT<MO>> trajectories, List<IndexPoint> data, boolean delayOutput, Object... params) {
//		BufferedWriter writer;
//
//		int startId = (int) params[0];
////		int endId   = data.size()-1;
//		Map<Integer, int[]> mMATIndex = (Map<Integer, int[]>) params[1];
//		
//		try {
//			File file = getFile(getMovingObject(), filename + ".csv");
//			file.getParentFile().mkdirs();
//			writer = new BufferedWriter(new FileWriter(file));
//
////			Mov3letsUtils.getInstance().startTimer("TESTE-1");
////			String header = "";
////			String[] content = new String[trajectories.size()];
////			for (Entry<Integer, int[]> e : mMATIndex.entrySet()) {
////				if (e.getKey() > endId) continue;
//////			for (IndexPoint p : data) {
////				header += e.getKey() + ",";
////				
////				int[] matches = e.getValue();
////				for (int i = 0; i < trajectories.size(); i++) {
////					if (content[i] == null) content[i] = "";
////					content[i] += discretize(matches[i+startId]) + ",";
////				}
////			}			
////			header += "class" + System.getProperty("line.separator");
////			
////			writer.write(header);
////			
////			for (int i = 0; i < trajectories.size(); i++) {
////				writer.write(content[i] + "\"" + trajectories.get(i).getMovingObject() + "\"" + System.getProperty("line.separator"));
////			}
////			Mov3letsUtils.getInstance().stopTimer("TESTE-1");
//			
//			String header = "";
//			for (Entry<Integer, int[]> e : mMATIndex.entrySet()) {
////				if (e.getKey() > endId) continue;
//				header += e.getKey() + ",";
//			}
//			header += "class" + System.getProperty("line.separator");
//			writer.write(header);
//			
//			for (int i = 0; i < trajectories.size(); i++) {
//				String line = "";
//				for (Entry<Integer, int[]> e : mMATIndex.entrySet()) {
////					if (e.getKey() > endId) continue;
//					line += discretize(e.getValue()[i+startId]) + ",";
//				}
//				writer.write(line + "\"" + trajectories.get(i).getMovingObject() + "\"" + System.getProperty("line.separator"));
//			}
//
//			writer.close();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	public double discretize(int value) {
		switch (output){
			case "discrete" :
				if (value >= getDescriptor().getAttributes().size()) {
					return 2.0;
//				} else if (value >= (getDescriptor().getAttributes().size()/2)) {
//					return 1.0;
				} else {
					return 0.0;
				}

			case "numeric" :
			default:
				return value;
			
		}
	}

}
