/**
 * Mov3lets - Multiple Aspect Trajectory (MASTER) Classification Version 3. 
 * Copyright (C) 2019  Tarlis Portela <tarlis@tarlis.com.br>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package br.com.tarlis.mov3lets.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.tarlis.mov3lets.method.qualitymeasure.Quality;
import br.com.tarlis.mov3lets.model.aspect.Aspect;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Subtrajectory {

	private int start;
	private int end;
 
	private MAT<?> trajectory;
	private List<Point> points;
		
//	private List<List<Double>> distances;
	private double[][] distances;
	private List<Subtrajectory> bestAlignments;
	
	private Quality quality;
	private double proportionInClass;

	private HashMap<String, Aspect<?>> features;
	
	//TODO necessary?????????????????????????????:
	public Map<String, double[]> splitpointData; 
	
	private int[] pointFeatures;
	private int k; // Combination index
	
//	private Map<Integer, double[]> mdist; 
	
	private double[] splitpoints;
	
	private double[] maxDistances;

	private boolean[] goodTrajectories;

	public Subtrajectory(int start, int end, MAT<?> t) {
		super();
		this.start = start;
		this.end = end;
		this.trajectory = t;
		this.points = t.getPoints().subList(start, end+1);
//		this.features = new HashMap<>();
	}
	
	/**
	 * 
	 * @param start
	 * @param end
	 * @param t
	 * @param pointFeatures
	 * @param numberOfTrajectories
	 */
	public Subtrajectory(int start, int end, MAT<?> t, int[] pointFeatures, int numberOfTrajectories) {
		super();
		this.start = start;
		this.end = end;
		this.trajectory = t;
		this.points = t.getPoints().subList(start, end+1);
		this.pointFeatures = pointFeatures;
//		this.distances = new HashMap<Aspect<?>, Double>();
		this.distances = new double[pointFeatures.length][numberOfTrajectories];
//		this.features = new HashMap<>();
	}
	
	public Subtrajectory(int start, int end, MAT<?> t, int numberOfTrajectories, int[] pointFeatures, int k) {
		super();
		this.start = start;
		this.end = end;
		this.trajectory = t;
		this.points = t.getPoints().subList(start, end+1);
		this.k = k;
		this.pointFeatures = pointFeatures;
//		this.distances = new HashMap<Aspect<?>, Double>();
		this.distances = new double[pointFeatures.length][numberOfTrajectories];
//		this.features = new HashMap<>();
		this.bestAlignments = new ArrayList<Subtrajectory>();
	}

	public boolean[] getGoodTrajectories() {
		return goodTrajectories;
	}
	
//	public HashMap<String, IFeature> getFeatures() {
//		return features;
//	}
//	
//	@Override
//	public IFeature getFeature(String featureName){
//		return 	features.get(featureName);
//	}

	@Override
	public String toString() {

		String string = new String();

//		string += "Origin: t" + getTrajectory().getTid() + " from " + start + " to " + end + ", ";
//		string += "Size: " + getSize() + ", ";
//		string += "Label: " + getTrajectory().getMovingObject().toString() + "\n";
//		string += "Distances: \n";
		string += "Size: " + getSize() + ", Features: " + Arrays.toString(getPointFeatures()) + "\n";
		string += "Origin: t" + getTrajectory().getTid() + " from " + start + " to " + end + "\n";
		string += "Quality: " + getQuality() + "\n";
		string += "Label: " + getTrajectory().getMovingObject().toString() + "\n";
		string += "Data: \n";
		for (double[] row : distances) 
			string += Arrays.toString(row) + "\n"; 

		return string;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public MAT<?> getTrajectory() {
		return trajectory;
	}

	public void setTrajectory(MAT<?> trajectory) {
		this.trajectory = trajectory;
	}
	
	/**
	 * @return the points
	 */
	public List<Point> getPoints() {
		return points;
	}
	
	/**
	 * @param points the points to set
	 */
	public void setPoints(List<Point> points) {
		this.points = points;
	}

	public void setFeatures(HashMap<String, Aspect<?>> features) {
		this.features = features;
	}

	public int getSize() {
		return end - start + 1;
	}
	
	/**
	 * @return the bestAlignments
	 */
	public List<Subtrajectory> getBestAlignments() {
		return bestAlignments;
	}
	
	/**
	 * @param bestAlignments the bestAlignments to set
	 */
	public void setBestAlignments(List<Subtrajectory> bestAlignments) {
		this.bestAlignments = bestAlignments;
	}
	
//	/**
//	 * @return the distances
//	 */
//	public List<List<Double>> getDistances() {
//		return distances;
//	}
//	
//	/**
//	 * @param distances the distances to set
//	 */
//	public void setDistances(List<List<Double>> distances) {
//		this.distances = distances;
//	}

//	/**
//	 * @return
//	 */
//	public double[][] getDistancesToArray() {
//		double[][] distances = new double[getDistances().size()][];
//		for (int i = 0; i < getDistances().size(); i++) {
//			distances[i] = getDistances().get(i).stream().mapToDouble(d -> d).toArray();
//			
//		}
//		return distances;
//	}
	
	public double[][] getDistances() {
		return distances;
	}
	
	public void setDistances(double[][] distances) {
		this.distances = distances;
	}
	
//	 public Subtrajectory[] getBestAlignments() {
//		return bestAlignments;
//	}
//	 
//	public Map<Integer, double[]> getMdist() {
//		return mdist;
//	}
//	
//	public void setBestAlignments(Subtrajectory[] bestAlignments) {
//		this.bestAlignments = bestAlignments;
//	}	
	
	public void setQuality(Quality quality) {
		this.quality = quality;
	}
	
	public Quality getQuality() {
		// TODO Auto-generated method stub
		return this.quality;
	}

	public double[] getPoint() {
		// TODO Auto-generated method stub
		return null;
	}

//	public void createMDist() {
//		// TODO Auto-generated method stub
//		mdist = new HashMap<>();
//	}
	
	public int[] getPointFeatures() {
		return pointFeatures;
	}
	
	public void setPointFeatures(int[] pointFeatures) {
		this.pointFeatures = pointFeatures;
	}
	
	/**
	 * @return the k
	 */
	public int getK() {
		return k;
	}
	
	/**
	 * @param k the k to set
	 */
	public void setK(int k) {
		this.k = k;
	}
	
	public double[] getMaxDistances() {
		return maxDistances;
	}

	public void setMaxDistances(double[] maxDistances) {
		this.maxDistances = maxDistances;
	}

	public double[] getSplitpoints() {
		return splitpoints;
	}

	public void setSplitpoints(double[] splitpoints) {
		this.splitpoints = splitpoints;
	}
	
	public Map<String, double[]> getSplitpointData() {
		return splitpointData;
	}

	public void setSplitpointData(Map<String, double[]> splitpointData) {
		this.splitpointData = splitpointData;
	}
	
	/**
	 * @return the proportionInClass
	 */
	public double getProportionInClass() {
		return proportionInClass;
	}
	
	/**
	 * @param proportionInClass the proportionInClass to set
	 */
	public void setProportionInClass(double proportionInClass) {
		this.proportionInClass = proportionInClass;
	}
	
}
