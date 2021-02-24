/**
 * 
 */
package br.ufsc.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import br.ufsc.mov3lets.method.qualitymeasure.EntropyQualityMeasure;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class HiperPivotsCEMoveletsDiscovery.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class HiperPivotsCeMoveletsDiscovery<MO> extends HiperPivotsMoveletsDiscovery<MO> {

	/** The sample trajectories. */
	protected List<MAT<MO>> sampleTrajectories;

	/**
	 * Instantiates a new hiper pivots CE movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor the descriptor
	 */
	public HiperPivotsCeMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajsFromClass, data, train, test, qualityMeasure, descriptor);
	}
	
	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.discovery.HiperMoveletsDiscovery#discover().
	 * 
	 * @return
	 */
	public List<Subtrajectory> discover() {

		int maxSize = getDescriptor().getParamAsInt("max_size");
		int minSize = getDescriptor().getParamAsInt("min_size");

		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();
		
		int trajsLooked = 0, trajsIgnored = 0;

		progressBar.trace("Hiper Movelets Discovery for Class: " + trajsFromClass.get(0).getMovingObject()); 

		Random random = new Random();
		Set<MAT<MO>> sample = new LinkedHashSet();
		sample.addAll(trajsFromClass);
		
		int i = 0;
		while (sample.size() < trajsFromClass.size()*2 && i++ < trajsFromClass.size()*3)
			sample.add( train.get(random.nextInt(train.size())) );
		
		this.sampleTrajectories = new ArrayList(sample);
		
		this.proportionMeasure = new EntropyQualityMeasure<MO>(this.sampleTrajectories, TAU);
		
		while (queue.size() > 0) {
			MAT<MO> trajectory = queue.get(0);
			queue.remove(trajectory);
			trajsLooked++;
			
			// This guarantees the reproducibility
			random = new Random(trajectory.getTid());
			
			/** STEP 2.1: Starts at discovering movelets */
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.sampleTrajectories, minSize, maxSize, random);
			
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
		
		return movelets;
	}

}
