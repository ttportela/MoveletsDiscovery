/**
 * 
 */
package br.ufsc.mov3lets.method.filter;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * @author tarlisportela
 *
 */
public class OverlappingPointsFilter extends BestMoveletsFilter {
	
	protected List<Subtrajectory> bucket;

	public OverlappingPointsFilter(List<Subtrajectory> bucket) {
		this.bucket = bucket;
	}

//	/**
//	 * Filter ovelapping points.
//	 *
//	 * @param orderedCandidates the ordered candidates
//	 * @param bucket NOT REQUIRED
//	 * @return the list
//	 */
//	@Override
//	public List<Subtrajectory> filter(List<Subtrajectory> orderedCandidates) {
//		List<Subtrajectory> bestCandidates = new ArrayList<>();		
//		for(Subtrajectory candidate : orderedCandidates) {
//			
//			if(bestCandidates.isEmpty())
//				bestCandidates.add(candidate);
//			else {
//				boolean similar = false;
//				for(Subtrajectory best_candidate : bestCandidates) {
//					
//					if((best_candidate.getEnd() > candidate.getStart()) &&
//					   (best_candidate.getStart() < candidate.getEnd())) {
//						similar = true;
//						break;
//					}
//					
//				}
//				if(!similar) {
//					bestCandidates.add(candidate);
//				} else
//					bucket.add(candidate);
//			}
//		}
//		
//		return bestCandidates;
//		
////		List<Subtrajectory> recovered = new ArrayList<Subtrajectory>();
////		for (Subtrajectory candidate1 : bestCandidates) {
////			for (Subtrajectory candidate2 : bucket) {
////				if (areSelfSimilar(candidate1, candidate2, 0)) {
////					recovered.add(candidate2);
////				}
////			}		
////		}
////		
////		bucket.removeAll(recovered);
////		
////		return recovered;
//	}

	/**
	 * Filter ovelapping points.
	 *
	 * @param orderedCandidates the ordered candidates
	 * @param bucket NOT REQUIRED
	 * @return the list
	 */
	public List<Subtrajectory> filter(List<Subtrajectory> candidates) {
		
		List<Subtrajectory> rankedCandidates = new ArrayList<>(rank(candidates));
		
		for (int i = 0; (i < rankedCandidates.size()); i++) {

			Subtrajectory candidate = rankedCandidates.get(i);

			// Removing self similar
			if (searchIfSelfSimilarity(candidate, rankedCandidates.subList(0, i), selfSimilarityProp)) {
				rankedCandidates.remove(i);
				i--;
			}

		}

		return rankedCandidates;
	}

}
