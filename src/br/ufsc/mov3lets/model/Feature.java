package br.ufsc.mov3lets.model;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import br.ufsc.mov3lets.method.qualitymeasure.Quality;

public class Feature {
	 
	/** The trajectory. */
	protected MAT<?> trajectory;
	
	/** The distances. */
	protected double[][] distances;
	
	/** The quality. */
	protected Quality quality;
	
	/** The splitpoint data. */
	protected Map<String, double[]> splitpointData; 
	
	/** The point features / attributes. */
	protected int[] pointFeatures;
	
	/** The splitpoints. */
	protected double[] splitpoints;
	
	/** The max distances. */
	protected double[] maxDistances;

//	/** The good trajectories. */
//	protected boolean[] goodTrajectories;
	
	/** The covered. */
	protected List<MAT<?>> covered;

//	/**
//	 * Gets the good trajectories.
//	 *
//	 * @return the good trajectories
//	 */
//	public boolean[] getGoodTrajectories() {
//		return goodTrajectories;
//	}

	/**
	 * Gets the trajectory.
	 *
	 * @return the trajectory
	 */
	public MAT<?> getTrajectory() {
		return trajectory;
	}

	/**
	 * Sets the trajectory.
	 *
	 * @param trajectory the new trajectory
	 */
	public void setTrajectory(MAT<?> trajectory) {
		this.trajectory = trajectory;
	}
	
	/**
	 * Gets the distances.
	 *
	 * @return the distances
	 */
	public double[][] getDistances() {
		return distances;
	}
	
	/**
	 * Sets the distances.
	 *
	 * @param distances the new distances
	 */
	public void setDistances(double[][] distances) {
		this.distances = distances;
	}
	
	/**
	 * Sets the quality.
	 *
	 * @param quality the new quality
	 */
	public void setQuality(Quality quality) {
		this.quality = quality;
	}
	
	/**
	 * Gets the quality.
	 *
	 * @return the quality
	 */
	public Quality getQuality() {
		// TODO Auto-generated method stub
		return this.quality;
	}
	
	/**
	 * Gets the point features.
	 *
	 * @return the point features
	 */
	public int[] getPointFeatures() {
		return pointFeatures;
	}
	
	/**
	 * Sets the point features.
	 *
	 * @param pointFeatures the new point features
	 */
	public void setPointFeatures(int[] pointFeatures) {
		this.pointFeatures = pointFeatures;
	}
	
	/**
	 * Gets the max distances.
	 *
	 * @return the max distances
	 */
	public double[] getMaxDistances() {
		return maxDistances;
	}

	/**
	 * Sets the max distances.
	 *
	 * @param maxDistances the new max distances
	 */
	public void setMaxDistances(double[] maxDistances) {
		this.maxDistances = maxDistances;
	}

	/**
	 * Gets the splitpoints.
	 *
	 * @return the splitpoints
	 */
	public double[] getSplitpoints() {
		return splitpoints;
	}

	/**
	 * Sets the splitpoints.
	 *
	 * @param splitpoints the new splitpoints
	 */
	public void setSplitpoints(double[] splitpoints) {
		this.splitpoints = splitpoints;
	}
	
	/**
	 * Gets the splitpoint data.
	 *
	 * @return the splitpoint data
	 */
	public Map<String, double[]> getSplitpointData() {
		return splitpointData;
	}

	/**
	 * Sets the splitpoint data.
	 *
	 * @param splitpointData the splitpoint data
	 */
	public void setSplitpointData(Map<String, double[]> splitpointData) {
		this.splitpointData = splitpointData;
	}
	
	/**
	 * Gets the covered.
	 *
	 * @return the covered
	 */
	public List<MAT<?>> getCovered() {
		return covered;
	}
	
	/**
	 * Sets the covered.
	 *
	 * @param covered the new covered
	 */
	public void setCovered(List<MAT<?>> covered) {
		this.covered = covered;
	}
	
	public String getFeatureName() {
		return "ft_TID" + getTrajectory().getTid() + 
				"_FT_" + StringUtils.join(ArrayUtils.toObject(getPointFeatures()), "_") +
				"_CLASS" + getTrajectory().getMovingObject();
	}

}
