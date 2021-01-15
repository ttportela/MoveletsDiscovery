/**
 * 
 */
package br.ufsc.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class HiperMoveletsDiscovery.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class HiperRandomMoveletsDiscovery<MO> extends HiperMoveletsDiscovery<MO> {

	/** The queue. */
	protected List<MAT<MO>> queue;

	/**
	 * Instantiates a new hiper random movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data           the data
	 * @param train          the train
	 * @param test           the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor     the descriptor
	 */
	public HiperRandomMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train,
			List<MAT<MO>> test, QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajsFromClass, data, train, test, qualityMeasure, descriptor);
	}

	/**
	 * Overridden method.
	 * 
	 * @see br.com.tarlis.mov3lets.method.discovery.SuperMoveletsDiscovery#selectBestCandidates(br.com.tarlis.mov3lets.model.MAT,
	 *      int, java.util.Random, java.util.List).
	 * 
	 * @param trajectory
	 * @param maxSize
	 * @param random
	 * @param candidatesByProp
	 * @return
	 */
	public List<Subtrajectory> selectBestCandidates(MAT<MO> trajectory, int maxSize, Random random,
			List<Subtrajectory> candidatesByProp) {
		List<Subtrajectory> bestCandidates;

		int candidates_total = candidatesByProp.size();
		int n = bucketSize(candidatesByProp.size());
				
		bestCandidates = randomPop(candidatesByProp, n);
		bestCandidates = filterByQuality(bestCandidates, random, trajectory);

		/*
		 * STEP 2.1.5: Recover Approach (IF Nothing found) * * * * * * * * * * * * * * *
		 * * * * * * * * *
		 */
		for (int i = n; i < candidatesByProp.size(); i += n) {
			bestCandidates = filterByQuality(randomPop(candidatesByProp, n), random, trajectory);
			
			if (i > candidatesByProp.size() || !bestCandidates.isEmpty()) break;
			else n *= 2; // expand the window size
		}

//		queue.removeAll(getCoveredInClass(bestCandidates));

		progressBar.plus("Class: " + trajectory.getMovingObject() + ". Trajectory: " + trajectory.getTid()
				+ ". Trajectory Size: " + trajectory.getPoints().size() + ". Number of Candidates: "
				+ candidates_total + ". Total of Movelets: " + bestCandidates.size() + ". Max Size: " + maxSize
				+ ". Used Features: " + this.maxNumberOfFeatures);

		return bestCandidates;
	}

	public List<Subtrajectory> randomPop(List<Subtrajectory> list, int totalItems) {
		Random rand = new Random();

		// create a temporary list for storing 
		// selected element 
		List<Subtrajectory> newList = new ArrayList<>();
		for (int i = 0; i < totalItems; i++) {

			// take a raundom index between 0 to size 
			// of given List 
			int randomIndex = rand.nextInt(list.size());

			// pop element to temporary list 
			newList.add(list.remove(randomIndex));
		}
		return newList;
	}

}
