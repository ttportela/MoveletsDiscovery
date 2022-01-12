package br.ufsc.mov3lets.method.qualitymeasure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.Pair;

import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class LeftSidePureCVLigth.
 *
 * @param <MO> the generic type
 */
public class LeftSidePureCVLigthEA<MO> extends LeftSidePureCVLigth<MO> {

	/**
	 * Instantiates a new left side pure CV ligth.
	 *
	 * @param trajectories the trajectories
	 * @param samples      the samples
	 * @param sampleSize   the sample size
	 * @param medium       the medium
	 */
	public LeftSidePureCVLigthEA(List<MAT<MO>> trajectories, int samples, double sampleSize, String medium) {
		super(trajectories, samples, sampleSize, medium);
//		this.trajectories = trajectories;
		this.classes = this.labels.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
	}

	/**
	 * Prune points.
	 *
	 * @param distances the distances
	 * @param target    the target
	 * @param labels    the labels
	 * @return the list
	 */ // TARLIS just here:
	public Pair<Integer, double[]> getBestSplitpoints(double[][] distances, MO target, List<MO> labels, double[] maxDistances) {

		List<double[]> targetDistances = new ArrayList<>();
		List<double[]> nonTargetDistances = new ArrayList<>();
		RealMatrix rm = new Array2DRowRealMatrix(distances);

		/*
		 * Select only the distance of the non-target label
		 */
		for (int i = 0; i < labels.size(); i++) {
			if (labels.get(i).equals(target))
				targetDistances.add(rm.getColumn(i));
			else
				nonTargetDistances.add(rm.getColumn(i));
		}

		/*
		 * Select only the distance of the target label
		 */
//		for (int i = 0; i < labels.size(); i++) {
//			if (!labels.get(i).equals(target))
//				nonTargetDistances.add(rm.getColumn(i));
//		}

//		// Remove candidates with DoubleMax values
		for (int i = 0; i < nonTargetDistances.size(); i++) {
			if (new DescriptiveStatistics(nonTargetDistances.get(i)).getMax() == MAX_VALUE)
				nonTargetDistances.remove(i--);
		}

		nonTargetDistances.sort(new Comparator<double[]>() {
			@Override
			public int compare(double[] o1, double[] o2) {
				double sum1 = 0.0, sum2 = 0.0;
				for (int i = 0; i < o1.length; i++) {
					sum1 += o1[i] / maxDistances[i];
					sum2 += o2[i] / maxDistances[i];
				}
				return Double.compare(sum1, sum2);
//				for (int i = 0; i < o1.length; i++) {
//					if ((i % 2) == 0) {
//						if (o1[i] < o2[i])
//							return -1;
//						else if (o2[i] < o1[i])
//							return +1;
//					} else {
//						if (o1[i] > o2[i])
//							return -1;
//						else if (o2[i] > o1[i])
//							return +1;
//					}
//				}
//				return 0;
			}
		});
		
//		/*
//		 * Remove repeated candidates
//		 */
//		for (int i = 1; i < nonTargetDistances.size(); i++) {
//			if (Arrays.equals(nonTargetDistances.get(i - 1), nonTargetDistances.get(i)))
//				nonTargetDistances.remove(i--);
//		}

		/*
		 * Iteratively remove all distances
		 */
//		Optional<Pair<MutableBoolean, double[]>> result;
//		Pair<MutableBoolean, double[]> pair, pairToVerify;
//		boolean willRemove;

		/*
		 * Remove repeated and greater candidates
		 */
//		for (int i = 1; i < nonTargetDistances.size(); i++) {
//			for (int j = i+1; j < nonTargetDistances.size(); j++) {
//				if (firstVectorGreaterThanTheSecond(nonTargetDistances.get(j),nonTargetDistances.get(i - 1)))
//					nonTargetDistances.remove(j--);	
//			}
//			
//			if (Arrays.equals(nonTargetDistances.get(i - 1), nonTargetDistances.get(i)))
//				nonTargetDistances.remove(i--);
//			else if (firstVectorGreaterThanTheSecond(nonTargetDistances.get(i),nonTargetDistances.get(i - 1)))
//				nonTargetDistances.remove(i--);
//		}

//		result = nonTargetDistances.stream().findFirst();

//		/* enquanto houver pares para explorar */
//		while (result.isPresent()) {
//
//			pair = result.get();
//			pair.getFirst().setValue(true);
//
//			for (int i = 0; i < nonTargetDistances.size(); i++) {
//
//				pairToVerify = nonTargetDistances.get(i);
//
//				willRemove = firstVectorGreaterThanTheSecond(pairToVerify.getSecond(), pair.getSecond());
//
//				if (willRemove)
//					nonTargetDistances.remove(i--);
//
//			}
//
//			result = nonTargetDistances.stream()
//					.filter((Pair<MutableBoolean, double[]> e) -> (e.getFirst().booleanValue() == false)).findFirst();
//
//		} // while ( result.isPresent() ){
		

//		int best = 0;
		double[] bestCandidate = new double[distances.length];
		int bestCount = -1;
		int currentCount;
		
		if (!nonTargetDistances.isEmpty()) {
			bestCandidate = nonTargetDistances.get(0);
			bestCount = countCovered(targetDistances, bestCandidate);
		}
			
//		Arrays.fill(bestCandidate, MAX_VALUE);
		for (int i = 1; i < nonTargetDistances.size(); i++) {
			// Remove repeated candidates
			if (Arrays.equals(nonTargetDistances.get(i - 1), nonTargetDistances.get(i))) {
				nonTargetDistances.remove(i--);
				continue;
			}
			
			double[] currentCandidate = nonTargetDistances.get(i);
			currentCount = countCovered(targetDistances, currentCandidate);

			if (currentCount > bestCount) {
				bestCount = currentCount;
				bestCandidate = currentCandidate;
			} else if (currentCount <= bestCount) { break; }
			
//			if (firstVectorGreaterThanTheSecond(currentCandidate, bestCandidate)) break;
		}
		
//		int index = 0, low = 0, high = nonTargetDistances.size()-1;
//		while (low <= high) {
//	        int mid = low  + ((high - low) / 2);
//	        int mid_l = low  + ((mid - low) / 2);
//	        int mid_h = mid  + ((high - mid) / 2);
//	        
//	        int l_covered = countCovered(targetDistances, nonTargetDistances.get(mid_l));
//	        int h_covered = countCovered(targetDistances, nonTargetDistances.get(mid_h));
//	        
//	        if (l_covered < h_covered) {
//	            low = mid + 1;
//	            index = mid_h;
//	        } else { // if (l_covered >= h_covered) {
//	            high = mid - 1;
//	            index = mid_l;
//	        } 
////	        else if (l_covered == h_covered) {
////	            index = mid;
////	            break;
////	        }
//	    }
//
//		double[] bestCandidate = nonTargetDistances.get(index);
//		int bestCount = countCovered(targetDistances, bestCandidate);
		return new Pair<Integer, double[]>(bestCount, bestCandidate);
	}

