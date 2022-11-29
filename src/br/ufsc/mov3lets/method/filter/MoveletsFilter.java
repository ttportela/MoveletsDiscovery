package br.ufsc.mov3lets.method.filter;

import java.util.List;

import br.ufsc.mov3lets.model.Subtrajectory;

public interface MoveletsFilter {
	
	public List<Subtrajectory> filter(List<Subtrajectory> candidates);
	
}
