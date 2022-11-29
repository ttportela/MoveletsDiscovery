/**
 * 
 */
package br.ufsc.mov3lets.method.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.Feature;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.utils.Mov3letsUtils;

/**
 * The Class CSVDOutputter. 
 * For discrete CSV output.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class CSVFOutputter<MO> extends FeatureOutputter<MO> {

	/** The medium. */
	protected String medium = "none"; // Other values minmax, sd, interquartil

	/** The output. */
	//protected String output = "discrete"; // Accept only discrete.
	
	/** The attributes to train. */
	protected Map<String, BitSet> attributesToTrainD0 = new HashMap<String,BitSet>();
	protected Map<String, BitSet> attributesToTrainD1 = new HashMap<String,BitSet>();
	
	/** The attributes to test. */
	protected Map<String, BitSet> attributesToTestD0 = new HashMap<String,BitSet>();
	protected Map<String, BitSet> attributesToTestD1 = new HashMap<String,BitSet>();
//	protected List<Map<String, Double>> features = new ArrayList<Map<String,Double>>(); // TODO Necessary?
	
	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param filePath the file path
	 * @param descriptor the descriptor
	 * @param subfolderClasses the subfolder classes
	 */
	public CSVFOutputter(String filePath, String movingObjectName, Descriptor descriptor, boolean subfolderClasses) {
		super(filePath, movingObjectName, descriptor, subfolderClasses);
		init();
	}
	
	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param descriptor the descriptor
	 */
	public CSVFOutputter(Descriptor descriptor) {
		super(descriptor);
		init();
	}

	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param resultDirPath the result dir path
	 * @param descriptor the descriptor
	 */
	public CSVFOutputter(String resultDirPath, String movingObjectName, Descriptor descriptor) {
		super(resultDirPath, movingObjectName, descriptor, true);
		init();
	}
	
	public void init() {
		medium = getDescriptor().hasParam("medium")? getDescriptor().getParamAsText("medium") : medium;
		//output = getDescriptor().hasParam("output")? getDescriptor().getParamAsText("output") : output;
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
		// Assumes that the movelets were previously written one by one.
		
		if (delayOutput) {			
			decreaseDelayCount(filename);
			if (delayCount > 0)
				return;
		}
		
		Map<String, BitSet> attributeToTrajectoriesD0 = 
				"train".equals(filename)? attributesToTrainD0 : attributesToTestD0;
		Map<String, BitSet> attributeToTrajectoriesD1 = 
				"train".equals(filename)? attributesToTrainD1 : attributesToTestD1;
		
//		if (movelets.isEmpty()) {
//			Mov3letsUtils.traceW("Empty movelets set [NOT OUTPUTTED]");
		if (attributeToTrajectoriesD0.isEmpty() && attributeToTrajectoriesD1.isEmpty()) {
			Mov3letsUtils.traceW("Empty movelets set for class "+getMovingObject()+" [NOT OUTPUTTED]");
			return;
		}
		
		Set<String> features = new HashSet<String>();
		features.addAll(attributeToTrajectoriesD0.keySet());
		features.addAll(attributeToTrajectoriesD1.keySet());
		
		BufferedWriter writer;

		try {
			File file = getFile(getMovingObject(), filename + ".csv");
			boolean append = !this.subfolderClasses && file.exists() // Append if it is not class separated
					|| !delayOutput;  // OR is not delayed
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file, append));

			// TODO Features?
