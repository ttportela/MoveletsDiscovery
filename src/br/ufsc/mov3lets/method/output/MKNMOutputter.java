/**
 * 
 */
package br.ufsc.mov3lets.method.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;
import br.ufsc.mov3lets.utils.Mov3letsUtils;

/**
 * The Class MSVMOutputter.
 * This is Movelet Support Vector Machines Classifier. 
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class MKNMOutputter<MO> extends CSVOutputter<MO> {

	// This is a singleton outputter:
	private static MKNMOutputter<?> instance = null;
	public static <MO> OutputterAdapter getInstance(String resultDirPath, Descriptor descriptor) {
		if (instance == null)
			instance = new MKNMOutputter<MO>(resultDirPath, null, descriptor);
		return instance;
	}

	/** The K. */
	protected int K = 5;
	
//	/** The number of movelets. */
//	protected Map<String, Double> classCount = new HashMap<String,Double>();
	
	/**
	 * Instantiates a new MSVM outputter.
	 *
	 * @param filePath the file path
	 * @param descriptor the descriptor
	 * @param subfolderClasses the subfolder classes
	 */
	public MKNMOutputter(String filePath, String movingObjectName, Descriptor descriptor, boolean subfolderClasses) {
		super(filePath, movingObjectName, descriptor, false);
	}
	
	/**
	 * Instantiates a new MSVM outputter.
	 *
	 * @param descriptor the descriptor
	 */
	public MKNMOutputter(Descriptor descriptor) {
		super(descriptor);
		this.subfolderClasses = false;
	}

	/**
	 * Instantiates a new MSVM outputter.
	 *
	 * @param resultDirPath the result dir path
	 * @param descriptor the descriptor
	 */
	public MKNMOutputter(String resultDirPath, String movingObjectName, Descriptor descriptor) {
		super(resultDirPath, movingObjectName, descriptor, false);
	}
	
//	@Override
//	public void init() {
//		super.init();
//		K = getDescriptor().hasParam("mknn_k")? getDescriptor().getParamAsInt("mknn_k") : K;
//	}

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
	public synchronized void write(String filename, List<MAT<MO>> trajectories, List<Subtrajectory> movelets, 
			boolean delayOutput, Object... params) {
		List<Map<String, Double>> attributeToTrajectories = 
				"train".equals(filename)? attributesToTrain : attributesToTest;
		
//		if (delayOutput) {
			attributesToTrajectories(trajectories, movelets, attributeToTrajectories);
			
			decreaseDelayCount(filename);
			if (delayCount > 0)
				return;
//		}
		
		if (attributeToTrajectories.isEmpty()) {
//			Mov3letsUtils.traceW("Empty movelets set for class "+getMovingObject()+" [NOT OUTPUTTED]");
			return;
		}
		
		if (getDescriptor().hasParam("mknn_k")) {
			K = getDescriptor().getParamAsInt("mknn_k");				
		} else {
			// Otherwise is the mean number of movelets per class:
//			K = attributeToTrajectories.get(0).keySet().size() / 2;
			int ccount = (int) trajectories.stream().map(e -> (MO) e.getMovingObject()).distinct().count();
			K = (int) ((attributeToTrajectories.get(0).keySet().size() / ccount) * 1.3);
			K = (K % 2) == 0? K+1 : K;
		}
		
		BufferedWriter writer;

		try {
			File file = getFile(getMovingObject(), filename + "-MKNM_prediction.csv");
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file));

//			String header = "";
//			header += (!attributeToTrajectories.get(0).keySet().isEmpty()) ?
//					attributeToTrajectories.get(0).keySet().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : ""; 
			String header = "k_"+K+"_proportion,predict_class,class" + System.getProperty("line.separator");
			
			writer.write(header);

			double TP = 0.0, TN = 0.0;
			
			for (int i = 0; i < trajectories.size(); i++) {
				Map<String,Double> attributes = attributeToTrajectories.get(i);
				String line = "";
				
				List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(attributes.entrySet());
				Collections.sort(list, new Comparator<Entry<String, Double>>() {
				    public int compare(Entry<String, Double> entry1, Entry<String, Double> entry2) {
				        return entry1.getValue().compareTo(entry2.getValue());
				    }
				});
				
				Map<String, Double> classCount = new HashMap<String,Double>();
				for (int j = 0; j < K; j++) {
					String key = list.get(j).getKey().split("_")[4].substring(5);
					double count = classCount.getOrDefault(key, 0.0);
					classCount.put(key, count+1.0);
				}

				Entry<String, Double> max = Collections.max(classCount.entrySet(), new Comparator<Entry<String, Double>>() {
				    public int compare(Entry<String, Double> entry1, Entry<String, Double> entry2) {
				        return entry1.getValue().compareTo(entry2.getValue());
				    }
				});
				String predict_class = max.getKey();
				double predict = max.getValue() / ((double)K);
				
				line += String.valueOf(predict) + ",";				
				line += "\"" + predict_class + "\",";
				line += "\"" + trajectories.get(i).getMovingObject() + "\""+ System.getProperty("line.separator");
				
				if (predict_class.equalsIgnoreCase(trajectories.get(i).getMovingObject().toString()))
					TP++;
				else 
					TN++;
				
				writer.write(line);
			}

			writer.close();
			attributeToTrajectories.clear();
			
			Mov3letsUtils.trace(
					"\nMKNM results for " + filename + ". K: " + K + ". " +
					"Correctly labeled: " + TP + ". " +
					"Wrongly labeled: " + TN + ". " +
					"Accuracy: " + (TP / (TP+TN)) + ". "
			);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Attribute to trajectories Numeric.
	 *
	 * @param trajectories the trajectories
	 * @param movelet the movelet
	 * @param attributeToTrajectories the attribute to trajectories
	 */
	protected void attributeToTrajectoriesNumeric(List<MAT<MO>> trajectories, Subtrajectory movelet, List<Map<String, Double>> attributeToTrajectories) {
		String attributeName =  "sh_TID" + movelet.getTrajectory().getTid() + 
								"_START" + movelet.getStart() + 
								"_SIZE" + movelet.getSize() + 
								"_CLASS" + movelet.getTrajectory().getMovingObject();

		RealMatrix rm = new Array2DRowRealMatrix(movelet.getDistances());
				
		double[] splitpoints = movelet.getSplitpoints();
		double[] maxDistances = movelet.getMaxDistances();
				
		double distance;
		
		for (int i = 0; i < trajectories.size(); i++) {
			
			if (isCovered(rm.getColumn(i), splitpoints)){
				distance = normalizeCovered(rm.getColumn(i), splitpoints, maxDistances);
			} else {
				distance = normalizeNonCovered(rm.getColumn(i), splitpoints, maxDistances);
			}
			
//			distance /= movelet.getQuality().getData().get("quality");
				
			getAttributes(i, attributeToTrajectories).put(attributeName, distance);
		}
	}
	
	/**
	 * Setter for delayCount.
	 * 
	 * @param delayCount the delayCount to set (as int instance).
	 */
	public void setDelay(int delayCount) {
		this.delayCount += delayCount;
	}

}
