/**
 * 
 */
package br.ufsc.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.discovery.structures.TrajectoryDiscovery;
import br.ufsc.mov3lets.method.qualitymeasure.ProportionQualityMeasure;
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
public class HipertMoveletsDiscovery<MO> extends HiperMoveletsDiscovery<MO> implements TrajectoryDiscovery {

	/**
	 * Instantiates a new hiper movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor the descriptor
	 */
	public HipertMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
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
		
//		int trajsLooked = 0, trajsIgnored = 0;

//		progressBar.trace("HiperT Movelets Discovery for Class: " + trajsFromClass.get(0).getMovingObject()); 
		
		this.proportionMeasure = new ProportionQualityMeasure<MO>(this.trajsFromClass); //, TAU);
		
//		while (queue.size() > 0) {
//			MAT<MO> trajectory = queue.get(0);
//			queue.remove(trajectory);
//			trajsLooked++;
			
			// This guarantees the reproducibility
			Random random = new Random(trajectory.getTid());
			/** STEP 2.1: Starts at discovering movelets */
			List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
			

			// Removes trajectories from queue:
//			trajsIgnored += updateQueue(getCoveredInClass(candidates));
			
			/** STEP 2.4: SELECTING BEST CANDIDATES */			
//			candidates = filterMovelets(candidates);		
			movelets.addAll(filterMovelets(candidates));
			
			setStats("");
			
//			System.gc();
//		}
		
		/** STEP 2.2: Runs the pruning process */
		if(getDescriptor().getFlag("last_prunning"))
			movelets = lastPrunningFilter(movelets);

		/** STEP 2.2: ---------------------------- */
		outputMovelets(movelets);
		/** -------------------------------------- */	
//		System.gc();
		
//		progressBar.plus(trajsIgnored, 
//						   "Class: " + trajsFromClass.get(0).getMovingObject() 
////					   + ". Total of Movelets: " + movelets.size() 
//					   + ". Trajs. Looked: " + trajsLooked 
//					   + ". Trajs. Ignored: " + trajsIgnored);
		
		return movelets;
	}

}
