package br.com.tarlis.mov3lets.method.qualitymeasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.Pair;

import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

public class ProportionQualityMeasure<MO> extends QualityMeasure<MO> {

	protected double TAU = 0.5;
	
	public ProportionQualityMeasure(List<MAT<MO>> trajectories, double tau) {
		super(trajectories, 0, 0.0, "");
		this.TAU = tau;
	}

	public ProportionQualityMeasure(List<MAT<MO>> trajectories, int samples, double sampleSize, String medium) {
		super(trajectories, samples, sampleSize, medium);
		this.trajectories = trajectories;
	}

	@Override
	public void assesQuality(Subtrajectory candidate, Random random) {

		int outTotal = (int) this.trajectories.stream().filter(e -> !candidate.getTrajectory().getMovingObject().equals(e.getMovingObject())).count();
		if (outTotal == 0) {
			assesClassQuality(candidate, TAU, random);
			return;
		}
		
		int inTotal  = (int) this.trajectories.stream().filter(e ->  candidate.getTrajectory().getMovingObject().equals(e.getMovingObject())).count();
		
		assesQuality(candidate, random, inTotal, outTotal);
		
	}
	
	public int countCovered(double[][] targetDistances, double[] limits){
		
		int count = 0;
		
		for (int i = 0; i < targetDistances[0].length; i++) {
			
			for (int j = 0; j < limits.length; j++) {
				if (targetDistances[j][i] <= limits[j])
					count++;
			}
			
		}
		
		return count;
	}
	
	public boolean isCovered(double[] point, double[] limits, double gamma){
		
		for (int i = 0; i < limits.length; i++) {
			if (point[i] > (limits[i]*gamma))
				return false;
		}
		
		return true;
	}
	
