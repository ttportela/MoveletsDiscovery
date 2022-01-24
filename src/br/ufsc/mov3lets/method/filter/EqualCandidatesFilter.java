package br.ufsc.mov3lets.method.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.Subtrajectory;
import br.ufsc.mov3lets.model.aspect.Aspect;

public class EqualCandidatesFilter extends MoveletsFilter {
	
	protected Descriptor descriptor;
	
	public EqualCandidatesFilter(Descriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	/**
	 * Filter equal candidates.
	 *
	 * @param orderedCandidates the ordered candidates
	 * @param bucket NOT REQUIRED
	 * @return the list
	 */
	@Override
	public List<Subtrajectory> filter(List<Subtrajectory> orderedCandidates) {
		/* STEP 2.1.4: IDENTIFY EQUAL CANDIDATES
		 * * * * * * * * * * * * * * * * * * * * * * * * */
		List<Subtrajectory> bestCandidates = new ArrayList<>();
//		int[] attribute_usage = new int [numberOfFeatures]; // array of 5 ints
		
		for(Subtrajectory candidate : orderedCandidates) {
			
			if(bestCandidates.isEmpty())
				bestCandidates.add(candidate);
			else {
				boolean equal = false;
				for(Subtrajectory best_candidate : bestCandidates) {
					
					List<HashMap<Integer, Aspect<?>>> used_features_c1 = getDimensions(candidate);
					List<HashMap<Integer, Aspect<?>>> used_features_c2 = getDimensions(best_candidate);
					
					if(used_features_c1.size()==used_features_c2.size())
						if(areEqual(used_features_c1, used_features_c2)) {
							equal = true;
							break;
						}
					
				}
				if(!equal) {
					bestCandidates.add(candidate);
//					attribute_usage[candidate.getPointFeatures().length-1]++;
				}
			}
		}
		return bestCandidates;
	}
	
	/**
	 * Gets the dimensions.
	 *
	 * @param candidate the candidate
	 * @return the dimensions
	 */
	public List<HashMap<Integer, Aspect<?>>> getDimensions(Subtrajectory candidate) {
		
		List<Integer> features_in_movelet = new ArrayList<>();
		
		int[] list_features = candidate.getPointFeatures();
		
		for(int i=0; i <= descriptor.getAttributes().size(); i++) {
			
			if(ArrayUtils.contains(list_features, i))				
				features_in_movelet.add(i);
			
		}
		
		List<HashMap<Integer, Aspect<?>>> used_features = new ArrayList<>();
		
		for(int i=0; i < candidate.getPoints().size(); i++) {
			
			Point point = candidate.getPoints().get(i);
			
			HashMap<Integer, Aspect<?>> features_in_point = new HashMap<>();
			
			for(Integer feature : features_in_movelet) {
				features_in_point.put(feature, point.getAspects().get(feature));
			}
			
			used_features.add(features_in_point);
		}
		
		return used_features;
	}
	
	/**
	 * Are equal.
	 *
	 * @param first the first
	 * @param second the second
	 * @return true, if successful
	 */
	public boolean areEqual(List<HashMap<Integer, Aspect<?>>> first, List<HashMap<Integer, Aspect<?>>> second) {
		
		if (first.size() != second.size())
	        return false;
		
		if (first.get(0).size() != second.get(0).size())
	        return false;
	 
		for ( Integer key : first.get(0).keySet() ) {
			if(!second.get(0).containsKey(key)) {
		        return false;
		    }
		}
		
		boolean all_match = true;
		
		for(int i=0; i<first.size();i++) {
			
			HashMap<Integer, Aspect<?>> f = first.get(i);
			HashMap<Integer, Aspect<?>> s = second.get(i);
						
			if(!f.entrySet().stream()
				      .allMatch(e -> e.getValue().equals(s.get(e.getKey()))))
				return false;
			
		}
	    return all_match;
	}

}
