/**
 * 
 */
package br.ufsc.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.discovery.structures.TrajectoryDiscovery;
import br.ufsc.mov3lets.method.qualitymeasure.FrequentQualityMeasure;
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
public class RandomMoveletsDiscovery<MO> extends HiperMoveletsDiscovery<MO> implements TrajectoryDiscovery {

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
	public RandomMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train,
			List<MAT<MO>> test, QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajsFromClass, data, train, test, qualityMeasure, descriptor);
		this.trajectory = trajectory;
	}
	
	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.discovery.SuperMoveletsDiscovery#discover().
	 * 
	 * @return
	 */
	public List<Subtrajectory> discover() {

		int maxSize = getDescriptor().getParamAsInt("max_size");
		int minSize = getDescriptor().getParamAsInt("min_size");

		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();
		
//		int trajsLooked = 0, trajsIgnored = 0;

//		printStart(); 
		
		this.frequencyMeasure = new FrequentQualityMeasure<MO>(this.trajsFromClass); //, TAU);

//		while (queue.size() > 0) {
//			MAT<MO> trajectory = queue.get(0);
//		if (queue.contains(trajectory)) {
//			queue.remove(trajectory);
//			trajsLooked++;
			
			// This guarantees the reproducibility
			Random random = new Random(trajectory.getTid());
			/** STEP 2.1: Starts at discovering movelets */
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
			
//			progressBar.trace("Class: " + trajsFromClass.get(0).getMovingObject() 
//					+ ". Trajectory: " + trajectory.getTid() 
//					+ ". Used GAMMA: " + GAMMA);

			// Removes trajectories from queue:
//			trajsIgnored += updateQueue(getCoveredInClass(candidates));
			
			/** STEP 2.4: SELECTING BEST CANDIDATES */			
//			candidates = filterMovelets(candidates);		
			movelets.addAll(filterMovelets(candidates));
			
//			System.gc();
//		}
		
		/** STEP 2.2: Runs the pruning process */
		if(getDescriptor().getFlag("last_prunning"))
			movelets = lastPrunningFilter(movelets);

		/** STEP 2.2: ---------------------------- */
		outputMovelets(movelets);
		/** -------------------------------------- */	
		
//		progressBar.plus(trajsIgnored, 
//						   "Class: " + trajsFromClass.get(0).getMovingObject() 
////					   + ". Total of Movelets: " + movelets.size() 
//					   + ". Trajs. Looked: " + trajsLooked 
//					   + ". Trajs. Ignored: " + trajsIgnored);
		
//		progressBar.trace("Class: " + trajsFromClass.get(0).getMovingObject() 
//				   + ". Total of Movelets: " + movelets.size());
//		System.out.println("\nMOVELETS:");
//		for (Subtrajectory subtrajectory : movelets) {
//			System.out.println(subtrajectory);
//		}

//		/** STEP 2.5, to write all outputs: */
//		super.output("train", this.train, movelets, false);
//		
//		if (!this.test.isEmpty())
//			super.output("test", this.test, movelets, false);
		
		return movelets;
	}

	protected void printStart() {
		progressBar.trace("Random Movelets Discovery for Class: " + trajsFromClass.get(0).getMovingObject());
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
		
		Random randSelection;
		if (getDescriptor().hasParam("random_seed")) 
			randSelection = new Random(getDescriptor().getParamAsInt("random_seed"));
		else
			randSelection = new Random();

//		int candidates_total = candidatesByProp.size();
		int n = bucketSize(candidatesByProp.size());

		addStats("Class", trajectory.getMovingObject()); 
		addStats("Trajectory", trajectory.getTid());
		addStats("Trajectory Size", trajectory.getPoints().size()); 
		addStats("Number of Candidates", candidatesByProp.size());
				
		bestCandidates = randomPop(candidatesByProp, n, randSelection);
		
		long scored = bestCandidates.size();
		bestCandidates = filterByQuality(bestCandidates, random, trajectory);

		/*
		 * STEP 2.1.5: Recover Approach (IF Nothing found) * * * * * * * * * * * * * * *
		 * * * * * * * * *
		 */
		if (bestCandidates.isEmpty())
			for (int i = n; i < candidatesByProp.size(); i += n) {
				bestCandidates = randomPop(candidatesByProp, n, randSelection);
				
				scored += bestCandidates.size();
				bestCandidates = filterByQuality(bestCandidates, random, trajectory);
				
				if (i > candidatesByProp.size() || !bestCandidates.isEmpty()) break;
				else n *= 2; // expand the window size
			}

//		queue.removeAll(getCoveredInClass(bestCandidates));

		addStats("Scored Candidates", scored);
		addStats("Total of Movelets", bestCandidates.size());
		addStats("Max Size", maxSize);
		addStats("Used Features", this.maxNumberOfFeatures);
		progressBar.plus(getStats());

		return bestCandidates;
	}

	public List<Subtrajectory> randomPop(List<Subtrajectory> list, int totalItems, Random rand) {
		// create a temporary list for storing 
		// selected element 
		List<Subtrajectory> newList = new ArrayList<>();
		for (int i = 0; i < totalItems; i++) {
			if (list.isEmpty()) break;

			// take a raundom index between 0 to size 
			// of given List 
			int randomIndex = rand.nextInt(list.size());

			// pop element to temporary list 
			newList.add(list.remove(randomIndex));
		}
		return newList;
	}

//	/**
//	 * TODO Gets the covered in class. 
//	 *
//	 * @param bestCandidates the best candidates
//	 * @return the covered in class
//	 */
//	public Set<MAT<MO>> getCoveredInClass(List<Subtrajectory> bestCandidates) {
//		return new LinkedHashSet<MAT<MO>>();
//	}

}
