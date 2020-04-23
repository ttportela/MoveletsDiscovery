/**
 * 
 */
package br.com.tarlis.mov3lets.method.output;

import java.io.BufferedWriter;
import java.io.File;
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

import br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

/**
 * @author tarlis
 *
 */
public class CSVOutputter<MO> extends OutputterAdapter<MO> {

	protected String medium = "none"; // Other values minmax, sd, interquartil
	protected String output = "numeric"; // Other values: normalized?? and discrete
	
	protected List<Map<String, Double>> attributesToTrain = new ArrayList<Map<String,Double>>();
	protected List<Map<String, Double>> attributesToTest = new ArrayList<Map<String,Double>>();
//	protected List<Map<String, Double>> features = new ArrayList<Map<String,Double>>(); // TODO Necessary?
	
	/**
	 * @param filePath
	 */
	public CSVOutputter(String filePath, Descriptor descriptor, boolean subfolderClasses) {
		super(filePath, descriptor, subfolderClasses);
	}
	
	public CSVOutputter(Descriptor descriptor) {
		super(descriptor);
	}

	public CSVOutputter(String resultDirPath, Descriptor descriptor) {
		super(resultDirPath, descriptor, true);
	}

	@Override
	public void write(String filename, List<MAT<MO>> trajectories, List<Subtrajectory> movelets, boolean delayOutput) {
		List<Map<String, Double>> attributeToTrajectories = 
				"train".equals(filename)? attributesToTrain : attributesToTest;
		
		if (delayOutput) {
			attributesToTrajectories(trajectories, movelets, attributeToTrajectories);
			return;
		}
		
		BufferedWriter writer;

		try {
			File file = getFile(movelets.get(0).getTrajectory().getMovingObject().toString(), filename + ".csv");
			boolean append = !this.subfolderClasses && file.exists();  // Append if it is not class separated
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file, append));

			// TODO Features?
//			String header = (!trajectories.get(0).getFeatures().keySet().isEmpty()) ? 
//					trajectories.get(0).getFeatures().keySet().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
			if (!append) { //TODO incorreto, necessário adicionar as colunas (movelets) com exceção da classe
				String header = "";
				header += (!attributeToTrajectories.get(0).keySet().isEmpty()) ?
						attributeToTrajectories.get(0).keySet().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : ""; 
				
				header += "class" + System.getProperty("line.separator");
				
				writer.write(header);
			}
			
			for (int i = 0; i < trajectories.size(); i++) {
				Map<String,Double> attributes = attributeToTrajectories.get(i);
				String line = "";
				// TODO Features?
//				String line = (!trajectory.getFeatures().values().isEmpty()) ?
//								trajectory.getFeatures().values().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
				line += (!attributes.values().isEmpty()) ?
						attributes.values().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
				line += "\"" + trajectories.get(i).getMovingObject() + "\""+ System.getProperty("line.separator");
				
				writer.write(line);
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void attributesToTrajectories(List<MAT<MO>> trajectories, List<Subtrajectory> movelets, List<Map<String, Double>> attributeToTrajectories) {
		// It puts distances as trajectory attributes
		for (Subtrajectory movelet : movelets) {
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
	 * @param trajectories
	 * @param movelet
	 * @param attributeToTrajectories 
	 */
	protected void attributeToTrajectoriesNumeric(List<MAT<MO>> trajectories, Subtrajectory movelet, List<Map<String, Double>> attributeToTrajectories) {
		String attributeName =  "sh_TID" + movelet.getTrajectory().getTid() + 
								"_START" + movelet.getStart() + 
								"_SIZE" + movelet.getSize() + 
								"_CLASS" + movelet.getTrajectory().getMovingObject();

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
			
			getAttributes(i, attributeToTrajectories).put(attributeName, distance);
		}
	}

	/**
	 * @param trajectories
	 * @param movelet
	 * @param attributeToTrajectories 
	 */
	protected void attributeToTrajectoriesDiscrete(List<MAT<MO>> trajectories, Subtrajectory movelet, List<Map<String, Double>> attributeToTrajectories) {
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
				distance = normalizeCovered(rm.getColumn(i), splitpoints);
			} else {
				distance = normalizeNonCovered(rm.getColumn(i), splitpoints, maxDistances);
			}
				
			getAttributes(i, attributeToTrajectories).put(attributeName, distance);
		}
	}

	protected Map<String, Double> getAttributes(int idxTrajectory, List<Map<String, Double>> attributeToTrajectories) {
		Map<String,Double> attributes;
		if (attributeToTrajectories.size()-1 < idxTrajectory) {
			attributes = new ConcurrentHashMap<>();
			attributeToTrajectories.add(idxTrajectory, attributes);
		} else 
			attributes = attributeToTrajectories.get(idxTrajectory);
		return attributes;
	}
	

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
	 * Para o caso de empate por conta de movelets discretas.
	 * 
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
	
	protected double normalizeCovered(double[] point, double[] limits) {
		int dimensions = limits.length;
		double sumOfProportions = 0;
		
		for (int i = 0; i < dimensions; i++) {
			sumOfProportions += point[i] / limits[i];
		}
		
		return sumOfProportions / dimensions;
	}

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
