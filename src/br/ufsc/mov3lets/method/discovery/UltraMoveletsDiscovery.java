/**
 * 
 */
package br.ufsc.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import br.ufsc.mov3lets.method.output.OutputterAdapter;
import br.ufsc.mov3lets.method.qualitymeasure.ProportionQualityMeasure;
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
public class UltraMoveletsDiscovery<MO> extends HiperPivotsMoveletsDiscovery<MO> {

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
	public UltraMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
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
		
		int trajsIgnored = 0;

		progressBar.trace("Ultra Movelets Discovery for Class: " + trajectory.getMovingObject()); 
		
		this.proportionMeasure = new ProportionQualityMeasure<MO>(this.trajsFromClass, TAU);
		
//		while (queue.size() > 0) {
//			MAT<MO> trajectory = queue.get(0);
		if (queue.contains(trajectory)) {
			queue.remove(trajectory);
			
			// This guarantees the reproducibility
			Random random = new Random(trajectory.getTid());
			/** STEP 2.1: Starts at discovering movelets */
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
			
//			progressBar.trace("Class: " + trajsFromClass.get(0).getMovingObject() 
//					+ ". Trajectory: " + trajectory.getTid() 
//					+ ". Used GAMMA: " + GAMMA);

			// Removes trajectories from queue:
			trajsIgnored += updateQueue(getCoveredInClass(candidates));
			
			/** STEP 2.4: SELECTING BEST CANDIDATES */			
//			candidates = filterMovelets(candidates);		
			movelets.addAll(filterMovelets(candidates));
			
//			System.gc();
		}
		
		/** STEP 2.2: Runs the pruning process */
		if(getDescriptor().getFlag("last_prunning"))
			movelets = lastPrunningFilter(movelets);

		/** STEP 2.2: ---------------------------- */
		outputMovelets(movelets);
		/** -------------------------------------- */	
		
		progressBar.plus(trajsIgnored, 
						   "Class: " + trajsFromClass.get(0).getMovingObject() 
//					   + ". Total of Movelets: " + movelets.size() 
					   + ". Trajs. Looked: " + 1 
					   + ". Trajs. Ignored: " + trajsIgnored);
		
//		progressBar.trace("Class: " + trajsFromClass.get(0).getMovingObject() 
//				   + ". Total of Movelets: " + movelets.size());
//		System.out.println("\nMOVELETS:");
//		for (Subtrajectory subtrajectory : movelets) {
//			System.out.println(subtrajectory);
//		}

		/** STEP 2.5, to write all outputs: */
//		super.output("train", this.train, movelets, false);
//		
//		if (!this.test.isEmpty())
//			super.output("test", this.test, movelets, false);
		
		return movelets;
	}

	/**
	 * Mehod updateQueue. 
	 * 
	 * @param trajsIgnored
	 * @param candidates
	 * @return
	 */
	public int updateQueue(Set<MAT<MO>> coveredTrajectories) {
		int n = queue.size();
		queue.removeAll(coveredTrajectories);	
		int trajsIgnored = (n - queue.size());
		
		if (outputers != null)
			for (OutputterAdapter<MO> output : outputers) {
				output.setDelayCount(output.getDelayCount() - trajsIgnored);			
			}
		
		return trajsIgnored;
	}

}
