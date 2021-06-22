/**
 * 
 */
package br.ufsc.mov3lets.method.discovery.deprecated;

import java.util.List;
import java.util.Random;

import br.ufsc.mov3lets.method.discovery.RandomMoveletsDiscovery;
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
public class UltraMoveletsDiscovery<MO> extends RandomMoveletsDiscovery<MO> {

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
	public UltraMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train,
			List<MAT<MO>> test, QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajectory, trajsFromClass, data, train, test, qualityMeasure, descriptor);
		
		TAU 	= getDescriptor().hasParam("tau")? getDescriptor().getParamAsDouble("tau") : 0.9;
		BU 		= getDescriptor().hasParam("bucket_slice")? getDescriptor().getParamAsDouble("bucket_slice") : 0.1;
	}

	protected void printStart() {
		progressBar.trace("Ultra Movelets Discovery for Class: " + trajectory.getMovingObject());
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
//		int n = bucketSize(candidatesByProp.size());
		int n = (int) Math.ceil((double) candidates_total * BU/2.0); // By 10%
		
		addStats("Class", trajectory.getMovingObject()); 
		addStats("Trajectory", trajectory.getTid());
		addStats("Trajectory Size", trajectory.getPoints().size()); 
		addStats("Number of Candidates", candidatesByProp.size());

		Random randSelection;
		if (getDescriptor().hasParam("random_seed")) 
			randSelection = new Random(getDescriptor().getParamAsInt("random_seed"));
		else
			randSelection = new Random();

		calculateProportion(candidatesByProp, random);
		// Relative TAU based on the higher proportion:
		double rel_tau = relativeFrequency(candidatesByProp);	addStats("TAU", rel_tau);
		int bs = bucketSize(candidatesByProp.size());  			addStats("Bucket Size", bs);
		bestCandidates = filterByProportion(candidatesByProp, rel_tau, bs);
//		bestCandidates = filterByProportion(candidatesByProp, random);
		
		orderCandidates(bucket);
		bestCandidates.addAll( randomPop(bucket, n, randSelection) );
		
		long scored = bestCandidates.size();
		bestCandidates = filterByQuality(bestCandidates, random, trajectory);

		/*
		 * STEP 2.1.5: Recover Approach (IF Nothing found) * * * * * * * * * * * * * * *
		 * * * * * * * * *
		 */
		if (bestCandidates.isEmpty())
			while (bestCandidates.isEmpty() || !bucket.isEmpty()) {
				bestCandidates = bucket.subList(0, (n > bucket.size()? bucket.size() : n)); // 1/2 Top candidates
				bucket.removeAll(bestCandidates);
				
				bestCandidates.addAll( randomPop(bucket, n, randSelection) );				// 1/2 random candidates
				
				scored += bestCandidates.size();
				bestCandidates = filterByQuality(bestCandidates, random, trajectory);
				
				n *= 2;
			}

//		queue.removeAll(getCoveredInClass(bestCandidates));

		addStats("Scored Candidates", scored);
		addStats("Total of Movelets", bestCandidates.size());
		addStats("Max Size", maxSize);
		addStats("Used Features", this.maxNumberOfFeatures);
		progressBar.plus(getStats());

		return bestCandidates;
	}

}
