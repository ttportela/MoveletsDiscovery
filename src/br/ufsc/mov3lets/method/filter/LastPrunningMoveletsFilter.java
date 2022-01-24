package br.ufsc.mov3lets.method.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.Subtrajectory;

public class LastPrunningMoveletsFilter extends EqualCandidatesFilter {

	public LastPrunningMoveletsFilter(Descriptor descriptor) {
		super(descriptor);
	}
	
	/**
	 * Last Prunning Method
	 * 
	 * @param candidates
	 * @return
	 */
	@Override
	public List<Subtrajectory> filter(List<Subtrajectory> movelets) {
		List<Subtrajectory> noveltyShapelets = new ArrayList<>();
		Set<Integer> allCovered = new HashSet<Integer>();
		
		for (int i = 0; i < movelets.size(); i++) {
			double[][] distances = movelets.get(i).getDistances();
			double[] splitpoint = movelets.get(i).getSplitpoints();
			Set<Integer> currentCovered = findIndexesLowerSplitPoint(distances, splitpoint);
			
			if ( ! SetUtils.difference(currentCovered, allCovered).isEmpty()){
				noveltyShapelets.add(movelets.get(i));
				allCovered.addAll(currentCovered);
			}
		}
		
		return noveltyShapelets;
	}
	
	/**
	 * Find indexes lower split point.
	 *
	 * @param distances the distances
	 * @param splitpoints the splitpoints
	 * @return the sets the
	 */
	public Set<Integer> findIndexesLowerSplitPoint(double[][] distances, double[] splitpoints) {
		Set<Integer> indexes = new HashSet<>();
		
		RealMatrix rm = new Array2DRowRealMatrix(distances);
		
		for (int i = 0; i < distances[0].length; i++) {
			if (isCovered(rm.getColumn(i), splitpoints) )			
				indexes.add(i);
			}
		
		return indexes;		
	}

	/**
	 * Checks if is covered.
	 *
	 * @param point the point
	 * @param limits the limits
	 * @return true, if is covered
	 */
	/* Para o caso de empate por conta de movelets discretas
	 */
	public boolean isCovered(double[] point, double[] limits){
		
		int dimensions = limits.length;
		
		for (int i = 0; i < dimensions; i++) {
			if (limits[i] > 0){
				if (point[i] >= limits[i])
					return false;
			} else
				if (point[i] > limits[i])
					return false;
		}
		
		return true;
	}

}
