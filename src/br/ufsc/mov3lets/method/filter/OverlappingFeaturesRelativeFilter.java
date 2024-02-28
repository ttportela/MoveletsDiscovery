/**
 * 
 */
package br.ufsc.mov3lets.method.filter;

import java.util.List;

import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * @author tarlisportela
 *
 */
public class OverlappingFeaturesRelativeFilter extends OverlappingFeaturesFilter {
	
//	protected int[] pointFeatures;
	protected List<Subtrajectory> bucket;
	
	protected double selfSimilarityProp;

	public OverlappingFeaturesRelativeFilter(double selfSimilarityProp, double minimumQuality, List<Subtrajectory> bucket) {
		super(selfSimilarityProp, minimumQuality, bucket);
	}

	public OverlappingFeaturesRelativeFilter(double selfSimilarityProp, List<Subtrajectory> bucket) {
		super(selfSimilarityProp, 0.0, bucket);
	}

	/**
	 * Overlapping points and features filter. 
	 * 
	 * @param bestCandidates
	 */
	@Override
	public List<Subtrajectory> filter(List<Subtrajectory> candidates) {
		List<Subtrajectory> rankedCandidates = rank(candidates);
		
		// Sets a relative minimum quality based on the best qualified candidate:
		minimumQuality = rankedCandidates.get(0).getQuality().getValue() * minimumQuality;
		
		// Everything less than relative quality goes to the bucket. 
		for (int i = 0; (i < rankedCandidates.size()); i++) {
			
			// less than min. quality or zero will slipt the candidates ranking
			if (rankedCandidates.get(i).getQuality().getValue() < minimumQuality || 
				rankedCandidates.get(i).getQuality().hasZeroQuality()) {
				if (bucket != null) bucket.addAll(rankedCandidates.subList(i, rankedCandidates.size()));
				return rankedCandidates.subList(0, i);
			}

			Subtrajectory candidate = rankedCandidates.get(i);

			// Removing self similar
			if (searchIfFeaturesSimilarity(candidate, rankedCandidates.subList(0, i), selfSimilarityProp)) {
				rankedCandidates.remove(i);
				if (bucket != null) bucket.add(candidate);
				i--;
			}

		}

		return rankedCandidates;
	}

}
