package br.com.tarlis.mov3lets.method.output.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.method.qualitymeasure.LeftSidePureQuality;
import br.com.tarlis.mov3lets.method.qualitymeasure.Quality;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Point;
import br.com.tarlis.mov3lets.model.Subtrajectory;


public class SubtrajectoryGSON {
	
	private int start;

	private int end;
 
	private int trajectory;
	
	private String label;

//	private HashMap<String, IFeature> features;
	
//	private List<HashMap<String, IFeature>> points_with_only_the_used_features;
		
	private HashMap<String, Double> maxValues;
	
	private int[] pointFeatures;
	
	private double[] splitpoints;
	
	private double[][] distances;
	
	private int[] positions;
	
	private List<Point> data;
	
	private Map<String,Double> quality;
	
	public SubtrajectoryGSON(int start, int end, int trajectory, String label,
			int[] pointFeatures, double[] splitpoints, double[][] distances, 
			List<Subtrajectory> bestAlignments, Quality quality, List<Point> data) {
		super();
		this.start = start;
		this.end = end;
		this.trajectory = trajectory;
		this.label = label;
//		this.features = features;		
		this.distances = distances;
		this.positions = bestAlignments.stream().mapToInt(e -> (e!=null) ? e.getStart() : -1).toArray();
		this.pointFeatures = pointFeatures;
		this.splitpoints = splitpoints;
		this.quality = quality.getData();
		this.maxValues = new HashMap<>();
//		this.points_with_only_the_used_features = only_used_features;
		this.data = data;
		
		// TODO Features?
//		for (FeatureComparisonDesc featureComparisonDesc : description.getPointComparisonDesc().getFeatureComparisonDesc()) {
//			maxValues.put(featureComparisonDesc.getText(), featureComparisonDesc.getMaxValue());				
//		}
//
//		for (FeatureComparisonDesc featureComparisonDesc : description.getSubtrajectoryComparisonDesc().getFeatureComparisonDesc()) {
//			maxValues.put(featureComparisonDesc.getText(), featureComparisonDesc.getMaxValue());				
//		}
	}
	
	public SubtrajectoryGSON(int start, int trajectory, String label, int[] pointFeatures, double [] splitpoints, 
			double[][] distances, List<Subtrajectory> bestAlignments, Quality quality) {
		super();
		this.start = start;
		this.trajectory = trajectory;
		this.label = label;	
		this.pointFeatures = pointFeatures;
		this.splitpoints = splitpoints;
		this.distances = distances;
		this.positions = bestAlignments.stream().mapToInt(e -> (e!=null) ? e.getStart() : -1).toArray();
		this.quality = quality.getData();
//		this.points_with_only_the_used_features = only_used_features;
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

	public int getTrajectory() {
		return trajectory;
	}

	public void setTrajectory(int trajectory) {
		this.trajectory = trajectory;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<Point> getData() {
		return data;
	}
	
	public void setData(List<Point> data) {
		this.data = data;
	}
	
//	public void setUsedFeatures(List<HashMap<String, Feature>> Data) {
//		this.points_with_only_the_used_features = Data;
//	}
//	
//	public List<HashMap<String, Feature>> getOnlyUsedFeatures() {
//		return points_with_only_the_used_features;
//	}
	
	public double[] getSplitpoints() {
		return splitpoints;
	}
	
	public void setSplitpoints(double[] splitpoints) {
		this.splitpoints = splitpoints;
	}

//	public HashMap<String, Feature> getFeatures() {
//		return features;
//	}
//
//	public void setFeatures(HashMap<String, Feature> features) {
//		this.features = features;
//	}

	public double[][] getDistances() {
		return distances;
	}

	public void setDistances(double[][] distances) {
		this.distances = distances;
	}

	public Map<String, Double> getQuality() {
		return quality;
	}
	
	public void setQuality(Map<String, Double> quality) {
		this.quality = quality;
	}
	
	public int[] getPointFeatures() {
		return pointFeatures;
	}

	public void setPointFeatures(int[] pointFeatures) {
		this.pointFeatures = pointFeatures;
	}
	
	public Subtrajectory toSubtrajectory(List<MAT<?>> trajectories){
		MAT<?> t = trajectories.stream().filter(e -> e.getTid() == this.trajectory).collect(Collectors.toList()).get(0);
		
		Subtrajectory s = new Subtrajectory(start, end, t, pointFeatures, distances[0].length);
		
		LeftSidePureQuality lspq = new LeftSidePureQuality();
		lspq.setData(quality);		
		s.setDistances(distances);		
		s.setQuality(lspq);
		s.setSplitpoints(splitpoints);		
		
		return s;
	}

}
