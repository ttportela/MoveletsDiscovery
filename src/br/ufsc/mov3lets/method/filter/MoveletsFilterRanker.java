package br.ufsc.mov3lets.method.filter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.ufsc.mov3lets.model.Subtrajectory;

public class MoveletsFilterRanker implements MoveletsFilter, MoveletsRanker {

	protected double qualityProp = 0.0;
	
	public MoveletsFilterRanker() {}
	
	public MoveletsFilterRanker(double qualityProp) {
		this.qualityProp = qualityProp;
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
			if (rankedCandidates.get(i).getQuality().getValue() <= qualityProp)
				return rankedCandidates.subList(0, i);

		}

		return rankedCandidates;
	}
	
	/**
	 * Rank candidates.
	 *
	 * @param candidates the candidates
	 * @return the list
	 */
	public List<Subtrajectory> rank(List<Subtrajectory> candidates) {

		List<Subtrajectory> orderedCandidates = new ArrayList<>(candidates);
		
		orderedCandidates.removeIf(e -> e == null);
		
		orderedCandidates.sort(new Comparator<Subtrajectory>() {
			@Override
			public int compare(Subtrajectory o1, Subtrajectory o2) {
				
				return o1.getQuality().compareTo(o2.getQuality());				
				
			}
		});

		return orderedCandidates;
	}
	
}