	public Map<String,double[]> getSplitpoints(Subtrajectory candidate, double[][] distances, MO target, Random random) {
		
		List<Pair<double[],double[]>> results = new ArrayList<>();		
		Pair<double[][],List<MO>> chosePoints = null;
		
		for (int i = 0; i < this.samples; i++) {			
			
			//chosePoints = choosePoints(distances, labels, random);
			chosePoints = choosePointsStratified(distances, this.labels, target, random);
			
			int split = chosePoints.getSecond().lastIndexOf(target);
			RealMatrix rm = new Array2DRowRealMatrix(chosePoints.getFirst());
			
			/* Select only the distance of the target label
			 * */
			double[][] targetDistances = rm.getSubMatrix(0, distances.length-1, 0, split).getData(); //new double[distances.length][split+1];
			/* Select only the distance of the non-target label
			 * */
			double[][] nonTargetDistances = rm.getSubMatrix(0, distances.length-1, split+1, chosePoints.getSecond().size()-1).getData(); // new double[distances.length][chosePoints.getSecond().size()-(split+1)];
			
			double[] limits = new double[distances.length];
			for (int j = 0; j < distances.length; j++) {
				DescriptiveStatistics ds = new DescriptiveStatistics(targetDistances[j]);
				limits[j] = ds.getMean();
//				limits[j] = ds.apply(new Percentile(0.5));
			}

			double matchTarget 		= countCovered(targetDistances,    limits);
			double matchNonTarget 	= countCovered(nonTargetDistances, limits);
				
//			/* Step 3: Choose the best rectangle
//			 * */
			double proportionTarget 	= matchTarget 	 / ((double) (targetDistances[0].length * distances.length));
			double proportionNonTarget 	= matchNonTarget / ((double) (nonTargetDistances[0].length * distances.length));
//			double p = proportionTarget; 					   // D
//			double p = 1.0 - proportionNonTarget; 			   // I
			double p = proportionTarget / proportionNonTarget; // PT
//			double p = proportionNonTarget / proportionTarget; // PNT
			
			results.add(new Pair<double[],double[]>(new double[]{p, proportionTarget, proportionNonTarget}, limits));
		}	
		
		int split = chosePoints.getSecond().lastIndexOf(target);
		RealMatrix rm = new Array2DRowRealMatrix(chosePoints.getFirst());
		
		/* Select only the distance of the target label
		 * */
		double[][] targetDistances = rm.getSubMatrix(0, distances.length-1, 0, split).getData(); //new double[distances.length][split+1];
		/* Select only the distance of the non-target label
		 * */
		double[][] nonTargetDistances = rm.getSubMatrix(0, distances.length-1, split+1, chosePoints.getSecond().size()-1).getData(); // new double[distances.length][chosePoints.getSecond().size()-(split+1)];
		
		double[] limits = new double[distances.length];
		for (int j = 0; j < distances.length; j++) {
			DescriptiveStatistics ds = new DescriptiveStatistics(targetDistances[j]);
//			limits[j] = ds.getMean();
			limits[j] = ds.getPercentile(25.0);
		}

		double matchTarget 		= countCovered(targetDistances,    limits);
		double matchNonTarget 	= countCovered(nonTargetDistances, limits);
			
//		/* Step 3: Choose the best rectangle
//		 * */
		double proportionTarget 	= matchTarget 	 / ((double) (targetDistances[0].length * distances.length));
		double proportionNonTarget 	= matchNonTarget / ((double) (nonTargetDistances[0].length * distances.length));
		double p = proportionTarget / proportionNonTarget;
//		double p = proportionNonTarget / proportionTarget;
//		double p = 1.0 - proportionNonTarget;
		
		// Agora resultado vai ser usado para acumular os resultados parciais
		double[][] splitPoints = new double[distances.length][this.samples];
		
		double proportions[] = new double[] {0.0, 0.0, 0.0};
		
		for (int i = 0; i < this.samples; i++) {
			
			for (int j = 0; j < results.get(i).getSecond().length; j++) {
				splitPoints[j][i] += results.get(i).getSecond()[j];
			}			
			
			proportions[i] += results.get(i).getFirst()[i];
			
		}
		
		for (int i = 0; i < this.samples; i++) {
			proportions[i] = proportions[i] / (double) this.samples;
		}

		double[] splitPointsMean = new double[distances.length];		
		
		for (int i = 0; i < distances.length; i++) {
			DescriptiveStatistics ds = new DescriptiveStatistics(splitPoints[i]);
			splitPointsMean[i] = ds.getMean();
		}
		
		Map <String,double[]> splitpointsData = new HashMap<>();
		splitpointsData.put("mean", 		splitPointsMean);		
		splitpointsData.put("proportions", 	proportions);
		
		return splitpointsData;
	}
	
//	public Map<String,double[]> getSplitpoints2(Subtrajectory candidate, double[][] distances, MO target, Random random) {
//				
//		List<Integer> positive = new ArrayList<>();
//		List<Integer> negative = new ArrayList<>();
//		for (int i = 0; i < labels.size(); i++) {
//			if (labels.get(i).equals(target))
//				positive.add(i);
//			else
//				negative.add(i);
//		}
//		
//		List<Integer> choosed = new ArrayList<>();
//		choosed.addAll(positive.subList(0, positive.size() ));
//		choosed.addAll(negative.subList(0, negative.size() ));
//		
//		// Selecionar os dados
//		double[][] newDistances = new double[distances.length][choosed.size()];
//		List<MO> newLabels = new ArrayList<>();
//		
//		for (int i = 0; i < choosed.size(); i++) {
//			for (int j = 0; j < newDistances.length; j++) {
//				newDistances[j][i] = distances[j][choosed.get(i)];
//			}
//			newLabels.add(labels.get(choosed.get(i)));			
//		}
//		
//		Pair<double[][],List<MO>> chosePoints = new Pair<>(newDistances,newLabels);
//		
//		int split = chosePoints.getSecond().lastIndexOf(target);
//		RealMatrix rm = new Array2DRowRealMatrix(chosePoints.getFirst());
//			
//		/* Select only the distance of the target label
//		 * */
//		double[][] targetDistances = rm.getSubMatrix(0, distances.length-1, 0, split).getData(); //new double[distances.length][split+1];
//		/* Select only the distance of the non-target label
//		 * */
//		double[][] nonTargetDistances = rm.getSubMatrix(0, distances.length-1, split+1, chosePoints.getSecond().size()-1).getData(); // new double[distances.length][chosePoints.getSecond().size()-(split+1)];
//		
//		double[] limits = new double[distances.length];
//		for (int j = 0; j < distances.length; j++) {
//			DescriptiveStatistics ds = new DescriptiveStatistics(targetDistances[j]);
//			limits[j] = ds.getMean();
////				limits[j] = ds.apply(new Percentile(0.5));
//		}
//
//		double matchTarget 		= countCovered(targetDistances,    limits);
//		double matchNonTarget 	= countCovered(nonTargetDistances, limits);
//			
////			/* Step 3: Choose the best rectangle
////			 * */
//		double proportionTarget 	= matchTarget 	 / ((double) (targetDistances[0].length * distances.length));
//		double proportionNonTarget 	= matchNonTarget / ((double) (nonTargetDistances[0].length * distances.length));
////		double p = proportionTarget / proportionNonTarget; // d
////		double p = proportionNonTarget / proportionTarget; // i
////		double p = proportionTarget; // pt
//		double p = 1.0 - proportionNonTarget; // npt
//		
//		double proportions[] = new double[] {proportionTarget, proportionNonTarget, p};
//
//		double[] splitPointsMean = new double[distances.length];		
//		
//		for (int i = 0; i < distances.length; i++) {
//			DescriptiveStatistics ds = new DescriptiveStatistics(chosePoints.getFirst()[i]);
//			splitPointsMean[i] = ds.getMean();
//		}
//		
//		Map <String,double[]> splitpointsData = new HashMap<>();
//		splitpointsData.put("mean", 		splitPointsMean);		
//		splitpointsData.put("proportions", 	proportions);
//		
//		return splitpointsData;
//	}
	
