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
public class FrequentFeaturesCandidatesFilter extends FeaturesCandidatesFilter {

	protected double TAU;

	public FrequentFeaturesCandidatesFilter(int numberOfFeatures, int maxNumberOfFeatures, double tau) {
		super(numberOfFeatures, maxNumberOfFeatures);
		this.TAU = tau;
	}
	
	@Override
	public List<Subtrajectory> filter(List<Subtrajectory> candidatesByProp) {
		int[] attribute_usage = new int [this.numberOfFeatures]; // array of ints

		for(Subtrajectory candidate : candidatesByProp)
			for (int i : candidate.getPointFeatures())
				attribute_usage[i]++;

		// Selection of dimensions:
		// Limit by most frequent dimensions from best candidates
		List<Integer> features = new ArrayList<Integer>();
		for (int i = 0; i < attribute_usage.length; i++) {
			if (attribute_usage[i] >= (candidatesByProp.size() * TAU))
				features.add(i);
		}
		
		int[] pointFeatures = features.stream().mapToInt(Integer::valueOf).toArray();
		List<Subtrajectory> filteredCandidates = new ArrayList<>();
		for (Subtrajectory candidate : candidatesByProp) {
			if (areFeaturesSimilar(candidate, pointFeatures, 1.0)) {
				filteredCandidates.add(candidate);
			}
		}		
//		candidatesByProp.removeAll(recovered);
//		bucket.addAll(recovered);
		
		this.maxNumberOfFeatures = Math.min(pointFeatures.length, this.maxNumberOfFeatures);
		
		return filteredCandidates;
	}

}
