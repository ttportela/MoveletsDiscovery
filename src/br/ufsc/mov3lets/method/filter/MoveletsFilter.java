package br.ufsc.mov3lets.method.filter;

import java.util.Comparator;
import java.util.List;

import br.ufsc.mov3lets.model.Subtrajectory;

public abstract class MoveletsFilter {
	
	public abstract List<Subtrajectory> filter(List<Subtrajectory> rankedCandidates);

	/**
	 * Order candidates.
	 *
	 * @param candidatesByProp the candidates by prop
	 * @return 
	 */
	public List<Subtrajectory> orderCandidatesByQuality(List<Subtrajectory> candidatesByProp) {
		/* STEP 2.1.3: SORT THE CANDIDATES BY PROPORTION VALUE
		 * * * * * * * * * * * * * * * * * * * * * * * * * */
		candidatesByProp.sort(new Comparator<Subtrajectory>() {
			@Override
			public int compare(Subtrajectory o1, Subtrajectory o2) {
				
//				return (-1) * o1.getQuality().compareTo(o2.getQuality());
				return o1.getQuality().compareTo(o2.getQuality());				
				
			}
		});
		
		return candidatesByProp;
	}
	
}
