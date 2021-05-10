package br.ufsc.mov3lets.method.discovery;

import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;

public class DTWMoveletsDiscovery<MO> extends MoveletsDiscovery<MO> {

	public DTWMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data,
			List<MAT<MO>> train, List<MAT<MO>> test, QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajectory, trajsFromClass, data, train, test, qualityMeasure, descriptor);
		// TODO Auto-generated constructor stub
	}
	


	/**
	 * Looks for candidates in the trajectory, then compares with every other trajectory.
	 *
	 * @return the list
	 */
	public List<Subtrajectory> discover() {
		
//		printStart();

		int maxSize = getDescriptor().getParamAsInt("max_size");
		int minSize = getDescriptor().getParamAsInt("min_size");
		
		Random random = new Random(trajectory.getTid());
		/** STEP 2.1: Starts at discovering movelets */
		List<Subtrajectory> movelets = moveletsDiscovery(trajectory, this.trajsFromClass, minSize, maxSize, random);
		
		return movelets;
	}
	
//	protected void printStart() {
//		progressBar.trace("DTWMovelets Discovery starting");
//	}
	
	public void assesQuality(Subtrajectory candidate, Random random) {
		qualityMeasure.assesQuality(candidate, random);
//		assesQuality(candidate);
	}
	
	/**
	 * 
	 * @param candidates
	 * @return
	 */
	public List<Subtrajectory> filterMovelets(List<Subtrajectory> candidates) {

		List<Subtrajectory> orderedCandidates = rankCandidates(candidates);

		return bestShapelets(orderedCandidates, 0);
	}

}
