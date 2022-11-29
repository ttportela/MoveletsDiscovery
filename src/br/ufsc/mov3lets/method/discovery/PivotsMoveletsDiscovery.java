/**
 * 
 */
package br.ufsc.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * @author tarlisportela
 *
 */
public class PivotsMoveletsDiscovery<MO> extends UltraMoveletsDiscovery<MO> {
	
	/**
	 * Instantiates a new pivots movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor the descriptor
	 */
	public PivotsMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajectory, trajsFromClass, data, train, test, qualityMeasure, descriptor);
		
		// Pivots default is 10%:
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
		List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();
		
		int n = trajectory.getPoints().size();

		minSize = minSize(minSize, n);
		maxSize = maxSize(maxSize, minSize, n);
		
		if( minSize <= 1 ) minSize = 1;

		// It starts with the base case
		addStats("Class", trajectory.getMovingObject()); 
		addStats("Trajectory", trajectory.getTid());
		addStats("Trajectory Size", n);			

		List<Subtrajectory> pivots = findPivotCandidates(trajectory, trajectories, minSize);

		for (Subtrajectory subtrajectory : pivots) {
			assesQuality(subtrajectory, random);
		}
		total_size += pivots.size();

		pivots = filterTopCandidates(this.qualityRanker.rank(pivots));
		
		// Marks pivot points of trajectory to limit candidate extraction 
		SortedSet<Integer> trajectory_marks = new TreeSet<Integer>(); 
		for(Subtrajectory candidate : pivots) {
			int p = candidate.getStart();
			trajectory_marks.add(p);
			if (p > 0)
				trajectory_marks.add(p-1);
			if (p < n-1)
				trajectory_marks.add(p+1);
		}
		
		for(Subtrajectory candidate : pivots) {
			candidates.add(growPivot(candidate, trajectory, trajectories, minSize+1, maxSize, random, trajectory_marks));
		}

		addStats("Number of Candidates", total_size);
		addStats("Selected Candidates", candidates.size());
		
		candidates = this.bestFilter.filter(candidates);
		
		addStats("Total of Movelets", candidates.size());
		addStats("Max Size", this.maxSizeOfCandidates);
		addStats("Used Features", this.currentMaxCombinationOfFeatures);
		
		progressBar.plus(getStats());
				
		return candidates;
	}

}
