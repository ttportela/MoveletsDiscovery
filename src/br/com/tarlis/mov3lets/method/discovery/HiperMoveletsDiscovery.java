/**
 * 
 */
package br.com.tarlis.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.util.Combinations;

import br.com.tarlis.mov3lets.method.qualitymeasure.ProportionQualityMeasure;
import br.com.tarlis.mov3lets.method.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;

/**
 * @author tarlis
 * @param <MO>
 *
 */
public class HiperMoveletsDiscovery<MO> extends SuperMoveletsDiscovery<MO> {

	protected List<MAT<MO>> queue;

	/**
	 * @param trajsFromClass
	 * @param train
	 * @param candidates
	 * @param qualityMeasure
	 * @param descriptor
	 */
	public HiperMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, List<Subtrajectory> candidates,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajsFromClass, data, train, test, candidates, qualityMeasure, descriptor);
		this.queue = new ArrayList<MAT<MO>>();
		queue.addAll(trajsFromClass);
	}
	
	public void discover() {

		int maxSize = getDescriptor().getParamAsInt("max_size");
		int minSize = getDescriptor().getParamAsInt("min_size");

		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();
		
		int trajsLooked = 0, trajsIgnored = 0;

		progressBar.trace("Hiper Movelets Discovery for Class: " + trajsFromClass.get(0).getMovingObject()); 
		
		this.proportionMeasure = new ProportionQualityMeasure<MO>(this.trajsFromClass, TAU);
		
		while (queue.size() > 0) {
			MAT<MO> trajectory = queue.get(0);
			queue.remove(trajectory);
			trajsLooked++;
			int removed = queue.size();
			
			// This guarantees the reproducibility
			Random random = new Random(trajectory.getTid());
			/** STEP 2.1: Starts at discovering movelets */
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
			
//			progressBar.trace("Class: " + trajsFromClass.get(0).getMovingObject() 
//					+ ". Trajectory: " + trajectory.getTid() 
//					+ ". Used GAMMA: " + GAMMA);
			
			trajsIgnored += (removed - queue.size());
			
			/** STEP 2.4: SELECTING BEST CANDIDATES */			
//			candidates = filterMovelets(candidates);		
			movelets.addAll(filterMovelets(candidates));
			
			System.gc();
		}
		
		/** STEP 2.2: Runs the pruning process */
		if(getDescriptor().getFlag("last_prunning"))
			movelets = lastPrunningFilter(movelets);
		/** STEP 2.2: --------------------------------- */
		
		/** STEP 2.3.1: Output Movelets (partial) */
		super.output("train", this.train, movelets, true);
		base =  null;
		
		// Compute distances and best alignments for the test trajectories:
		/* If a test trajectory set was provided, it does the same.
		 * and return otherwise */
		/** STEP 2.3.2: Output Movelets (partial) */
		if (!this.test.isEmpty()) {
//			base = computeBaseDistances(trajectory, this.test);
			for (Subtrajectory candidate : movelets) {
				// It initializes the set of distances of all movelets to null
				candidate.setDistances(null);
				// In this step the set of distances is filled by this method
				computeDistances(candidate, this.test); //, computeBaseDistances(trajectory, this.test));
			}
			super.output("test", this.test, movelets, true);
		}
		/** --------------------------------- */	
		
		progressBar.plus(trajsIgnored, 
						   "Class: " + trajsFromClass.get(0).getMovingObject() 
//					   + ". Total of Movelets: " + movelets.size() 
					   + ". Trajs. Looked: " + trajsLooked 
					   + ". Trajs. Ignored: " + trajsIgnored);
		
//		progressBar.trace("Class: " + trajsFromClass.get(0).getMovingObject() 
//				   + ". Total of Movelets: " + movelets.size());
//		System.out.println("\nMOVELETS:");
//		for (Subtrajectory subtrajectory : movelets) {
//			System.out.println(subtrajectory);
//		}

		/** STEP 2.5, to write all outputs: */
		super.output("train", this.train, movelets, false);
		
		if (!this.test.isEmpty())
			super.output("test", this.test, movelets, false);
	}

	public List<Subtrajectory> selectBestCandidates(MAT<MO> trajectory, int maxSize, Random random,
			List<Subtrajectory> candidatesByProp) {
		List<Subtrajectory> bestCandidates;

//		GAMMA = getDescriptor().getParamAsDouble("gamma");
		bestCandidates = filterByProportion(candidatesByProp, random);
		
		bestCandidates = filterByQuality(bestCandidates, random);
		Set<MAT<MO>> covered = getCoveredInClass(bestCandidates);
		if (bestCandidates.size() > 0) {
			/** UPDATE QUEUE: */
			queue.removeAll(covered);
		} 
//		else
//		if (bestCandidates.isEmpty()) { 
//			/* STEP 2.1.5: SELECT ONLY HALF OF THE CANDIDATES (IF Nothing found)
//			 * * * * * * * * * * * * * * * * * * * * * * * * */
//			calculateProportion(candidatesByProp, GAMMA, random); // GAMMA = 0.0;
//			bestCandidates = candidatesByProp.subList(0, (int) Math.ceil((double) candidatesByProp.size() * TAU));
//			
//			/** UPDATE QUEUE: */
////			queue.removeAll(getCoveredInClass(bestCandidates));
//
//			/** STEP 2.2: SELECTING BEST CANDIDATES */	
//			bestCandidates = filterByQuality(bestCandidates, random);
//		}

		progressBar.plus("Class: " + trajectory.getMovingObject() 
						+ ". Trajectory: " + trajectory.getTid() 
						+ ". Trajectory Size: " + trajectory.getPoints().size() 
						+ ". Number of Candidates: " + candidatesByProp.size() 
						+ ". Total of Movelets: " + bestCandidates.size() 
						+ ". Max Size: " + maxSize
						+ ". Used Features: " + this.maxNumberOfFeatures 
						+ ". Memory Use: " + Mov3letsUtils.getUsedMemory());

		return bestCandidates;
	}

	public Set<MAT<MO>> getCoveredInClass(List<Subtrajectory> bestCandidates) {
		Set<MAT<MO>> covered = new LinkedHashSet<MAT<MO>>();
//		int[] count = new int[this.trajsFromClass.size()];
		// To remove from queue other covered trajectories: - by Tarlis
		for (int i = 0; i < bestCandidates.size(); i++) {
//			for (MAT<?> T : bestCandidates.get(i).getCovered()) {
//				count[this.trajsFromClass.indexOf(T)] += 1;
//			}
			if (covered.isEmpty())
				covered.addAll((List) bestCandidates.get(i).getCovered());
			else
				covered.retainAll((List) bestCandidates.get(i).getCovered());
		}
		
//		for (int j = 0; j < count.length; j++) {
//			if (count[j] >= this.trajsFromClass.size() * TAU)
//				covered.add(this.trajsFromClass.get(j));
//		}
		
		return covered;
	}

	public int[][] makeCombinations(boolean exploreDimensions, int numberOfFeatures, int maxNumberOfFeatures) {
		
		if (combinations != null)
			return combinations;
		
		if (!getDescriptor().getFlag("LDM"))
			return super.makeCombinations(exploreDimensions, numberOfFeatures, maxNumberOfFeatures);
		
		List<int[]> selected = new ArrayList<int[]>();
		
		int currentFeatures;
		if (exploreDimensions){
			currentFeatures = 1;
		} else {
			currentFeatures = numberOfFeatures;
		}
		
//		combinations = new int[(int) (Math.pow(2, maxNumberOfFeatures) - 1)][];
//		int k = 0;
		// For each possible NumberOfFeatures and each combination of those: 
		for (;currentFeatures <= maxNumberOfFeatures; currentFeatures++) {
			for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) {					
				
				if (!hasDuplicates(comb))
					selected.add(comb);
//				combinations[k++] = comb;
				
			} // for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) 					
		} // for (int i = 0; i < train.size(); i++

		return selected.toArray(new int[selected.size()][]);
	}

	public boolean hasDuplicates(int[] comb) {

		int[] orders = new int[comb.length];
		for (int j = 0; j < comb.length; j++) {
			orders[j] = getDescriptor().getAttributes().get(comb[j]).getOrder();
		}
		
		Arrays.sort(orders);
		for(int i = 1; i < orders.length; i++) {
		    if(orders[i] == orders[i - 1]) {
		        return true;
		    }
		}
		
		return false;
	}

}
