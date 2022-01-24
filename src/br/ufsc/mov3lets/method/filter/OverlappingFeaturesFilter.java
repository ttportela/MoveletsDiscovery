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
public class OverlappingFeaturesFilter extends BestMoveletsFilter {
	
	protected int[] pointFeatures;
	protected List<Subtrajectory> bucket;

	public OverlappingFeaturesFilter(int[] pointFeatures, List<Subtrajectory> bucket) {
		this.pointFeatures = pointFeatures;
		this.bucket = bucket;
	}

	/**
	 * Mehod overlappingCandidates. 
	 * 
	 * @param bestCandidates
	 */
	@Override
	public List<Subtrajectory> filter(List<Subtrajectory> bestCandidates) {
		List<Subtrajectory> recovered = new ArrayList<Subtrajectory>();
		for (Subtrajectory candidate : bucket) {
			if (areFeaturesSimilar(candidate, pointFeatures, 1)) {
				recovered.add(candidate);
			}
		}		
//		bestCandidates.addAll(recovered);
		bucket.removeAll(recovered);
		
		return recovered;
	}

}
