/**
 * 
 */
package br.ufsc.mov3lets.method.output;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.Feature;
import br.ufsc.mov3lets.model.MAT;

/**
 * The Class CSVOutputter.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class CSVAddOutputter<MO> extends OutputterAdapter<MO,List<Feature>> {

	/** The medium. */
	protected String medium = "none"; // Other values minmax, sd, interquartil
	
	/** The output. */
	protected String output = "numeric"; // Other values: normalized?? and discrete
	
	/** The attributes to train. */
	protected List<Map<String, Double>> attributesToTrain = new ArrayList<Map<String,Double>>();
	
	/** The attributes to test. */
	protected List<Map<String, Double>> attributesToTest = new ArrayList<Map<String,Double>>();
	
	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param filePath the file path
	 * @param descriptor the descriptor
	 * @param subfolderClasses the subfolder classes
	 */
	public CSVAddOutputter(String filePath, String movingObjectName, Descriptor descriptor, boolean subfolderClasses) {
		super(filePath, movingObjectName, descriptor, subfolderClasses);
	}
	
	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param descriptor the descriptor
	 */
	public CSVAddOutputter(Descriptor descriptor) {
		super(descriptor);
	}

	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param resultDirPath the result dir path
	 * @param descriptor the descriptor
	 */
	public CSVAddOutputter(String resultDirPath, String movingObjectName, Descriptor descriptor) {
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
	public synchronized void write(String filename, List<MAT<MO>> trajectories, List<Feature> movelets, 
			boolean delayOutput, Object... params) {
		List<Map<String, Double>> attributeToTrajectories =  new ArrayList<Map<String,Double>>();
//				"train".equals(filename)? attributesToTrain : attributesToTest;

		attributesToTrajectories(trajectories, movelets, attributeToTrajectories);
//		if (delayOutput) return;
		
//		if (movelets.isEmpty()) {
//			Mov3letsUtils.traceW("Empty movelets set [NOT OUTPUTTED]");
////			return;
//		}
		
		decreaseDelayCount(filename);
		boolean finish = false;
		if (!(delayCount > 0))
			finish = true;
		
		if (attributeToTrajectories.isEmpty()) return; //{
//			Mov3letsUtils.traceW("Empty movelets set for class "+getMovingObject()+" [NOT OUTPUTTED]");
//			return;
//		}
		
		BufferedWriter writer;
		BufferedReader reader;

		try {
			File filetemp = getFile(getMovingObject(), filename + ".tmp");
			File file = getFile(movelets.get(0).getTrajectory().getMovingObject().toString(), filename + ".csv");
			boolean append = true; //!this.subfolderClasses && file.exists() // Append if it is not class separated
//							 || !delayOutput;  // OR is not delayed
			file.getParentFile().mkdirs();
			reader = file.exists()? new BufferedReader(new FileReader(file)): null;
			writer = new BufferedWriter(new FileWriter(filetemp, append));

			String header = "";
			if (reader != null) {
				header = reader.readLine();
//				header = header.substring(0, header.indexOf(",class"));
			}
				
//			if (!append) { //TODO incorreto, necessário adicionar as colunas (movelets) com exceção da classe
			header += (!attributeToTrajectories.get(0).keySet().isEmpty()) ?
					attributeToTrajectories.get(0).keySet().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : ""; 
			
			if (finish)
				header += "class";
			header += System.getProperty("line.separator");
			
			writer.write(header);
			
			for (int i = 0; i < trajectories.size(); i++) {
				Map<String,Double> attributes = attributeToTrajectories.get(i);
				String line = "";
				
				if (reader != null) {
					line = reader.readLine();
				}
				
				line += (!attributes.values().isEmpty()) ?
						attributes.values().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
				if (finish)
					line += "\"" + trajectories.get(i).getMovingObject() + "\"";
				line += System.getProperty("line.separator");
				
				writer.write(line);
			}

			attributeToTrajectories.clear();
			if (reader != null)
				reader.close();
			writer.close();
			
			filetemp.renameTo(file);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Attributes to trajectories.
	 *
	 * @param trajectories the trajectories
	 * @param movelets the movelets
	 * @param attributeToTrajectories the attribute to trajectories
	 */
	protected void attributesToTrajectories(List<MAT<MO>> trajectories, List<Feature> movelets, List<Map<String, Double>> attributeToTrajectories) {
		// It puts distances as trajectory attributes
		for (Feature movelet : movelets) {
			switch (output){
				case "numeric" :
					attributeToTrajectoriesNumeric(trajectories, movelet, attributeToTrajectories);
					break;
				
				case "discrete" :	
					attributeToTrajectoriesDiscrete(trajectories, movelet, attributeToTrajectories);
					break;
					
			}
		}	
	}

	/**
	 * Attribute to trajectories numeric.
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
			
			getAttributes(i, attributeToTrajectories).put(movelet.getFeatureName(), distance);
		}
	}

	/**
	 * Attribute to trajectories discrete.
	 *
	 * @param trajectories the trajectories
	 * @param movelet the movelet
	 * @param attributeToTrajectories the attribute to trajectories
	 */
	protected void attributeToTrajectoriesDiscrete(List<MAT<MO>> trajectories, Feature movelet, List<Map<String, Double>> attributeToTrajectories) {
//		String attributeName =  "sh_TID" + movelet.getTrajectory().getTid() + 
//								"_START" + movelet.getStart() + 
//								"_SIZE" + movelet.getSize() + 
//								"_CLASS" + movelet.getTrajectory().getMovingObject();

		RealMatrix rm = new Array2DRowRealMatrix(movelet.getDistances());
				
		double[] splitpoints = movelet.getSplitpoints();
		double[] maxDistances = movelet.getMaxDistances();
				
		double distance;
		
		for (int i = 0; i < trajectories.size(); i++) {
			
			if (isCovered(rm.getColumn(i), splitpoints)){
				distance = normalizeCovered(rm.getColumn(i), splitpoints);
			} else {
				distance = normalizeNonCovered(rm.getColumn(i), splitpoints, maxDistances);
			}
				
			getAttributes(i, attributeToTrajectories).put(movelet.getFeatureName(), distance);
		}
	}

	/**
	 * Gets the attributes.
	 *
	 * @param idxTrajectory the idx trajectory
	 * @param attributeToTrajectories the attribute to trajectories
	 * @return the attributes
	 */
	protected Map<String, Double> getAttributes(int idxTrajectory, List<Map<String, Double>> attributeToTrajectories) {
		Map<String,Double> attributes;
		if (attributeToTrajectories.size()-1 < idxTrajectory) {
			attributes = new ConcurrentHashMap<>();
			attributeToTrajectories.add(idxTrajectory, attributes);
		} else 
			attributes = attributeToTrajectories.get(idxTrajectory);
		return attributes;
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
	 * Normalize covered.
	 *
	 * @param point the point
	 * @param limits the limits
	 * @return the double
	 */
	protected double normalizeCovered(double[] point, double[] limits) {
		int dimensions = limits.length;
		double sumOfProportions = 0;
		
		for (int i = 0; i < dimensions; i++) {
			sumOfProportions += point[i] / limits[i];
		}
		
		return sumOfProportions / dimensions;
	}

	/**
	 * Normalize non covered.
	 *
	 * @param point the point
	 * @param limits the limits
	 * @param maxDistances the max distances
	 * @return the double
	 */
	protected double normalizeNonCovered(double[] point, double[] limits, double[] maxDistances) {
		int dimensions = limits.length;
		double sumOfProportions = 0;
		
		if (maxDistances == null){
			maxDistances = new double[point.length];
			Arrays.fill(maxDistances, Double.MAX_VALUE);
		}
		
		for (int i = 0; i < dimensions; i++) {
			if (point[i] >= maxDistances[i]){
				point[i] = maxDistances[i];				
			}
			sumOfProportions += point[i] / limits[i];
		}
		
		return 1.0 + sumOfProportions / dimensions;
	}

	/**
	 * Default: none [mean].
	 * 
	 * @return the medium (none, minmax, sd, interquartil, mean)
	 */
	public String getMedium() {
		return medium;
	}

	/**
	 * Types: mean | minmax | sd | interquartil.
	 * 
	 * @param medium the medium to set
	 */
	public void setMedium(String medium) {
		this.medium = medium;
	}

	/**
	 * Default: numeric.
	 * 
	 * @return the output normalized and discretized
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * Types: numeric | discrete.
	 * 
	 * @param output the output to set
	 */
	public void setOutput(String output) {
		this.output = output;
	}

}
