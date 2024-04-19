package br.ufsc.mov3lets.method.filter;

import java.util.List;

import br.ufsc.mov3lets.model.Subtrajectory;

public class BestMoveletsFilter extends MoveletsFilterRanker {

	protected double selfSimilarityProp = 0.0;
	
	protected double minimumQuality = 0.0;
	
	public BestMoveletsFilter() {}
	
	public BestMoveletsFilter(double selfSimilarityProp, double minimumQuality) {
		this.selfSimilarityProp = selfSimilarityProp;
		this.minimumQuality = minimumQuality;
	}
	
	/**
	 * Best shapelets.
	 *
	 * @param rankedCandidates the ranked candidates
	 * @param selfSimilarityProp the self similarity prop
	 * @param bucket NOT REQUIRED
	 * @return the list
	 */
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
				rankedCandidates.get(i).getQuality().hasZeroQuality())
				return rankedCandidates.subList(0, i);

			Subtrajectory candidate = rankedCandidates.get(i);

			// Removing self similar
			if (searchIfSelfSimilarity(candidate, rankedCandidates.subList(0, i), selfSimilarityProp)) {
				rankedCandidates.remove(i);
				i--;
			}

		}

		return rankedCandidates;
	}

	/**
	 * Search if self similarity.
	 *
	 * @param candidate the candidate
	 * @param list the list
	 * @param selfSimilarityProp the self similarity prop
	 * @return true, if successful
	 */
	public boolean searchIfSelfSimilarity(Subtrajectory candidate, List<Subtrajectory> list,
			double selfSimilarityProp) {

		for (Subtrajectory s : list) {
			if (areSelfSimilar(candidate, s, selfSimilarityProp))
				return true;
		}

		return false;
	}
	
	/**
	 * Are self similar.
	 *
	 * @param candidate the candidate
	 * @param subtrajectory the subtrajectory
	 * @param selfSimilarityProp the self similarity prop
	 * @return true, if successful
	 */
	public boolean areSelfSimilar(Subtrajectory candidate, Subtrajectory subtrajectory,
			double selfSimilarityProp) {
		
		//return false;
		
		// If their tids are different return false
		
		if (candidate.getTrajectory().getTid() != subtrajectory.getTrajectory().getTid())
			return false;

		else if (candidate.getStart() < subtrajectory.getStart()) {

			if (candidate.getEnd() < subtrajectory.getStart())
				return false;

			if (selfSimilarityProp == 0)
				return true;

			double intersection = (candidate.getEnd() - subtrajectory.getStart())
					/ (double) Math.min(candidate.getSize(), subtrajectory.getSize());

			return intersection >= selfSimilarityProp;

		} else {

			if (subtrajectory.getEnd() < candidate.getStart())
				return false;

			if (selfSimilarityProp == 0)
				return true;

			double intersection = (subtrajectory.getEnd() - candidate.getStart())
					/ (double) Math.min(candidate.getSize(), subtrajectory.getSize());

			return intersection >= selfSimilarityProp;

		}

	}
	
	/**
	 * Are self similar.
	 *
	 * @param candidate the candidate
	 * @param subtrajectory the subtrajectory
	 * @param selfSimilarityProp the self similarity prop
	 * @return true, if successful
	 */
	public boolean areFeaturesSimilar(int[] pointFeaturesA, int[] pointFeaturesB,
			double featuresSimilarityProp) {
		
		double intersection = 0.0;
		
		for (int k : pointFeaturesA)
			for (int j : pointFeaturesB)
				if (k == j)
					intersection += 1.0;

		intersection = intersection
				/ (double) Math.min(pointFeaturesA.length, pointFeaturesB.length);
		
		return intersection <= featuresSimilarityProp;

	}
	
	public double getMinimumQuality() {
		return minimumQuality;
	}
	
	public double getSelfSimilarityProp() {
		return selfSimilarityProp;
	}
	
}
