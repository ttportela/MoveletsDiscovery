/**
 * 
 */
package br.ufsc.mov3lets.method.discovery.deprecated;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.discovery.UltraMoveletsDiscovery;
import br.ufsc.mov3lets.method.discovery.structures.TrajectoryDiscovery;
import br.ufsc.mov3lets.method.filter.OverlappingFeaturesFilter;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class HiperPivotsMoveletsDiscovery.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class UltraGMoveletsDiscovery<MO> extends UltraMoveletsDiscovery<MO> implements TrajectoryDiscovery {
	
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
	public UltraGMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajectory, trajsFromClass, data, train, test, qualityMeasure, descriptor);
	}
	
	protected Integer total_size = 0;
	
	/**
	 * ULTRA Movelets discovery. 
	 * v.teste.A: Gera movelets apenas dos pivots com qualidade 
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
		
		this.bestFilter = new OverlappingFeaturesFilter(0.0, null);  // ULTRA-G

		// It starts with the base case
		addStats("Class", trajectory.getMovingObject()); 
		addStats("Trajectory", trajectory.getTid());
		addStats("Trajectory Size", n);			

		List<Subtrajectory> candidatesOfSize = findPivotCandidates(trajectory, trajectories, minSize);
//		computeQuality(candidatesOfSize, random, trajectory);
//		calculateProportion(candidatesOfSize, random);
		for (Subtrajectory subtrajectory : candidatesOfSize) {
//			computeDistances(subtrajectory, trajectories);
			assesQuality(subtrajectory, random);
		}
		total_size += candidatesOfSize.size();

//		candidatesOfSize = this.bestFilter.filter(candidatesOfSize); // ULTRA-G
		for(Subtrajectory candidate : candidatesOfSize) {
			candidates.add(growPivot(candidate, trajectory, trajectories, minSize+1, maxSize, random, null));
		}

		addStats("Number of Candidates", total_size);
//		addStats("Pivot Candidates", candidatesOfSize.size());
		addStats("Selected Candidates", candidates.size());
		
		candidates = this.bestFilter.filter(candidates);
		
		addStats("Total of Movelets", candidates.size());
		addStats("Max Size", this.maxSizeOfCandidates);
		addStats("Used Features", this.currentMaxCombinationOfFeatures);
		
		progressBar.plus(getStats());
				
		return candidates;
	}

}