//			String header = (!trajectories.get(0).getFeatures().keySet().isEmpty()) ? 
//					trajectories.get(0).getFeatures().keySet().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
//			if (!append) { //TODO incorreto, necessário adicionar as colunas (movelets) com exceção da classe
			String header = "";
			for (String m : features) {
				header += m.replaceAll("[\\[|\\]|\\s]", "") + ",";
			}			
			header += "class" + System.getProperty("line.separator");
			
			writer.write(header);
			
			for (int i = 0; i < trajectories.size(); i++) {
				String line = "";

				for (String m : features) {
					if (inIndex(attributeToTrajectoriesD0, m, i))
						line += "0,";
					else if  (inIndex(attributeToTrajectoriesD1, m, i))
						line += "1,";
					else
						line += "2,";
				}
				
				line += "\"" + trajectories.get(i).getMovingObject() + "\""+ System.getProperty("line.separator");
				
				writer.write(line);
			}

			writer.close();
			attributeToTrajectoriesD0.clear();
			attributeToTrajectoriesD1.clear();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void writeMovelet(String filename, List<MAT<MO>> trajectories, Feature movelet, Object... params) {
		Map<String, BitSet> attributeToTrajectoriesD0 = 
				"train".equals(filename)? attributesToTrainD0 : attributesToTestD0;
		Map<String, BitSet> attributeToTrajectoriesD1 = 
				"train".equals(filename)? attributesToTrainD1 : attributesToTestD1;
		
		double[][] distances = movelet.getDistances();		
		RealMatrix rm = new Array2DRowRealMatrix(distances);
		Map<String,double[]> splitpointsData = movelet.getSplitpointData();		
		
		Pair<double[], double[]> splitpointLimits = fillSplitPointsLimits(splitpointsData,medium);
		double[] splitpointsLI = splitpointLimits.getFirst(); // Limit, inferior
		double[] splitpointsLS = splitpointLimits.getSecond(); // Limit, superior
        
		for (int i = 0; i <  trajectories.size(); i++) {
			if (isCovered(rm.getColumn(i), splitpointsLI))
				//distance = 0;
				addToIndex(attributeToTrajectoriesD0, movelet.getFeatureName(), i);
			else if (isCovered(rm.getColumn(i), splitpointsLS))
				//distance = 1;
				addToIndex(attributeToTrajectoriesD1, movelet.getFeatureName(), i);
		}
	}
	
	protected void addToIndex(Map<String, BitSet> index, String value, int tid) {
		
		BitSet rIds = index.get(value);

		if (rIds == null) {
			rIds = new BitSet();
			rIds.set(tid);
			index.put(value, rIds);
		} else {
			rIds.set(tid);
			index.replace(value, rIds);
		}

	}
	
	protected boolean inIndex(Map<String, BitSet> index, String value, int tid) {
		BitSet rIds = index.get(value);

		if (rIds == null) {
			return false;
		} else {
			return rIds.get(tid);
		}
	}
	
	/**
	 *  
	 * Para o caso de empate por conta de movelets discretas.
	 *
	 * @param point the point
	 * @param limits the limits
	 * @return true, if is covered
	 */
	protected boolean isCovered(double[] point, double[] limits){
		int dimensions = limits.length;
		
		for (int i = 0; i < dimensions; i++) {
			if (limits[i] > 0){
				if (point[i] >= limits[i])
					return false;
			} else
				if (point[i] > limits[i])
					return false;
		}
		
		return true;
	}
	
	/**
	 * Fill split points limits.
	 *
	 * @param splitpointsData the splitpoints data
	 * @param medium the medium
	 * @return the pair
	 */
	protected Pair<double[],double[]> fillSplitPointsLimits(Map<String, double[]> splitpointsData, String medium){
		int n = splitpointsData.get("mean").length;
		double[] splitpointsLI = new double[n];
		double[] splitpointsLS = new double[n];
		
		switch (medium){
		
		case "interquartil" :
			splitpointsLI = splitpointsData.get("p25");
			splitpointsLS = splitpointsData.get("p75");				
			break;
		case "sd" :
			for (int i = 0; i < n; i++) {
				splitpointsLI[i] = splitpointsData.get("mean")[i] - splitpointsData.get("sd")[i];
				splitpointsLS[i] = splitpointsData.get("mean")[i] + splitpointsData.get("sd")[i];
			}
			break;
		case "minmax" :
			splitpointsLI = splitpointsData.get("min");
			splitpointsLS = splitpointsData.get("max");				
			break;
		case "mean" :
		default :
			splitpointsLI = splitpointsData.get("mean");
			splitpointsLS = splitpointsData.get("mean");
		}		
		
		return new Pair<double[], double[]>(splitpointsLI,splitpointsLS);
	}

}
