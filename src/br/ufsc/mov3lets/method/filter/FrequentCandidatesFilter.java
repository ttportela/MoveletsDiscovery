package br.ufsc.mov3lets.method.filter;

import java.util.List;

import br.ufsc.mov3lets.model.Subtrajectory;

public class FrequentCandidatesFilter implements MoveletsFilter {

	protected double rel_tau;
	protected int n;
	protected List<Subtrajectory> bucket;

	public FrequentCandidatesFilter(double rel_tau, int n, List<Subtrajectory> bucket) {
		this.rel_tau = rel_tau;
		this.n = n;
		this.bucket = bucket;
	}
	
	/**
	 * Filter by proportion.
	 *
	 * @param candidatesByProp the candidates by prop
	 * @param random the random
	 * @return the list
	 */
	@Override
	public List<Subtrajectory> filter(List<Subtrajectory> candidatesByProp) {
		/* STEP 2.1.2: SELECT ONLY CANDIDATES WITH PROPORTION > 50%
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
//		List<Subtrajectory> orderedCandidates = new ArrayList<>();
		List<Subtrajectory> orderedCandidates = candidatesByProp;
		for (int i = 0; i < candidatesByProp.size(); i++) {
			if(i >= n || candidatesByProp.get(i).getQuality().getData().get("quality") < rel_tau) {
				orderedCandidates = candidatesByProp.subList(0, i);
				bucket.addAll(candidatesByProp.subList(i, candidatesByProp.size()));
				break;
			}
		}
//		for(Subtrajectory candidate : candidatesByProp)
//			if(orderedCandidates.size() <= n &&
//				candidate.getQuality().getData().get("quality") >= rel_tau) //TAU)
//				orderedCandidates.add(candidate);
//			else 
//				bucket.add(candidate);
		
		return orderedCandidates;
	}

//	/**
//	 * Mehod candidateQuality. 
//	 * 
//	 * @param candidate
//	 * @return
//	 */
//	protected double candidateQuality(Subtrajectory candidate) {
//		return candidate.getQuality().getData().get("quality");
//	}
	
}
