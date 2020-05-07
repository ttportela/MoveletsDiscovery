/**
 * 
 */
package br.com.tarlis.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.com.tarlis.mov3lets.method.qualitymeasure.ProportionQuality;
import br.com.tarlis.mov3lets.method.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

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
		
		while (queue.size() > 0) {
			MAT<MO> trajectory = queue.get(0);
			queue.remove(trajectory);
			trajsLooked++;
			int removed = queue.size();
			
			// This guarantees the reproducibility
			Random random = new Random(trajectory.getTid());
			/** STEP 2.1: Starts at discovering movelets */
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
			
			progressBar.trace("Class: " + trajsFromClass.get(0).getMovingObject() 
					+ ". Trajectory: " + trajectory.getTid() 
					+ ". Used GAMMA: " + GAMMA);
			
			trajsIgnored += (removed - queue.size());
			
			/** STEP 2.3: Runs the pruning process */
			if(getDescriptor().getFlag("last_prunning"))
				candidates = lastPrunningFilter(candidates); // TODO is this here?
			
			movelets.addAll(candidates);
			
				
			/** STEP 2.4.1: Output Movelets (partial) */
			super.output("train", this.train, candidates, true);
			
			// Compute distances and best alignments for the test trajectories:
			/* If a test trajectory set was provided, it does the same.
			 * and return otherwise */
			/** STEP 2.4.2: Output Movelets (partial) */
			if (!this.test.isEmpty()) {
				for (Subtrajectory candidate : candidates) {
					// It initializes the set of distances of all movelets to null
					candidate.setDistances(null);
					// In this step the set of distances is filled by this method
					computeDistances(candidate, this.test);
				}
				super.output("test", this.test, candidates, true);
			}

			System.gc();
		}		
		
		progressBar.plus(trajsIgnored, 
						   "Class: " + trajsFromClass.get(0).getMovingObject() 
					   + ". Total of Movelets: " + movelets.size() 
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
		List<Subtrajectory> bestCandidates = new ArrayList<Subtrajectory>();
		double[] percentiles = {0.1, 0.2, 0.3, 0.4, 0.5};
		
		for (double gamma : percentiles) {
			bestCandidates = filterByProportion(candidatesByProp, GAMMA = gamma, random);
			List<MAT<MO>> covered = getCoveredInClass(bestCandidates);
			
			bestCandidates = filterByQuality(bestCandidates, random);
			if (bestCandidates.size() > 0) {
				queue.removeAll(covered);
				break;
			}
		}
		
		if (bestCandidates.isEmpty()) { 
			/* STEP 2.1.5: SELECT ONLY HALF OF THE CANDIDATES (IF Nothing found)
			 * * * * * * * * * * * * * * * * * * * * * * * * */
			calculateProportion(candidatesByProp, 1.0, random); GAMMA = 0.0;
			bestCandidates = candidatesByProp.subList(0, (int) Math.ceil((double) candidatesByProp.size() * TAU));
			
			/** UPDATE QUEUE: */
			queue.removeAll(getCoveredInClass(bestCandidates));

			/** STEP 2.2: SELECTING BEST CANDIDATES */	
			bestCandidates = filterByQuality(bestCandidates, random);
		}

		
		progressBar.plus("Class: " + trajectory.getMovingObject() 
						+ ". Trajectory: " + trajectory.getTid() 
						+ ". Trajectory Size: " + trajectory.getPoints().size() 
						+ ". Number of Candidates: " + candidatesByProp.size() 
						+ ". Total of Movelets: " + bestCandidates.size() 
						+ ". Max Size: " + maxSize
						+ ". Used Features: " + this.maxNumberOfFeatures 
						+ ". Used GAMMA: " + GAMMA);

		return bestCandidates;
	}

	public List<MAT<MO>> getCoveredInClass(List<Subtrajectory> bestCandidates) {
		List<MAT<MO>> covered = new ArrayList<MAT<MO>>();
		// Remove from queue other covered trajectories: - by Tarlis
		for (Subtrajectory candidate : bestCandidates) {
//			if (candidate.getProportionInClass() > 0.5)
			covered.addAll((List) ((ProportionQuality) candidate.getQuality()).getCoveredInClass() );
		}
		
		return covered;
	}

}
