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

/**
 * @author tarlis
 * @param <MO>
 *
 */
public class HiperPivotsMoveletsDiscovery<MO> extends HiperMoveletsDiscovery<MO> {

	/**
	 * @param trajsFromClass
	 * @param train
	 * @param candidates
	 * @param qualityMeasure
	 * @param descriptor
	 */
	public HiperPivotsMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajsFromClass, data, train, test, qualityMeasure, descriptor);
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
		
//		GAMMA = getDescriptor().getParamAsDouble("gamma");
		calculateProportion(candidatesOfSize, random);
		orderCandidates(candidatesOfSize);
		candidatesOfSize = filterByProportion(candidatesOfSize, random);

		if( minSize <= 1 ) {
			candidatesByProp.addAll(candidatesOfSize);
		}				
		
		double[][][][] newSize = clone4DArray(base);		

		total_size = total_size + candidatesOfSize.size();
		
		// Tratar o resto dos tamanhos 
		for (size = 2; size <= maxSize; size++) {
			
			// Precompute de distance matrix
   			newSize = newSize(trajectory, trajectories, base, newSize, size);
			
			candidatesOfSize = growPivots(candidatesOfSize, trajectory, trajectories, base, newSize, size);
//			GAMMA = getDescriptor().getParamAsDouble("gamma");

			calculateProportion(candidatesOfSize, random);
			orderCandidates(candidatesOfSize);
			candidatesOfSize = filterOvelappingPoints(candidatesOfSize);
			candidatesOfSize = filterByProportion(candidatesOfSize, random);
	
			total_size = total_size + candidatesOfSize.size();
			
			if (size >= minSize){
				
				//for (Subtrajectory candidate : candidatesOfSize) assesQuality(candidate);				
//				candidatesOfSize.forEach(x -> assesQuality(x, random));
				candidatesByProp.addAll(candidatesOfSize);
			}
		
//			lastSize = newSize;
						
		} // for (int size = 2; size <= max; size++)	
		
		/** STEP 2.2: SELECTING BEST CANDIDATES */	
		orderCandidates(candidatesByProp);
		List<Subtrajectory> bestCandidates = filterEqualCandidates(candidatesByProp);
		bestCandidates = filterByQuality(candidatesByProp, random, trajectory);
		
		/* STEP 2.1.5: Recover Approach (IF Nothing found)
		 * * * * * * * * * * * * * * * * * * * * * * * * */
		if (bestCandidates.isEmpty()) { 
			n = (int) Math.ceil((double) (candidatesByProp.size()+bucket.size()) * 0.1); // By 10%
			orderCandidates(bucket);
			bucket = filterEqualCandidates(bucket);
			
			for (int i = n; i < n*10; i += n) {
				bestCandidates = filterByQuality(bucket.subList(i-n, (i > bucket.size()? bucket.size() : i)), random, trajectory);
				
				if (i > bucket.size() || !bestCandidates.isEmpty()) break;
			}
		}
		
//		List<Subtrajectory> bestCandidates = filterByQuality(candidatesByProp, random, trajectory);
		
		queue.removeAll(getCoveredInClass(bestCandidates));		
	
		progressBar.plus("Class: " + trajectory.getMovingObject() 
						+ ". Trajectory: " + trajectory.getTid() 
						+ ". Trajectory Size: " + trajectory.getPoints().size() 
						+ ". Number of Candidates: " + candidatesByProp.size() 
						+ ". Total of Movelets: " + bestCandidates.size() 
						+ ". Max Size: " + maxSize
						+ ". Used Features: " + this.maxNumberOfFeatures);
//						+ ". Memory Use: " + Mov3letsUtils.getUsedMemory());
	
		base =  null;
		newSize = null;
	
		return bestCandidates;
	}

	public List<Subtrajectory> growPivots(List<Subtrajectory> candidatesOfSize, MAT<MO> trajectory,
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

	public Subtrajectory buildNewSize(Subtrajectory candidate, MAT<MO> trajectory, List<MAT<MO>> trajectories,
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

	public List<Subtrajectory> filterByProportion(List<Subtrajectory> candidatesByProp, Random random) {
//		calculateProportion(candidatesByProp, random);

		// Relative TAU based on the higher proportion:
		double rel_tau = (candidatesByProp.size() > 0? candidatesByProp.get(0).getQuality().getData().get("quality") : 0.0) * TAU;
		
		/* STEP 2.1.2: SELECT ONLY CANDIDATES WITH PROPORTION > 50%
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		List<Subtrajectory> orderedCandidates = new ArrayList<>();
		for(Subtrajectory candidate : candidatesByProp)
			if(candidate.getQuality().getData().get("quality") >= rel_tau) //TAU)
				orderedCandidates.add(candidate);
			else 
//				break;
				bucket.add(candidate);
		
//		if (orderedCandidates.isEmpty()) return orderedCandidates;
				
		/* STEP 2.1.4: IDENTIFY EQUAL CANDIDATES -> not for pivots
		 * * * * * * * * * * * * * * * * * * * * * * * * */
//		List<Subtrajectory> bestCandidates = filterEqualCandidates(orderedCandidates);
//		return bestCandidates;
		
//		List<Subtrajectory> bestCandidates = orderedCandidates;// new ArrayList<>();
//		
//		bestCandidates = bestCandidates.subList(0, (int) Math.ceil((double) bestCandidates.size() * GAMMA));
		
		return orderedCandidates;
	}

	public List<Subtrajectory> filterOvelappingPoints(List<Subtrajectory> orderedCandidates) {
		
		List<Subtrajectory> bestCandidates = new ArrayList<>();		
		for(Subtrajectory candidate : orderedCandidates) {
			
			if(bestCandidates.isEmpty())
				bestCandidates.add(candidate);
			else {
				boolean similar = false;
				for(Subtrajectory best_candidate : bestCandidates) {
					
					if((best_candidate.getEnd() > candidate.getStart()) &&
					   (best_candidate.getStart() < candidate.getEnd())) {
						similar = true;
						break;
					}
					
				}
				if(!similar) {
					bestCandidates.add(candidate);
				} else
					bucket.add(candidate);
			}
		}
		
		return bestCandidates;
	}
	
//	public Set<MAT<MO>> getCoveredInClass(List<Subtrajectory> bestCandidates) {
//		Set<MAT<MO>> covered = new LinkedHashSet<MAT<MO>>();
//		Map<MAT<?>, Integer> count = new HashMap<MAT<?>, Integer>();
//
//		for (int i = 0; i < bestCandidates.size(); i++) {
//			for (MAT<?> T : bestCandidates.get(i).getCovered()) {
//				int x = count.getOrDefault(T, 0); 
//				x++;
//				count.put(T, x);
//			}
////			if (covered.isEmpty())
////				covered.addAll((List) bestCandidates.get(i).getCovered());
////			else
////				covered.retainAll((List) bestCandidates.get(i).getCovered());
//		}
//		
//		for (Entry<MAT<?>, Integer> e : count.entrySet()) {
//			if (e.getValue() >= (this.trajsFromClass.size() / 2))
//				covered.add((MAT<MO>) e.getKey());
//		}
//		
////		for (int j = 0; j < count.length; j++) {
////			if (count[j] >= this.trajsFromClass.size() * TAU)
////				covered.add(this.trajsFromClass.get(j));
////		}
//		
//		return covered;
//	}

}