	/**
	 * Gets the best splitpoints CV.
	 *
	 * @param candidate the candidate
	 * @param distances the distances
	 * @param target    the target
	 * @param random    the random
	 * @return the best splitpoints CV
	 */
	public Map<String, double[]> getBestSplitpointsCV(Subtrajectory candidate, double[][] distances, MO target,
			Random random, double[] maxDistances) {

		List<Pair<Integer, double[]>> results = new ArrayList<>();
		Pair<double[][], List<MO>> chosePoints = null;

		for (int i = 0; i < this.samples; i++) {
			
			chosePoints = choosePointsStratified(distances, labels, target, random);

			Pair<Integer, double[]> bestSplitpoints = getBestSplitpoints(chosePoints.getFirst(), target, chosePoints.getSecond(), maxDistances);

			results.add(bestSplitpoints);
		}
//		System.out.println(ct);System.exit(1);

		// Agora resultado vai ser usado para acumular os resultados parciais
		double[][] splitPoints = new double[distances.length][this.samples];

		Double minCovered = MAX_VALUE;

		for (int i = 0; i < this.samples; i++) {

			for (int j = 0; j < results.get(i).getSecond().length; j++) {
				splitPoints[j][i] += results.get(i).getSecond()[j];
			}

			if (results.get(i).getFirst() < minCovered)
				minCovered = results.get(i).getFirst() * 1.0d;

		}

		double[] splitPointsMean = new double[distances.length];

		for (int i = 0; i < distances.length; i++) {
			DescriptiveStatistics ds = new DescriptiveStatistics(splitPoints[i]);
			splitPointsMean[i] = ds.getMean();
		}

		Map<String, double[]> splitpointsData = new HashMap<>();
		splitpointsData.put("mean", splitPointsMean);

		return splitpointsData;
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

		double[] maxDistances = getMaxDistances(distances);

		Map<String, double[]> splitpointsData = getBestSplitpointsCV(candidate, distances, target, random, maxDistances);

		double f1 = 0;

		f1 = getFMeasure(distances, this.labels, splitpointsData, target);

		double dimensions = distances.length;

		/*
		 * It extracts several information about the quality
		 */
		Map<String, Double> data = new HashMap<>();

		data.put("quality", f1);
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
