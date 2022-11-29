package br.ufsc.mov3lets.method.filter;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.mov3lets.model.Subtrajectory;

public class FeaturesCandidatesFilter extends BestMoveletsFilter {
	
	protected int numberOfFeatures;
	protected int maxNumberOfFeatures;

	public FeaturesCandidatesFilter(int numberOfFeatures, int maxNumberOfFeatures) {
		this.numberOfFeatures = numberOfFeatures;
		this.maxNumberOfFeatures = maxNumberOfFeatures;
	}

	@Override
	public List<Subtrajectory> filter(List<Subtrajectory> candidatesByProp) {
		int[] attribute_usage = new int [this.numberOfFeatures]; // array of ints

		for(Subtrajectory candidate : candidatesByProp)
			attribute_usage[candidate.getPointFeatures().length-1]++;
		
		// Selection of threshold:
		// Limit by mode of dimension usage from best candidates.
		int LAMBDA = -1;
		for (int i = 0; i < attribute_usage.length; i++) {
			if (LAMBDA <= attribute_usage[i])
				LAMBDA = i+1;
		}
		
		// Include every other candidate with overlapping points
//		candidatesByProp.addAll(overlappingCandidatesByPoints(candidatesByProp));
		
		List<Subtrajectory> filteredCandidates = new ArrayList<>();
		for(Subtrajectory candidate : candidatesByProp)
			if(candidate.getPointFeatures().length <= LAMBDA)
				filteredCandidates.add(candidate);
		
		this.maxNumberOfFeatures = Math.min(LAMBDA, this.maxNumberOfFeatures);
		
		return filteredCandidates;
	}
	
	public int getMaxNumberOfFeatures() {
		return maxNumberOfFeatures;
	}

}
