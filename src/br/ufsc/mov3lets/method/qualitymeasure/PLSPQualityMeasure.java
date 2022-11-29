package br.ufsc.mov3lets.method.qualitymeasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.Pair;

import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * This quality class is a merge of F-Score and Class Poportion measures (LeftSidePureCVLigth and ProportionQualityMeasure).
 *
 * @param <MO> the generic type
 */
public class PLSPQualityMeasure<MO> extends LeftSidePureCVLigth<MO> {

//	/** The class trajectories. */
//	protected List<MAT<MO>> classTrajectories;
	/**
	 * Instantiates a new left side pure CV ligth.
	 *
	 * @param trajectories the trajectories
	 * @param samples      the samples
	 * @param sampleSize   the sample size
	 * @param medium       the medium
	 */
	public PLSPQualityMeasure(List<MAT<MO>> trajectories, int samples, double sampleSize, String medium) {
		super(trajectories, samples, sampleSize, medium);
	}
	
	/**
	 * Gets the splitpoints.
	 *
	 * @param candidate the candidate
	 * @param distances the distances
	 * @param target the target
	 * @param random the random
	 * @return the splitpoints
	 */
	public Map<String,double[]> getSplitpointsProportion(Subtrajectory candidate, double[][] distances, MO target, Random random) {
		
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

		double[] limits = new double[distances.length];
		for (int j = 0; j < distances.length; j++) {
			DescriptiveStatistics ds = new DescriptiveStatistics(targetDistances[j]);
			limits[j] = ds.getPercentile(25.0);
		}
		
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
	
	/**
	 * Count covered.
	 *
	 * @param targetDistances the target distances
	 * @param limits the limits
	 * @return the int
	 */
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
	
	/**
	 * Overridden method.
	 * 
	 * @see br.com.tarlis.mov3lets.method.qualitymeasure.QualityMeasure#assesQuality(br.com.tarlis.mov3lets.model.Subtrajectory,
	 *      java.util.Random).
	 * 
	 * @param candidate
	 * @param random
	 */
	public void assesQuality(Subtrajectory candidate, Random random) {

		double[][] distances = candidate.getDistances();
		MO target = (MO) candidate.getTrajectory().getMovingObject();
		
//		this.classTrajectories = trajectories.stream()
//				  .filter(c -> c.getMovingObject().equals(candidate.getTrajectory().getMovingObject()))
//				  .collect(Collectors.toList());

		Map<String,double[]> classSplitpointsData = getSplitpointsProportion(candidate, distances, target, random);
		Map<String, double[]> splitpointsData = getBestSplitpointsCV(candidate, distances, target, random);

		double f1 = 0;

		f1 = getFMeasure(distances, this.labels, splitpointsData, target);

		double[] maxDistances = getMaxDistances(distances);

		double dimensions = distances.length;

		/*
		 * It extracts several information about the quality
		 */
		Map<String, Double> data = new HashMap<>();

    	data.put("quality", 	(classSplitpointsData.get("proportions")[0] + f1) / 2);

    	data.put("proportion", 	classSplitpointsData.get("proportions")[0]);
    	data.put("p_target", 	classSplitpointsData.get("proportions")[1]);
    	data.put("p_nontarget", classSplitpointsData.get("proportions")[2]);
    	
		data.put("f1", f1);
		data.put("dimensions", 1.0 * dimensions);
		data.put("size", 1.0 * candidate.getSize());
		data.put("start", 1.0 * candidate.getStart());
		data.put("tid", 1.0 * candidate.getTrajectory().getTid());

		Quality quality = new LeftSidePureQuality();
		quality.setData(data);
		candidate.setQuality(quality);
		candidate.setSplitpoints(splitpointsData.get("mean"));
		candidate.setSplitpointData(splitpointsData);
		candidate.setMaxDistances(maxDistances);
	}
}