	protected void assesQuality(Subtrajectory candidate, Random random, int inTotal, int outTotal) {
		
		/*
		 * STEP 1: VERIFY WHICH ARE THE TRAJECTORIES THAT CONTAIN THAT CANDIDATE FOR EACH DIMENSION
		 */
		
		MO target = (MO) candidate.getTrajectory().getMovingObject();
		double[][] distances = candidate.getDistances();	
		
		Map<String,double[]> splitpointsData = getSplitpoints(candidate, distances, target, random);	
		double[] maxDistances = getMaxDistances(distances);
		
		List<MAT<MO>> coveredInClass = new ArrayList<MAT<MO>>();
//		List<MAT<MO>> coveredOutClass = new ArrayList<MAT<MO>>();
		
		/*
		 * STEP 2: Covered Trajectories
		 */
		RealMatrix rm = new Array2DRowRealMatrix(distances);
		double[] splitPoints = splitpointsData.get("mean");	
		for (int j = 0; j < distances[0].length; j++) {
			
			MAT<MO> T = this.trajectories.get(j);
			if (target.equals(T.getMovingObject()) && isCovered(rm.getColumn(j), splitPoints)) {
				coveredInClass.add(this.trajectories.get(j));
			} 
//			else {
//				coveredOutClass.add(this.trajectories.get(j));
//			}
			
		}
				
		Map<String, Double> data = new HashMap<>();

    	data.put("quality", 	splitpointsData.get("proportions")[0]);
    	data.put("p_target", 	splitpointsData.get("proportions")[1]);
    	data.put("p_nontarget", splitpointsData.get("proportions")[2]);
    	data.put("dimensions", 	1.0 * candidate.getPointFeatures().length );    	
    	data.put("size", 		1.0 * candidate.getSize() );
    	data.put("start", 		1.0 * candidate.getStart() );
    	data.put("tid", 		1.0 * candidate.getTrajectory().getTid() );
    	
    	ProportionQuality quality = new ProportionQuality();
    	quality.setData(data);	    
    	quality.setCoveredInClass((List) coveredInClass);	
		candidate.setQuality(quality);
		candidate.setSplitpoints(splitpointsData.get("mean"));
		candidate.setSplitpointData(splitpointsData);
		candidate.setMaxDistances(maxDistances);
	}
	
