package br.com.tarlis.mov3lets.method.qualitymeasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

public class ProportionQualityMeasure<MO> extends QualityMeasure {

	protected List<MAT<MO>> trajsFromClass;
	protected double GAMMA  = 0.0;
	protected double TAU = 0.5;
	
	public ProportionQualityMeasure(List<MAT<MO>> trajsFromClass, double tau, double gamma) {
		super();
		this.trajsFromClass = trajsFromClass;
		this.TAU = tau;
		this.GAMMA = gamma;
	}

	@Override
	public void assesQuality(Subtrajectory candidate, Random random) {
		/*
		 * STEP 1: VERIFY WHICH ARE THE TRAJECTORIES THAT CONTAIN THAT CANDIDATE FOR EACH DIMENSION
		 */
		
		List<List<Integer>> trajectories_with_candidate = new ArrayList<>();
		List<MAT<MO>> coveredInClass = new ArrayList<MAT<MO>>();
		
		for(double[] distances : candidate.getDistances()) {
			
			List<Integer> one_dimension_coverage = new ArrayList<>();
			Integer i=0;
			
			for(double distance:distances) {
				
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
			int b = this.trajsFromClass.size();
			proportions += (double) a / b;
			

			if (((double) a / b) > TAU)
				coveredInClass.add(this.trajsFromClass.get(j));
			
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
