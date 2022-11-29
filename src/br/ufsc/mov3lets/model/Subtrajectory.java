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
package br.ufsc.mov3lets.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * The Class Subtrajectory.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 */
public class Subtrajectory extends Feature {

	/** The start. */
	protected int start;
	
	/** The end. */
	protected int end;
	
	/** The points. */
	protected List<Point> points;
	
	/** The best alignments. */
	protected List<Subtrajectory> bestAlignments;
	
//	private double proportionInClass;
//	private HashMap<String, Aspect<?>> features;
	
	/** The k. */
	protected int k; // Combination index
	
//	private Map<Integer, double[]> mdist; 
	
	public Subtrajectory() {}

	/**
	 * Instantiates a new subtrajectory.
	 *
	 * @param start the start
	 * @param end the end
	 * @param t the t
	 */
	public Subtrajectory(int start, int end, MAT<?> t) {
		super();
		this.start = start;
		this.end = end;
		this.trajectory = t;
		this.points = t.getPoints().subList(start, end+1);
//		this.features = new HashMap<>();
	}
	
	/**
	 * Instantiates a new subtrajectory.
	 *
	 * @param start the start
	 * @param end the end
	 * @param t the t
	 * @param pointFeatures the point features
	 * @param numberOfTrajectories the number of trajectories
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
	
	/**
	 * Instantiates a new subtrajectory.
	 *
	 * @param start the start
	 * @param end the end
	 * @param t the t
	 * @param numberOfTrajectories the number of trajectories
	 * @param pointFeatures the point features
	 * @param k the k
	 */
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
	
//	public HashMap<String, IFeature> getFeatures() {
//		return features;
//	}
//	
//	@Override
//	public IFeature getFeature(String featureName){
//		return 	features.get(featureName);
//	}

	/**
	 * Overridden method. 
	 * @see java.lang.Object#toString().
	 * 
	 * @return
	 */
	@Override
	public String toString() {

		String string = new String();

		string += "Origin: t" + getTrajectory().getTid() + " from " + start + " to " + end + ", ";
		string += "Size: " + getSize() + ", Features: " + Arrays.toString(getPointFeatures()) + "\n";
		string += "Label: " + getTrajectory().getMovingObject().toString() + "\n";
		string += "Quality: " + getQuality() + "\n";
//		string += "Distances: \n";
//		string += "Data: \n";

		return string;
	}

	/**
	 * Gets the start.
	 *
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Sets the start.
	 *
	 * @param start the new start
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * Gets the end.
	 *
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Sets the end.
	 *
	 * @param end the new end
	 */
	public void setEnd(int end) {
		this.end = end;
	}
	
	/**
	 * Gets the points.
	 *
	 * @return the points
	 */
	public List<Point> getPoints() {
		return points;
	}
	
	/**
	 * Sets the points.
	 *
	 * @param points the points to set
	 */
	public void setPoints(List<Point> points) {
		this.points = points;
	}

//	public void setFeatures(HashMap<String, Aspect<?>> features) {
//		this.features = features;
//	}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public int getSize() {
		return end - start + 1;
	}
	
	/**
	 * Gets the best alignments.
	 *
	 * @return the bestAlignments
	 */
	public List<Subtrajectory> getBestAlignments() {
		return bestAlignments;
	}
	
	/**
	 * Sets the best alignments.
	 *
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

//	public void createMDist() {
//		// TODO Auto-generated method stub
//		mdist = new HashMap<>();
//	}
	
	/**
	 * Gets the k.
	 *
	 * @return the k
	 */
	public int getK() {
		return k;
	}
	
	/**
	 * Sets the k.
	 *
	 * @param k the k to set
	 */
	public void setK(int k) {
		this.k = k;
	}
	
//	/**
//	 * @return the proportionInClass
//	 */
//	public double getProportionInClass() {
//		return proportionInClass;
//	}
//	
//	/**
//	 * @param proportionInClass the proportionInClass to set
//	 */
//	public void setProportionInClass(double proportionInClass) {
//		this.proportionInClass = proportionInClass;
//	}

	public Subtrajectory best(Subtrajectory s) {
		int x = this.getQuality().compareTo(s.getQuality());
		if (x <= 0)
			return this;
		else 
			return s;
	}
	
	public String getFeatureName() {
		return "mv_TID" + getTrajectory().getTid() + 
				"_START" + getStart() + 
				"_SIZE" + getSize() + 
				"_FT_" + StringUtils.join(ArrayUtils.toObject(getPointFeatures()), "_") +
				"_CLASS" + getTrajectory().getMovingObject();
	}
}
