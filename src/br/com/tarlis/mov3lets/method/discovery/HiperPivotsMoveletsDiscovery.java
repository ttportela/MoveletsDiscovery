/**
 * 
 */
package br.com.tarlis.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
public class HiperPivotsMoveletsDiscovery<MO> extends HiperMoveletsDiscovery<MO> {

	protected List<MAT<MO>> queue;

	/**
	 * @param trajsFromClass
	 * @param train
	 * @param candidates
	 * @param qualityMeasure
	 * @param descriptor
	 */
	public HiperPivotsMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test, List<Subtrajectory> candidates,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajsFromClass, data, train, test, candidates, qualityMeasure, descriptor);
	}
	

	
	/**
	 * @param trajectory2
	 * @param data2
	 * @param minSize
	 * @param maxSize
	 * @param random 
	 * @return
	 */
	public List<Subtrajectory> moveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajectories, int minSize, int maxSize, Random random) {
		List<Subtrajectory> candidatesByProp = new ArrayList<Subtrajectory>();

		int n = trajectory.getPoints().size();
		
		// TO USE THE LOG, PUT "-Ms -3"
		switch (maxSize) {
			case -1: maxSize = n; break;
			case -2: maxSize = (int) Math.round( Math.log10(n) / Math.log10(2) ); break;	
			case -3: maxSize = (int) Math.ceil(Math.log(n))+1; break;	
			default: break;
		}

		// It starts with the base case	
		int size = 1;
		Integer total_size = 0;
		
		base = computeBaseDistances(trajectory, trajectories);
		
		List<Subtrajectory> candidatesOfSize = findCandidates(trajectory, trajectories, size, base);
		
		GAMMA = getDescriptor().getParamAsDouble("gamma");
		candidatesOfSize = filterByProportion(candidatesOfSize, GAMMA, random);

		if( minSize <= 1 ) {
			candidatesByProp.addAll(candidatesOfSize);
		}				
		
		double[][][][] lastSize = clone4DArray(base);		

		total_size = total_size + candidatesOfSize.size();
		
		// Tratar o resto dos tamanhos 
		for (size = 2; size <= maxSize; size++) {
			
			// Precompute de distance matrix
   			double[][][][] newSize = newSize(trajectory, trajectories, base, lastSize, size);
			
			candidatesOfSize = growPivots(candidatesOfSize, trajectory, trajectories, base, newSize, size);
			GAMMA = getDescriptor().getParamAsDouble("gamma");
			candidatesOfSize = filterByProportion(candidatesOfSize, GAMMA, random);
	
			total_size = total_size + candidatesOfSize.size();
			
			if (size >= minSize){
				
				//for (Subtrajectory candidate : candidatesOfSize) assesQuality(candidate);				
//				candidatesOfSize.forEach(x -> assesQuality(x, random));
				candidatesByProp.addAll(candidatesOfSize);
			}
		
			lastSize = newSize;
						
		} // for (int size = 2; size <= max; size++)	
	
//		base =  null;
		lastSize = null;
		
		queue.removeAll(getCoveredInClass(candidatesByProp));
		
		/** STEP 2.2: SELECTING BEST CANDIDATES */	
		List<Subtrajectory> bestCandidates = filterByQuality(candidatesByProp, random);
	
		progressBar.plus("Class: " + trajectory.getMovingObject() 
						+ ". Trajectory: " + trajectory.getTid() 
						+ ". Trajectory Size: " + trajectory.getPoints().size() 
						+ ". Number of Candidates: " + candidatesByProp.size() 
						+ ". Total of Movelets: " + bestCandidates.size() 
						+ ". Max Size: " + maxSize
						+ ". Used Features: " + this.maxNumberOfFeatures 
						+ ". Memory Use: " + Mov3letsUtils.getUsedMemory() 
						+ ". Used GAMMA: " + GAMMA);
	
		return bestCandidates;
	}

	private List<Subtrajectory> growPivots(List<Subtrajectory> candidatesOfSize, MAT<MO> trajectory,
			List<MAT<MO>> trajectories, double[][][][] base, double[][][][] newSize, int size) {
		List<Subtrajectory> newCandidates = new ArrayList<Subtrajectory>();
		
		for(Subtrajectory candidate : candidatesOfSize) {
			Subtrajectory subtrajectory = buildNewSize(candidate, trajectory, trajectories, newSize, size, true);
			if (subtrajectory != null)
				newCandidates.add(subtrajectory);
			
			subtrajectory = buildNewSize(candidate, trajectory, trajectories, newSize, size, false);	
			if (subtrajectory != null)
				newCandidates.add(subtrajectory);		
		}
		
		return newCandidates;
	}



	private Subtrajectory buildNewSize(Subtrajectory candidate, MAT<MO> trajectory, List<MAT<MO>> trajectories,
			double[][][][] mdist, int size, boolean left) {
		
		int start = candidate.getStart() - (left? 1 : 0);
		int end   = candidate.getEnd()   + (left? 0 : 1);
		
		if (start < 0 || end > trajectory.getPoints().size()-1)
			return null;
		
		Subtrajectory subtrajectory = new Subtrajectory(start, end, trajectory, trajectories.size(),
				candidate.getPointFeatures(), candidate.getK());
		
		double[][][] distancesForAllT = mdist[start];
		
		// For each trajectory in the database
		for (int i = 0; i < trajectories.size(); i++) {
			MAT<MO> T = trajectories.get(i);	
			
			double[][] distancesForT = distancesForAllT[i];
			double[][] ranksForT = new double[distancesForT.length][];
			
			int limit = T.getPoints().size() - size + 1;
			
			if (limit > 0)
				for (int k = 0; k < numberOfFeatures; k++) {				
					ranksForT[k] = rankingAlgorithm.rank(Arrays.stream(distancesForT[k],0,limit).toArray());
				} // for (int k = 0; k < numberOfFeatures; k++)
				
			int bestPosition = (limit > 0) ? bestAlignmentByRanking(ranksForT, subtrajectory.getPointFeatures()) : -1;
			for (int j = 0; j < subtrajectory.getPointFeatures().length; j++) {	
				double distance = (bestPosition >= 0) ? 
						distancesForT[subtrajectory.getPointFeatures()[j]][bestPosition] : MAX_VALUE;
				subtrajectory.getDistances()[j][i] = (distance != MAX_VALUE) ? 
						Math.sqrt( distance / size ) : MAX_VALUE;					
			}
			
		}
		
		return subtrajectory;
	}

}
