package br.com.tarlis.mov3lets.method.qualitymeasure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

public class ProportionQualityMeasure<MO> extends QualityMeasure {

	protected List<MAT<MO>> trajectories;
	protected double TAU = 0.5;
	
	public ProportionQualityMeasure(List<MAT<MO>> trajectories, double tau) {
		super();
		this.trajectories = trajectories;
		this.TAU = tau;
	}

	public ProportionQualityMeasure(List<MAT<MO>> trajectories) {
		super();
		this.trajectories = trajectories;
	}

	@Override
	public void assesQuality(Subtrajectory candidate, Random random) {

		int outTotal = (int) this.trajectories.stream().filter(e -> !candidate.getTrajectory().getMovingObject().equals(e.getMovingObject())).count();
		if (outTotal == 0) {
			assesClassQuality(candidate, random);
			return;
		}
		
		int inTotal  = (int) this.trajectories.stream().filter(e ->  candidate.getTrajectory().getMovingObject().equals(e.getMovingObject())).count();
		
		assesQuality(candidate, random, inTotal, outTotal);
		
	}
	
	protected void assesQuality(Subtrajectory candidate, Random random, int inTotal, int outTotal) {
		
		/*
		 * STEP 1: VERIFY WHICH ARE THE TRAJECTORIES THAT CONTAIN THAT CANDIDATE FOR EACH DIMENSION
		 */
		
		MO target = (MO) candidate.getTrajectory().getMovingObject();
		double[][] distances = candidate.getDistances();	
		
		double[] maxDistances = getMaxDistances(distances);
		
//		List<List<Integer>> trajectories_with_candidate = new ArrayList<>();
		List<MAT<MO>> coveredInClass = new ArrayList<MAT<MO>>();
//		List<MAT<MO>> coveredOutClass = new ArrayList<MAT<MO>>();
		
		int[] in  = new int[distances[0].length];
		int[] out = new int[distances[0].length];
		
//		double splitPoints = 0.0; // new double[distances.length][inTotal];
		double[] splitPointsMean = new double[distances.length];	
		
		for (int j = 0; j < distances.length; j++) {
			
//			List<Integer> one_dimension_coverage = new ArrayList<>();
			Integer i=0;
			
			DescriptiveStatistics ds = new DescriptiveStatistics(distances[j]);
			double GAMMA = ds.apply(new Percentile(0.5));
			
			splitPointsMean[j] = GAMMA;
			
			for(double distance : distances[j]) {
				
				if(distance <= GAMMA) {
//					one_dimension_coverage.add(i);
					
					if (target.equals(this.trajectories.get(i).getMovingObject()))
						in[j]++;
					else 
						out[j]++;
				}
				
				i++;
			}
			
//			trajectories_with_candidate.add(one_dimension_coverage);
		}		
			
		/*
		 * STEP 2: CALCULATE THE PROPORTION
		 */
		
		double proportionsIn = 0.0, proportionsOut = 0.0;
		
		for (int j = 0; j < distances[0].length; j++) {
//			List<Integer> trajectories_per_dimension = trajectories_with_candidate.get(j);
			
			MAT<MO> T = this.trajectories.get(j);
			if (target.equals(T.getMovingObject())) {
				proportionsIn  += (double) in[j] / inTotal;
				
				if (((double) in[j] / inTotal) > TAU) {
					coveredInClass.add(this.trajectories.get(j));

//					int k = 0;
//					for (int i = 0; i < distances.length; i++)
//						splitPoints[i][k++] += distances[i][j];
				}
				
			} else {
				proportionsOut += (double) out[j] / outTotal;

//				if (((double) out[j] / outTotal) > TAU) 
//					coveredOutClass.add(this.trajectories.get(j));
				
			}
			
		}	
			
		double proportion_in  = proportionsIn / (distances.length * 1.0d);
		double proportion_out = proportionsOut / (distances.length * 1.0d);
		
		/*
		 * STEP 3: Split Points covered.
		 */
//		double[] splitPointsMean = new double[distances.length];		
//		
//		for (int i = 0; i < distances.length; i++) {
//			DescriptiveStatistics ds = new DescriptiveStatistics(splitPoints[i]);
//			splitPointsMean[i] = ds.getMean();
//		}
		
		Map <String,double[]> splitpointsData = new HashMap<>();
		splitpointsData.put("mean", splitPointsMean);
		
		
		Map<String, Double> data = new HashMap<>();

    	data.put("quality", proportion_in / proportion_out);
    	data.put("p_in", proportion_in);
    	data.put("p_out", proportion_out);
    	data.put("dimensions", 1.0 * candidate.getPointFeatures().length );    	
    	data.put("size", 1.0 * candidate.getSize() );
    	data.put("start", 1.0 * candidate.getStart() );
    	data.put("tid", 1.0 * candidate.getTrajectory().getTid() );
    	
    	ProportionQuality quality = new ProportionQuality();
    	quality.setData(data);	    
    	quality.setCoveredInClass((List) coveredInClass);	
		candidate.setQuality(quality);
		candidate.setSplitpoints(splitpointsData.get("mean"));
		candidate.setSplitpointData(splitpointsData);
		candidate.setMaxDistances(maxDistances);
	}
	
	public void assesClassQuality(Subtrajectory candidate, Random random) {
		/*
		 * STEP 1: VERIFY WHICH ARE THE TRAJECTORIES THAT CONTAIN THAT CANDIDATE FOR EACH DIMENSION
		 */
		
		List<List<Integer>> trajectories_with_candidate = new ArrayList<>();
		List<MAT<MO>> coveredInClass = new ArrayList<MAT<MO>>();
		
		for(double[] distances : candidate.getDistances()) {
			DescriptiveStatistics ds = new DescriptiveStatistics(distances);
			double GAMMA = ds.apply(new Percentile(0.25));
			
			List<Integer> one_dimension_coverage = new ArrayList<>();
			Integer i=0;
			
			for(double distance : distances) {
				
				if(distance <= GAMMA)
					one_dimension_coverage.add(i);
				
				i++;
			}
			
			trajectories_with_candidate.add(one_dimension_coverage);
		}

			
		/*
		 * STEP 2: CALCULATE THE PROPORTION
		 */
		
		double proportions = 0.0;
		
		for (int j = 0; j < trajectories_with_candidate.size(); j++) {
			List<Integer> trajectories_per_dimension = trajectories_with_candidate.get(j);
						
			int a = trajectories_per_dimension.size();
			int b = this.trajectories.size();
			proportions += (double) a / b;
			

			if (((double) a / b) > TAU) {
				coveredInClass.add(this.trajectories.get(j));
			
			}
			
		}	
			
		double proportion = proportions/trajectories_with_candidate.size();
		
		/*
		 * STEP 3: IF THE CANDIDATE COVERS ONLY LESS THAN HALF OF THE TRAJECTORIES, THEN ABORT IT.
		 */
		
//		if(proportion<0.5) {
//			return -1.0;
//		}
		
		Map<String, Double> data = new HashMap<>();
		
    	data.put("proportion", proportion);
    	data.put("dimensions", 1.0 * candidate.getPointFeatures().length );    	
    	data.put("size", 1.0 * candidate.getSize() );
    	data.put("start", 1.0 * candidate.getStart() );
    	data.put("tid", 1.0 * candidate.getTrajectory().getTid() );
    	
    	ProportionQuality quality = new ProportionQuality();
    	quality.setData(data);	    
    	quality.setCoveredInClass((List) coveredInClass);	
		candidate.setQuality(quality);
	}

}