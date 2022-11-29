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
public class OverlappingFeaturesFilter extends BestMoveletsFilter {
	
//	protected int[] pointFeatures;
	protected List<Subtrajectory> bucket;
	
	protected double selfSimilarityProp;

	public OverlappingFeaturesFilter(double selfSimilarityProp, double minimumQuality, List<Subtrajectory> bucket) {
		super(selfSimilarityProp, minimumQuality);
//		this.pointFeatures = pointFeatures;
		this.bucket = bucket;
	}

	public OverlappingFeaturesFilter(double selfSimilarityProp, List<Subtrajectory> bucket) {
		super(selfSimilarityProp, 0.0);
//		this.pointFeatures = pointFeatures;
		this.bucket = bucket;
	}

	/**
	 * Overlapping points and features filter. 
	 * 
	 * @param bestCandidates
	 */
	@Override
	public List<Subtrajectory> filter(List<Subtrajectory> candidates) {
		List<Subtrajectory> rankedCandidates = rank(candidates);
		
		// Realiza o loop até que acabem os atributos ou até que atinga o número
		// máximo de nBestShapelets
		// Isso é importante porque vários candidatos bem rankeados podem ser
		// selfsimilares com outros que tiveram melhor score;
		for (int i = 0; (i < rankedCandidates.size()); i++) {
			
			// Se a shapelet candidata tem score 0 então já termina o processo
			// de busca
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

	public boolean searchIfFeaturesSimilarity(Subtrajectory candidate, List<Subtrajectory> list,
			double selfSimilarityProp) {
		
		for (Subtrajectory s : list) {
			if (areSelfSimilar(candidate, s, selfSimilarityProp) &&
				areFeaturesSimilar(candidate.getPointFeatures(), s.getPointFeatures(), selfSimilarityProp))
				return true;
		}

		return false;
	}

}
