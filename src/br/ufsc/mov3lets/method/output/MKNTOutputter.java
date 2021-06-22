/**
 * 
 */
package br.ufsc.mov3lets.method.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

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
public class MKNTOutputter<MO> extends CSVOutputter<MO> {

	// This is a singleton outputter:
	private static MKNTOutputter<?> instance = null;
	public static <MO> OutputterAdapter getInstance(String resultDirPath, Descriptor descriptor) {
		if (instance == null)
			instance = new MKNTOutputter<MO>(resultDirPath, null, descriptor);
		return instance;
	}

	/** The K. */
	protected int K = 5;
	
	/** The train. */
	protected List<MAT<MO>> train = null;
	
	protected Map<Subtrajectory, Integer> mapMovelets = new HashMap<Subtrajectory, Integer>();
	
//	/** The test. */
//	private List<MAT<MO>> test = null;
	
//	/** The attributes to train. */
//	protected List<BitSet> attributesToTrain = new ArrayList<BitSet>();
//	
//	/** The attributes to test. */
//	protected List<BitSet> attributesToTest = new ArrayList<BitSet>();
//	
	/**
	 * Instantiates a new MSVM outputter.
	 *
	 * @param filePath the file path
	 * @param descriptor the descriptor
	 * @param subfolderClasses the subfolder classes
	 */
	public MKNTOutputter(String filePath, String movingObjectName, Descriptor descriptor, boolean subfolderClasses) {
		super(filePath, movingObjectName, descriptor, false);
	}
	
	/**
	 * Instantiates a new MSVM outputter.
	 *
	 * @param descriptor the descriptor
	 */
	public MKNTOutputter(Descriptor descriptor) {
		super(descriptor);
		this.subfolderClasses = false;
	}

