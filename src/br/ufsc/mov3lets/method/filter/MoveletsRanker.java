package br.ufsc.mov3lets.method.filter;

import java.util.List;

import br.ufsc.mov3lets.model.Subtrajectory;

public interface MoveletsRanker {
	
	/**
	 * Rank candidates.
	 *
	 * @param candidates the candidates
	 * @return the list
	 */
	public List<Subtrajectory> rank(List<Subtrajectory> candidates);
	
}
