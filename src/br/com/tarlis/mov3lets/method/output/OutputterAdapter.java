/**
 * 
 */
package br.com.tarlis.mov3lets.method.output;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

/**
 * @author tarlis
 *
 */
public abstract class OutputterAdapter<MO> {
	
	protected String filePath;
	protected Descriptor descriptor;

	protected String medium = "none"; // Other values minmax, sd, interquartil
	protected String output = "numeric"; // Other values normalized and discretized
	

	public OutputterAdapter(Descriptor descriptor) {
		this(descriptor.getParamAsText("respath"), descriptor);
	}
	
	/**
	 * 
	 */
	public OutputterAdapter(String filePath, Descriptor descriptor) {
		this.filePath = filePath;
		this.descriptor = descriptor;
	}
	
	public abstract void write(List<MAT<MO>> trajectories, List<Subtrajectory> movelets);
	
	public void attributesToTrajectories(List<MAT<MO>> trajectories, List<Subtrajectory> movelets) {
		// It puts distances as trajectory attributes
		for (Subtrajectory movelet : movelets) {
			switch (output){
				case "numeric" :
					attributeToTrajectoriesNumeric(trajectories, movelet);
					break;
				
				case "discrete" :	
					attributeToTrajectoriesDiscrete(trajectories, movelet);
					break;
					
			}
		}	
	}

	/**
	 * @param trajectories
	 * @param movelet
	 */
	private void attributeToTrajectoriesNumeric(List<MAT<MO>> trajectories, Subtrajectory movelet) {
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
			
			trajectories.get(i).getAttributes().put(attributeName, distance);
		}
	}

	/**
	 * @param trajectories
	 * @param movelet
	 */
	private void attributeToTrajectoriesDiscrete(List<MAT<MO>> trajectories, Subtrajectory movelet) {
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
				
			trajectories.get(i).getAttributes().put(attributeName, distance);
		}
	}
	

	public Pair<double[],double[]> fillSplitPointsLimits(Map<String, double[]> splitpointsData, String medium){
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
	public boolean isCovered(double[] point, double[] limits){
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
	
	private double normalizeCovered(double[] point, double[] limits) {
		int dimensions = limits.length;
		double sumOfProportions = 0;
		
		for (int i = 0; i < dimensions; i++) {
			sumOfProportions += point[i] / limits[i];
		}
		
		return sumOfProportions / dimensions;
	}

	private double normalizeNonCovered(double[] point, double[] limits, double[] maxDistances) {
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
	
	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * @return the descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	/**
	 * @param descriptor the descriptor to set
	 */
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}
	
}