	public void assesClassQuality_Bkp(Subtrajectory candidate, double gamma, Random random) {
		/*
		 * STEP 1: VERIFY WHICH ARE THE TRAJECTORIES THAT CONTAIN THAT CANDIDATE FOR EACH DIMENSION
		 */
		
		List<List<Integer>> trajectories_with_candidate = new ArrayList<>();
		List<MAT<MO>> coveredInClass = new ArrayList<MAT<MO>>();
		
//		double[] limits = new double[candidate.getDistances().length];
//		for (int j = 0; j < candidate.getDistances().length; j++) {
//			DescriptiveStatistics ds = new DescriptiveStatistics(candidate.getDistances()[j]);
//			limits[j] = ds.apply(new Percentile(gamma));
////				limits[j] = ds.apply(new Percentile(0.5));
//		}
//		
//		double matchTarget 		= countCovered(candidate.getDistances(), limits);
//
//		RealMatrix rm = new Array2DRowRealMatrix(candidate.getDistances());
//		for (int j = 0; j < candidate.getDistances()[0].length; j++) {
//			if (isCovered(rm.getColumn(j), limits))
//				coveredInClass.add(this.trajectories.get(j));
//		}

		double[] splitPoints = new double[candidate.getDistances().length];	

		for (int i = 0; i < candidate.getDistances().length; i++) {
			DescriptiveStatistics ds = new DescriptiveStatistics(candidate.getDistances()[i]);
			splitPoints[i] = ds.getPercentile(gamma*100.0);			
		}

		for (int i = 0; i < candidate.getDistances().length; i++) {
			double[] distances = candidate.getDistances()[i];
			
			List<Integer> one_dimension_coverage = new ArrayList<>();
			Integer j=0;
			
			for(double distance : distances) {
				
				if(distance <= splitPoints[i])
					one_dimension_coverage.add(j);
				
				j++;
			}
			
			trajectories_with_candidate.add(one_dimension_coverage);
		}

			
		/*
		 * STEP 2: CALCULATE THE PROPORTION
		 */
		
		double proportions = 0.0;
//		List<Integer> intersection = new ArrayList<>();
//		intersection.addAll(trajectories_with_candidate.get(0));
		
		for (int j = 0; j < trajectories_with_candidate.size(); j++) {
			List<Integer> trajectories_per_dimension = trajectories_with_candidate.get(j);
						
			int a = trajectories_per_dimension.size();
			int b = this.trajectories.size();
			proportions += (double) a / b;
			

//			if (((double) a / b) > TAU) {
//				coveredInClass.add(this.trajectories.get(j));
//			}
//			intersection.retainAll(trajectories_per_dimension);
			
		}	
			
		double proportion = proportions / (double) trajectories_with_candidate.size();// matchTarget / (double) (candidate.getDistances().length * this.trajectories.size());
		
		
		/*
		 * STEP 2.1: Covered Trajectories
		 */
//		for (Integer j : intersection) {
//			coveredInClass.add(this.trajectories.get(j));
//		}
		if (proportion > TAU) {
			RealMatrix rm = new Array2DRowRealMatrix(candidate.getDistances());
			for (int j = 0; j < candidate.getDistances()[0].length; j++) {
				
				if (isCovered(rm.getColumn(j), splitPoints, gamma)) {
					coveredInClass.add(this.trajectories.get(j));
				} 
	//			else {
	//				coveredOutClass.add(this.trajectories.get(j));
	//			}
				
			}
		}
		
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
	
	public void assesClassQuality_bkp2(Subtrajectory candidate, double gamma, Random random) {
		/*
		 * STEP 1: VERIFY WHICH ARE THE TRAJECTORIES THAT CONTAIN THAT CANDIDATE FOR EACH DIMENSION
		 */
		List<MAT<MO>> coveredInClass = new ArrayList<MAT<MO>>();

		double[] splitPoints = new double[candidate.getDistances().length];	

		for (int i = 0; i < candidate.getDistances().length; i++) {
			DescriptiveStatistics ds = new DescriptiveStatistics(candidate.getDistances()[i]);
			splitPoints[i] = ds.getPercentile(gamma*100.0);			
		}
		

		double matchTarget 		= countCovered(candidate.getDistances(), splitPoints);
		double proportion 		= matchTarget / (double) candidate.getDistances()[0].length;
		

		if (proportion > TAU) {
			RealMatrix rm = new Array2DRowRealMatrix(candidate.getDistances());
			for (int j = 0; j < candidate.getDistances()[0].length; j++) {
				if (isCovered(rm.getColumn(j), splitPoints))
					coveredInClass.add(this.trajectories.get(j));
			}
		}
		
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
	
	public void assesClassQuality(Subtrajectory candidate, double gamma, Random random) {
		/*
		 * STEP 1: VERIFY WHICH ARE THE TRAJECTORIES THAT CONTAIN THAT CANDIDATE FOR EACH DIMENSION
		 */
		List<MAT<MO>> coveredInClass = new ArrayList<MAT<MO>>();
		
		double[][] distances = candidate.getDistances();
		double[] splitPoints = getMaxDistances(candidate.getDistances());//new double[candidate.getDistances().length];	
		
		double proportion  = 0.0;
		
		double pZero = 0.0;
		int[] freq = new int[distances[0].length];
		
		for (int i = 0; i < distances.length; i++) {
			splitPoints[i] = splitPoints[i] * gamma;
			double total = 0.0;	
			double sum = 0.0;
			double pSum = 0.0;
			
			for (int j = 0; j < distances[i].length; j++) {
				if (distances[i][j] != MAX_VALUE) {
					if (distances[i][j] <= splitPoints[i])
						sum += distances[i][j];

					if (distances[i][j] == 0.0) {
						pSum += 1.0;
						freq[j] += 1;
					}
					
					total += distances[i][j];
				}
			}
			
			proportion += sum / total;
			pZero += pSum / (double) distances[i].length;
			
		}
		
		proportion 		= proportion / (double) distances.length;
		pZero 			= pZero / (double) distances.length;
		
		if (pZero > TAU) {
			for (int j = 0; j < freq.length; j++) {
				if (freq[j] > distances[0].length)
					coveredInClass.add(this.trajectories.get(j));
			}
		}
		
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