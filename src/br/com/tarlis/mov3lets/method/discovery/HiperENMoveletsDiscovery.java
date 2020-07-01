/**
 * 
 */
package br.com.tarlis.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import br.com.tarlis.mov3lets.method.qualitymeasure.QualityMeasure;
import br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

/**
 * @author tarlis
 * @param <MO>
 *
 */
public class HiperENMoveletsDiscovery<MO> extends HiperCEMoveletsDiscovery<MO> {

	protected List<MAT<MO>> sampleTrajectories;

	/**
	 * @param trajsFromClass
	 * @param train
	 * @param candidates
	 * @param qualityMeasure
	 * @param descriptor
	 */
	public HiperENMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajsFromClass, data, train, test, qualityMeasure, descriptor);
	}

	public void orderCandidates(List<Subtrajectory> candidatesByProp) {
		/* STEP 2.1.3: SORT THE CANDIDATES BY PROPORTION VALUE
		 * * * * * * * * * * * * * * * * * * * * * * * * * */
		candidatesByProp.sort(new Comparator<Subtrajectory>() {
			@Override
			public int compare(Subtrajectory o1, Subtrajectory o2) {
				
//				return (-1) * o1.getQuality().compareTo(o2.getQuality());
				return (-1) * Double.compare(entrophy(o1), entrophy(o2));				
				
			}
		});
	}
	
	public List<Subtrajectory> filterByProportion(List<Subtrajectory> candidatesByProp, Random random) {
		calculateProportion(candidatesByProp, random);

		/* STEP 2.1.2: SELECT ONLY CANDIDATES WITH PROPORTION > 50%
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		List<Subtrajectory> orderedCandidates = new ArrayList<>();
		for(Subtrajectory candidate : candidatesByProp)
			if(entrophy(candidate) >= TAU)
				orderedCandidates.add(candidate);
			else 
//				break;
				bucket.add(candidate);
		
		if (orderedCandidates.isEmpty()) return orderedCandidates;
				
		List<Subtrajectory> bestCandidates = filterEqualCandidates(orderedCandidates);
		
//		bestCandidates = bestCandidates.subList(0, (int) Math.ceil((double) bestCandidates.size() * GAMMA));
		
		return bestCandidates;
	}

	public Double entrophy(Subtrajectory candidate) {
		double p = candidate.getQuality().getData().get("p_target") / candidate.getQuality().getData().get("p_nontarget");
		return -p * (Math.log(p));
//		return -Math.log(candidate.getQuality().getData().get("p_target") / candidate.getQuality().getData().get("p_nontarget"));
	}

}
