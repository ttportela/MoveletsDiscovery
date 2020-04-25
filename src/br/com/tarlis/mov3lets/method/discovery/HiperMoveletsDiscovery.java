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
	}
	
	public void discover() {

		int maxSize = getDescriptor().getParamAsInt("max_size");
		int minSize = getDescriptor().getParamAsInt("min_size");

		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();
		
		List<MAT<MO>> queue = new ArrayList<MAT<MO>>();
		queue.addAll(trajsFromClass);
		
		int trajsLooked = 0, trajsIgnored = 0;
		
		while (queue.size() > 0) {
			MAT<MO> trajectory = queue.get(0);
			queue.remove(trajectory);
			trajsLooked++;
			
			// This guarantees the reproducibility
			Random random = new Random(trajectory.getTid());
			/** STEP 2.1: Starts at discovering movelets */
			progressBar.trace("Hiper Movelets Discovery for Class: " + trajectory.getMovingObject()); // Might be saved in HD
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
			
			/** UPDATE QUEUE: */
			// Remove from queue other covered trajectories: - by Tarlis
			int removed = queue.size();
			for (Subtrajectory candidate : candidates) {
//				if (candidate.getProportionInClass() > 0.5)
				queue.removeAll( ((ProportionQuality)candidate.getQuality()).getCoveredInClass() );
			}
			trajsIgnored += (removed - queue.size());
			
			/** STEP 2.3, for this trajectory movelets: 
			 * It transforms the training and test sets of trajectories using the movelets */
			for (Subtrajectory candidate : candidates) {
				// It initializes the set of distances of all movelets to null
				candidate.setDistances(null);
				candidate.setQuality(null);
				// In this step the set of distances is filled by this method
				computeDistances(candidate, this.train); // computeDistances(movelet, trajectories);

				/* STEP 2.1.6: QUALIFY BEST HALF CANDIDATES 
				 * * * * * * * * * * * * * * * * * * * * * * * * */
//				assesQuality(candidate);
				assesQuality(candidate, random);
			}

			/** STEP 2.2: SELECTING BEST CANDIDATES */	
			candidates = filterMovelets(candidates);
			
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

}
