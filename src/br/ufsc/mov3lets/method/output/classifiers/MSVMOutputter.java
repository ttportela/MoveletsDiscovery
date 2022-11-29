/**
 * 
 */
package br.ufsc.mov3lets.method.output.classifiers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

import br.ufsc.mov3lets.method.output.CSVOutputter;
import br.ufsc.mov3lets.method.output.OutputterAdapter;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.Feature;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.utils.Mov3letsUtils;

/**
 * The Class MSVMOutputter.
 * This is Movelet Support Vector Machines Classifier. 
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class MSVMOutputter<MO> extends CSVOutputter<MO> {

	// This is a singleton outputter:
	private static MSVMOutputter<?> instance = null;
	public static <MO> OutputterAdapter getInstance(String resultDirPath, Descriptor descriptor) {
		if (instance == null)
			instance = new MSVMOutputter<MO>(resultDirPath, null, descriptor);
		return instance;
	}

	/** The movelets. */
//	protected List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();
	
	/** The number of movelets. */
	protected Map<String, Double> classCount = new HashMap<String,Double>();
	
	/**
	 * Instantiates a new MSVM outputter.
	 *
	 * @param filePath the file path
	 * @param descriptor the descriptor
	 * @param subfolderClasses the subfolder classes
	 */
	public MSVMOutputter(String filePath, String movingObjectName, Descriptor descriptor, boolean subfolderClasses) {
		super(filePath, movingObjectName, descriptor, false);
	}
	
	/**
	 * Instantiates a new MSVM outputter.
	 *
	 * @param descriptor the descriptor
	 */
	public MSVMOutputter(Descriptor descriptor) {
		super(descriptor);
		this.subfolderClasses = false;
	}

	/**
	 * Instantiates a new MSVM outputter.
	 *
	 * @param resultDirPath the result dir path
	 * @param descriptor the descriptor
	 */
	public MSVMOutputter(String resultDirPath, String movingObjectName, Descriptor descriptor) {
		super(resultDirPath, movingObjectName, descriptor, false);
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
	public synchronized void write(String filename, List<MAT<MO>> trajectories, List<Feature> movelets, 
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
		
		BufferedWriter writer;

		try {
			File file = getFile(getMovingObject(), filename + "-MSVM_results.csv");
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file));

			String header = "";
			header += (!attributeToTrajectories.get(0).keySet().isEmpty()) ?
					attributeToTrajectories.get(0).keySet().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : ""; 
			header += "predict_class,class" + System.getProperty("line.separator");
			
			writer.write(header);
			
			double TP = 0.0, TN = 0.0;
			
			for (int i = 0; i < trajectories.size(); i++) {
				Map<String,Double> attributes = attributeToTrajectories.get(i);
				String line = "";
				
				// BEFORE:
//				line += (!attributes.values().isEmpty()) ?
//						attributes.values().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
//				Entry<String, Double> min = Collections.min(attributes.entrySet(), new Comparator<Entry<String, Double>>() {
//				    public int compare(Entry<String, Double> entry1, Entry<String, Double> entry2) {
//				        return entry1.getValue().compareTo(entry2.getValue());
//				    }
//				});

//				String predict_class = min.getKey().substring(6);
				
				String predict_class = "-";
				double predict = Double.MAX_VALUE;
				// Pre-process:
				for (Entry<String, Double> e : attributes.entrySet()) {
					String key = e.getKey().substring(6);
					double value = classCount.get(key);
					value = e.getValue() / value;
					line += String.valueOf(value) + ",";
					
					// Prediction:
					if (value < predict) {
						predict = value;
						predict_class = key;
					}
				}
				
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
					"\nMSVM results for " + filename + ". " +
					"Correctly labeled: " + TP + ". " +
					"Wrongly labeled: " + TN + ". " +
					"Accuracy: " + (TP / (TP+TN)) + ". "
			);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Attribute to trajectories Discrete.
	 *
	 * @param trajectories the trajectories
	 * @param movelet the movelet
	 * @param attributeToTrajectories the attribute to trajectories
	 */
	protected
	void attributeToTrajectoriesDiscrete(List<MAT<MO>> trajectories, Feature movelet, List<Map<String, Double>> attributeToTrajectories) {
//		String attributeName =  "sh_TID" + movelet.getTrajectory().getTid() + 
//								"_START" + movelet.getStart() + 
//								"_SIZE" + movelet.getSize() + 
//								"_CLASS" + movelet.getTrajectory().getMovingObject();
		String attributeName =  "CLASS_" + movelet.getTrajectory().getMovingObject();

		double[][] distances = movelet.getDistances();		
		RealMatrix rm = new Array2DRowRealMatrix(distances);
		Map<String,double[]> splitpointsData = movelet.getSplitpointData();		
		
		Pair<double[], double[]> splitpointLimits = fillSplitPointsLimits(splitpointsData,medium);
		double[] splitpointsLI = splitpointLimits.getFirst(); // Limite inferior
		double[] splitpointsLS = splitpointLimits.getSecond(); // Limite superior
        
		for (int i = 0; i <  trajectories.size(); i++) {
			double distance;
			if (isCovered(rm.getColumn(i), splitpointsLI))
				distance = 0;
			else if (isCovered(rm.getColumn(i), splitpointsLS))
				distance = 1;
			else 
				distance = 2;

			putAttributes(i, attributeToTrajectories, attributeName, distance);
			putCount(movelet.getTrajectory().getMovingObject().toString());
		}
	}

	/**
	 * Attribute to trajectories Numeric.
	 *
	 * @param trajectories the trajectories
	 * @param movelet the movelet
	 * @param attributeToTrajectories the attribute to trajectories
	 */
	protected void attributeToTrajectoriesNumeric(List<MAT<MO>> trajectories, Feature movelet, List<Map<String, Double>> attributeToTrajectories) {
//		String attributeName =  "sh_TID" + movelet.getTrajectory().getTid() + 
//								"_START" + movelet.getStart() + 
//								"_SIZE" + movelet.getSize() + 
//								"_CLASS" + movelet.getTrajectory().getMovingObject();
		String attributeName =  "CLASS_" + movelet.getTrajectory().getMovingObject();

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
			
			putAttributes(i, attributeToTrajectories, attributeName, distance);
			putCount(movelet.getTrajectory().getMovingObject().toString());
		}
	}

	protected void putAttributes(int idxTrajectory, List<Map<String, Double>> attributeToTrajectories, 
			String attributeName, double distance) {
		Map<String, Double> atributes = getAttributes(idxTrajectory, attributeToTrajectories);
		distance += atributes.getOrDefault(attributeName, 0.0);
		atributes.put(attributeName, distance);
	}

	protected void putCount(String className) {
		double count = classCount.getOrDefault(className, 0.0);
		classCount.put(className, count+1.0);
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