	/**
	 * Instantiates a new MSVM outputter.
	 *
	 * @param resultDirPath the result dir path
	 * @param descriptor the descriptor
	 */
	public MKNTOutputter(String resultDirPath, String movingObjectName, Descriptor descriptor) {
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

	public void write(String filename, List<MAT<MO>> trajectories, List<Subtrajectory> movelets, 
			boolean delayOutput, Object... params) {
//		List<Map<String, Double>> attributeToTrajectories = 
//				"train".equals(filename)? attributesToTrain : attributesToTest;
		List<Map<String, Double>> attributeToTrajectories = null;
		if ("train".equals(filename)) {
			attributeToTrajectories = this.attributesToTrain;
			train = trajectories;
		} else {
			attributeToTrajectories = this.attributesToTest;
		}
		
//		attributesToSets(trajectories, movelets, attributeToTrajectories);
		attributesToTrajectories(trajectories, movelets, attributeToTrajectories);
		
		decreaseDelayCount(filename);
		if (delayCount > 0)
			return;
		
		if (attributeToTrajectories.isEmpty()) return;
		
		if (getDescriptor().hasParam("mknn_k")) {
			K = getDescriptor().getParamAsInt("mknn_k");				
		} // TODO Its fixed 5?
		else {
			// Otherwise is the mean number of trajectories per class:
			int ccount = (int) trajectories.stream().map(e -> (MO) e.getMovingObject()).distinct().count();
			K = (int) ((train.size() / ccount) * 1.3);
			K = (K % 2) == 0? K+1 : K;
		}
		
		BufferedWriter writer;

		try {
			File file = getFile(getMovingObject(), filename + "-MKNT_prediction.csv");
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file));

			String header = "k_"+K+"_proportion,predict_class,class" + System.getProperty("line.separator");
			
			writer.write(header);

			double TP = 0.0, TN = 0.0;
			
			for (int i = 0; i < trajectories.size(); i++) {
				Map<String,Double> attributes = attributeToTrajectories.get(i);
				
				List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(attributes.entrySet());
				Collections.sort(list, new Comparator<Entry<String, Double>>() {
				    public int compare(Entry<String, Double> entry1, Entry<String, Double> entry2) {
				        return entry1.getValue().compareTo(entry2.getValue());
				    }
				});
				
				Set<String> tids = new HashSet<String>();
				Map<String, Double> classCount = new HashMap<String,Double>();
				for (int j = 0; j < K; j++) {
					String tid = list.get(j).getKey().split("_")[1].substring(3);
					if (tids.contains(tid)) continue; tids.add(tid);
					
					String key = list.get(j).getKey().split("_")[4].substring(5);
					double count = classCount.getOrDefault(key, 0.0);
					classCount.put(key, count+1.0);
					
					if (tids.size() >= K) break;
				}
				
				Entry<String, Double> max = Collections.max(classCount.entrySet(), new Comparator<Entry<String, Double>>() {
				    public int compare(Entry<String, Double> entry1, Entry<String, Double> entry2) {
				        return entry1.getValue().compareTo(entry2.getValue());
				    }
				});
				String predict_class = max.getKey();
				double predict = max.getValue() / ((double)K);

				String line = "";
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
//			attributeToTrajectories.clear();
			
			Mov3letsUtils.trace(
					"\nMKNT results for " + filename + ". K: " + K + ". " +
					"Correctly labeled: " + TP + ". " +
					"Wrongly labeled: " + TN + ". " +
					"Accuracy: " + (TP / (TP+TN)) + ". "
			);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public void write_BKP(String filename, List<MAT<MO>> trajectories, List<Subtrajectory> movelets, 
//			boolean delayOutput, Object... params) {
////		List<Map<String, Double>> attributeToTrajectories = 
////				"train".equals(filename)? attributesToTrain : attributesToTest;
//		List<BitSet> attributeToTrajectories = null;
//		if ("train".equals(filename)) {
//			attributeToTrajectories = this.attributesToTrain;
//			train = trajectories;
//		} else {
//			attributeToTrajectories = this.attributesToTest;
//		}
//		
//		attributesToSets(trajectories, movelets, attributeToTrajectories);
//		
//		decreaseDelayCount(filename);
//		if (delayCount > 0)
//			return;
//		
//		if (attributeToTrajectories.isEmpty()) return;
//		
//		if (getDescriptor().hasParam("mknn_k")) {
//			K = getDescriptor().getParamAsInt("mknn_k");				
//		} // TODO Its fixed 5?
//		else {
//			// Otherwise is the mean number of trajectories per class:
//			int ccount = (int) trajectories.stream().map(e -> (MO) e.getMovingObject()).distinct().count();
//			K = (int) ((train.size() / ccount) * 1.3);
//			K = (K % 2) == 0? K+1 : K;
//		}
//		
//		BufferedWriter writer;
//
//		try {
//			File file = getFile(getMovingObject(), filename + "-MKNT_prediction.csv");
//			file.getParentFile().mkdirs();
//			writer = new BufferedWriter(new FileWriter(file));
//
//			String header = "k_"+K+"_proportion,predict_class,class" + System.getProperty("line.separator");
//			
//			writer.write(header);
//
//			double TP = 0.0, TN = 0.0;
//			
//			for (int i = 0; i < trajectories.size(); i++) {
//				Map<MAT<MO>, Integer> kcount = new HashMap<MAT<MO>, Integer>();
//				BitSet attributesT1 = attributeToTrajectories.get(i);
//				
//				for (int j = 0; j < train.size(); j++) {
//					BitSet attributesT2 = this.attributesToTrain.get(j);
//					BitSet matches = (BitSet) attributesT1.clone();
//					
//					matches.and(attributesT2);
//					kcount.put(train.get(j), matches.cardinality());
//				}
//				
//				List<Entry<MAT<MO>, Integer>> list = new ArrayList<Entry<MAT<MO>, Integer>>(kcount.entrySet());
//				Collections.sort(list, new Comparator<Entry<MAT<MO>, Integer>>() {
//				    public int compare(Entry<MAT<MO>, Integer> entry1, Entry<MAT<MO>, Integer> entry2) {
//				        return entry2.getValue().compareTo(entry1.getValue());
//				    }
//				});
//				
//				Map<String, Double> classCount = new HashMap<String, Double>();
//				for (int j = 0; j < K; j++) {
//					String key = list.get(j).getKey().getMovingObject().toString();
//					double count = classCount.getOrDefault(key, 0.0);
////					count += 1.0 + (list.get(j).getValue() / mapMovelets.size());
//					classCount.put(key, count+1.0);
//				}
//
//				Entry<String, Double> max = Collections.max(classCount.entrySet(), new Comparator<Entry<String, Double>>() {
//				    public int compare(Entry<String, Double> entry1, Entry<String, Double> entry2) {
//				        return entry1.getValue().compareTo(entry2.getValue());
//				    }
//				});
//				
//				String predict_class = max.getKey();
//				double predict = max.getValue() / ((double)K);
//
//				String line = "";
//				line += String.valueOf(predict) + ",";				
//				line += "\"" + predict_class + "\",";
//				line += "\"" + trajectories.get(i).getMovingObject() + "\""+ System.getProperty("line.separator");
//				
//				if (predict_class.equalsIgnoreCase(trajectories.get(i).getMovingObject().toString()))
//					TP++;
//				else 
//					TN++;
//				
//				writer.write(line);
//			}
//
//			writer.close();
////			attributeToTrajectories.clear();
//			
//			Mov3letsUtils.trace(
//					"\nMKNT results for " + filename + ". K: " + K + ". " +
//					"Correctly labeled: " + TP + ". " +
//					"Wrongly labeled: " + TN + ". " +
//					"Accuracy: " + (TP / (TP+TN)) + ". "
//			);
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	protected synchronized void attributesToSets(List<MAT<MO>> trajectories, List<Subtrajectory> movelets, List<BitSet> attributeToTrajectories) {
		// It puts distances as trajectory attributes
		for (Subtrajectory movelet : movelets) {
			attributeToSetsDiscrete(trajectories, movelet, attributeToTrajectories);
		}	
	}

	/**
	 * Attribute to trajectories Discrete.
	 *
	 * @param trajectories the trajectories
	 * @param movelet the movelet
	 * @param attributeToTrajectories the attribute to trajectories
	 */
	protected synchronized  void attributeToSetsDiscrete(List<MAT<MO>> trajectories, Subtrajectory movelet, List<BitSet> attributeToTrajectories) {
//		String attributeName =  "sh_TID" + movelet.getTrajectory().getTid() + 
//								"_START" + movelet.getStart() + 
//								"_SIZE" + movelet.getSize() + 
//								"_CLASS" + movelet.getTrajectory().getMovingObject();
		int idx = mapMovelets.size();
		if (mapMovelets.containsKey(movelet))
			idx = mapMovelets.get(movelet);
		else
			mapMovelets.put(movelet, idx);

		double[][] distances = movelet.getDistances();		
		RealMatrix rm = new Array2DRowRealMatrix(distances);
		Map<String,double[]> splitpointsData = movelet.getSplitpointData();		
		
		Pair<double[], double[]> splitpointLimits = fillSplitPointsLimits(splitpointsData,medium);
		double[] splitpointsLI = splitpointLimits.getFirst(); // Limite inferior
//		double[] splitpointsLS = splitpointLimits.getSecond(); // Limite superior
        
		for (int i = 0; i <  trajectories.size(); i++) {
			putAttributes(i, attributeToTrajectories, idx, isCovered(rm.getColumn(i), splitpointsLI));
		}
	}

	/**
	 * Gets the attributes.
	 *
	 * @param idxTrajectory the idx trajectory
	 * @param attributeToTrajectories the attribute to trajectories
	 * @param covered 
	 * @param idx 
	 * @return the attributes
	 */
	protected void putAttributes(int idxTrajectory, List<BitSet> attributeToTrajectories, int idx, boolean covered) {
		BitSet attributes;
		if (attributeToTrajectories.size()-1 < idxTrajectory) {
			attributes = new BitSet();
			attributeToTrajectories.add(attributes);
		} else 
			attributes = attributeToTrajectories.get(idxTrajectory);
		
		if (covered)
			attributes.set(idx);
		
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
