package br.ufsc.mov3lets.method.discovery.deprecated;

import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.discovery.MoveletsDiscovery;
import br.ufsc.mov3lets.method.discovery.structures.TrajectoryDiscovery;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Subtrajectory;
import br.ufsc.mov3lets.model.aspect.Aspect;

public class DTWMoveletsDiscovery<MO> extends MoveletsDiscovery<MO> implements TrajectoryDiscovery {

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

		List<Subtrajectory> orderedCandidates = this.qualityRanker.rank(candidates);

		return bestShapelets(orderedCandidates, 0);
	}



	private List<Subtrajectory> bestShapelets(List<Subtrajectory> orderedCandidates, int i) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public double calculateDistance(Aspect<?> aspect, Aspect<?> aspect2, AttributeDescriptor attr) {
		// TODO Auto-generated method stub
		return 0;
	}

}
