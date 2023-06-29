/**
 * 
 */
package br.ufsc.mov3lets.method.discovery.deprecated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;

import br.ufsc.mov3lets.method.discovery.UltraMoveletsDiscovery;
import br.ufsc.mov3lets.method.discovery.structures.TrajectoryDiscovery;
import br.ufsc.mov3lets.method.filter.OverlappingFeaturesFilter;
import br.ufsc.mov3lets.method.qualitymeasure.FrequentQualityMeasure;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class HiperPivotsMoveletsDiscovery.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class Hiper2MoveletsDiscovery<MO> extends UltraMoveletsDiscovery<MO> implements TrajectoryDiscovery {
	
	protected double rel_tau;
	
	/**
	 * Instantiates a new hiper pivots movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor the descriptor
	 */
	public Hiper2MoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajectory, trajsFromClass, data, train, test, qualityMeasure, descriptor);

		TAU 	= getDescriptor().hasParam("tau")? getDescriptor().getParamAsDouble("tau") : 0.9;
		BU 		= getDescriptor().hasParam("bucket_slice")? getDescriptor().getParamAsDouble("bucket_slice") : 0.1;
	}
	
	/**
	 * Movelets discovery.
	 *
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param minSize the min size
	 * @param maxSize the max size
	 * @param random the random
	 * @return the list
	 */
	public List<Subtrajectory> moveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajectories, int minSize, int maxSize, Random random) {
		
		//List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();
		
		int n = trajectory.getPoints().size();

		minSize = minSize(minSize, n);
		maxSize = maxSize(maxSize, minSize, n);
		
		if( minSize <= 1 ) minSize = 1;
		
		this.frequencyMeasure = new FrequentQualityMeasure<MO>(this.trajsFromClass);
		//this.bestFilter = new OverlappingFeaturesFilter(0.0, TAU, this.bucket);  // ULTRA-C (default), TAU default = 0.0 min quality

		// It starts with the base case
		addStats("Class", trajectory.getMovingObject()); 
		addStats("Trajectory", trajectory.getTid());
		addStats("Trajectory Size", n);			

		List<Subtrajectory> candidatesByProp = findPivotCandidates(trajectory, this.trajsFromClass, minSize);
		
		calculateProportion(candidatesByProp, random);
		// Relative TAU based on the higher proportion:
		this.rel_tau = relativeFrequency(candidatesByProp);
//		int bs = bucketSize(candidatesByProp.size());

		int i = 1;
		List<Subtrajectory> bestCandidates = new ArrayList<Subtrajectory>();
		do {
			// 1 - filter pivots by proportion
			candidatesByProp = filterByProportion(candidatesByProp, rel_tau, candidatesByProp.size());
			
			// 2 - calculate the pivots by quality
			for (Subtrajectory subtrajectory : candidatesByProp) {
				computeDistances(subtrajectory, trajectories);
				assesQuality(subtrajectory, random);
			}
			total_size += candidatesByProp.size();
			
			// 3 - grow the pivots also doing 1 and 2.
			for(Subtrajectory candidate : candidatesByProp) {
				bestCandidates.add(growPivot(candidate, trajectory, trajectories, minSize+1, maxSize, random, null));
			}
			
			// 4 - filter in the end by quality
			this.bestFilter = new OverlappingFeaturesFilter(0.0, 0.0, this.bucket);
			bestCandidates = this.bestFilter.filter(bestCandidates);
			
			// 5 - recover from bucket in case it do not find movelets:
			if (bestCandidates.isEmpty()) {
				i++;
				this.rel_tau = this.rel_tau / 2.0; // use half of relative tau in each extra iteration.
				candidatesByProp = this.bucket;
				this.bucket = new ArrayList<Subtrajectory>();
			}
			
		} while (bestCandidates.isEmpty() && !this.bucket.isEmpty());

		addStats("Iterations", i);
		addStats("TAU", rel_tau);
//		addStats("Bucket Size", bs);
		
		addStats("Number of Candidates", total_size);
		
		addStats("Total of Movelets", bestCandidates.size());
		addStats("Max Size", this.maxSizeOfCandidates);
		addStats("Used Features", this.currentMaxCombinationOfFeatures);
		
		progressBar.plus(getStats());
				
		return bestCandidates;

	}

	/**
	 * Grow pivots (recursively).
	 *
	 * @param candidatesOfSize the candidates of size
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param base the base
	 * @param newSize the new size
	 * @param size the size
	 * @param maxSize 
	 * @param random 
	 * @return the list
	 */
	public Subtrajectory growPivot(Subtrajectory candidate, MAT<MO> trajectory, List<MAT<MO>> trajectories, 
			int size, int maxSize, Random random, SortedSet<Integer> trajectory_marks) {
				
		Subtrajectory subtrajectoryOfSize = growSize(candidate, trajectory, trajectories, size, false, random, trajectory_marks);
		Subtrajectory subtrajectoryOfFeatures = growFeatures(candidate, trajectory, trajectories, random);

		subtrajectoryOfSize = subtrajectoryOfSize.best(subtrajectoryOfFeatures);
		
		if (subtrajectoryOfSize.equals(candidate)) {
//			this.bucket.add(subtrajectoryOfSize);
			return candidate;
		} else {
			if (size < maxSize) {
				this.maxSizeOfCandidates = Integer.max(size, this.maxSizeOfCandidates);
				return growPivot(subtrajectoryOfSize, trajectory, trajectories, size+1, maxSize, random, trajectory_marks);
			} else
				return subtrajectoryOfSize;
		}
	}

	/**
	 * Builds the new size.
	 *
	 * @param candidate the candidate
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param mdist the mdist
	 * @param size the size
	 * @param left the left
	 * @param trajectory_marks 
	 * @return the subtrajectory
	 */
	public Subtrajectory growSize(Subtrajectory candidate, MAT<MO> trajectory, 
			List<MAT<MO>> trajectories, int size, boolean left, Random random, SortedSet<Integer> trajectory_marks) {
		
		int start = candidate.getStart() - (left? 1 : 0);
		int end   = candidate.getEnd()   + (left? 0 : 1);
		
		if ((start < 0 || end > trajectory.getPoints().size()-1) || (trajectory_marks != null && !inRange(start, end, trajectory_marks)))
			return candidate;
		
		total_size += 1;

		List<Point> points = trajectory.getPoints().subList(start, end+1);
		Subtrajectory subtrajectory = instantiateCandidate(start, end, 
				trajectory, trajectories, candidate.getPointFeatures(), candidate.getK(), points);
		
		// 1 - asses proportion
		computeDistances(subtrajectory, this.trajsFromClass);
		this.frequencyMeasure.assesClassQuality(subtrajectory, maxDistances, random);
		if (subtrajectory.getQuality().getData().get("quality") < this.rel_tau) {
			return candidate;
		}
		
		// 2 - asses quality:
		computeDistances(subtrajectory, trajectories);
		assesQuality(subtrajectory, random);
		
		return subtrajectory.best(candidate);
	}

	/**
	 * Builds the new size.
	 *
	 * @param candidate the candidate
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param random the random method
	 * @return the subtrajectory
	 */
	public Subtrajectory growFeatures(Subtrajectory candidate, MAT<MO> trajectory, 
			List<MAT<MO>> trajectories, Random random) {
		int combSize = candidate.getPointFeatures().length+1;
		if (combSize > this.maxNumberOfFeatures)
			return candidate;
		
		if (combSize > this.currentMaxCombinationOfFeatures) {
			addCombinations(combSize, combSize);
		}
		
		ArrayList<Integer> validCombs = new ArrayList<Integer>();
		for (int i = 0; i < combinations.length; i++) {
			if (combinations[i].length == combSize && 
				// combination starts with:
				Arrays.equals(candidate.getPointFeatures(), Arrays.copyOfRange(combinations[i], 0, combSize-1))) {
				validCombs.add(i);
			}
		}
		
		Subtrajectory subtrajectoryOfFeature = candidate;
		for (int k : validCombs) {
			List<Point> points = trajectory.getPoints().subList(candidate.getStart(), candidate.getEnd()+1);
			Subtrajectory subtrajectory = instantiateCandidate(candidate.getStart(), candidate.getEnd(), 
					trajectory, trajectories, combinations[k], k, points);
			
			total_size += 1;
			
			// 1 - asses proportion
			computeDistances(subtrajectory, this.trajsFromClass);
			this.frequencyMeasure.assesClassQuality(subtrajectory, maxDistances, random);
			if (subtrajectory.getQuality().getData().get("quality") >= this.rel_tau) {
			
				// 2 - asses quality:
				computeDistances(subtrajectory, trajectories);
				assesQuality(subtrajectory, random);
				
				subtrajectoryOfFeature = subtrajectory.best(subtrajectoryOfFeature);
			}
		}
		
		return subtrajectoryOfFeature;
	}

}
