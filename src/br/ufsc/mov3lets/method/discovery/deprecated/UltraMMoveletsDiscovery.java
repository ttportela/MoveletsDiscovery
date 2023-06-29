/**
 * 
 */
package br.ufsc.mov3lets.method.discovery.deprecated;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.discovery.UltraMoveletsDiscovery;
import br.ufsc.mov3lets.method.filter.OverlappingFeaturesFilter;
import br.ufsc.mov3lets.method.output.FeatureOutputter;
import br.ufsc.mov3lets.method.output.OutputterAdapter;
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
public class UltraMMoveletsDiscovery<MO> extends UltraMoveletsDiscovery<MO> {
	
	/**
	 * Instantiates a new hiper pivots movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor the descriptor
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public UltraMMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajectory, trajsFromClass, data, train, test, qualityMeasure, descriptor);
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
		
		this.bestFilter = new OverlappingFeaturesFilter(0.0, null);  // ULTRA-C (default)

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

		candidatesOfSize = this.bestFilter.filter(candidatesOfSize); // ULTRA-C
		for(Subtrajectory candidate : candidatesOfSize) {
			candidates.add(outputMovelet(
				growPivot(candidate, trajectory, trajectories, minSize+1, maxSize, random, null)
			));
		}

		addStats("Number of Candidates", total_size);
//		addStats("Pivot Candidates", candidatesOfSize.size());
//		addStats("Selected Candidates", candidatesOfSize.size());
		
//		candidates = this.bestFilter.filter(candidates); // ULTRA-C
		
		addStats("Total of Movelets", candidates.size());
		addStats("Max Size", this.maxSizeOfCandidates);
		addStats("Used Features", this.currentMaxCombinationOfFeatures);
		
		progressBar.plus(getStats());
				
		return candidates;

	}

	/**
	 * Method to output movelet by movelet. It is synchronized by thread. 
	 * 
	 * @param movelets
	 */
	public Subtrajectory outputMovelet(Subtrajectory movelet) {
		/** STEP 2.3.1: Output Movelets (partial) */
		this.lock.getWriteLock().lock();
		
		writeMovelet("train", this.train, movelet);
		
		// Compute distances and best alignments for the test trajectories:
		/* If a test trajectory set was provided, it does the same.
		 * and return otherwise */
		/** STEP 2.3.2: Output Movelets (partial) */
		if (!this.test.isEmpty()) {
			// It initializes the set of distances of all movelets to null
			movelet.setDistances(null);
			// In this step the set of distances is filled by this method
			computeDistances(movelet, this.test); //, computeBaseDistances(trajectory, this.test));
			writeMovelet("test", this.test, movelet);
		}
		
		movelet.setDistances(null);

		this.lock.getWriteLock().unlock();
		
		return movelet;

	}
	
	/**
	 * Output.
	 *
	 * @param filename the filename
	 * @param trajectories the trajectories
	 * @param movelets the movelets
	 * @param delayOutput the delay output
	 */
	public void writeMovelet(String filename, List<MAT<MO>> trajectories, Subtrajectory movelet) {
		// It puts distances as trajectory attributes
		if (outputers != null)
			for (OutputterAdapter<MO, ?> output : outputers) {
				((FeatureOutputter<MO>) output).writeMovelet(filename, trajectories, movelet);			
			}
	}

}
