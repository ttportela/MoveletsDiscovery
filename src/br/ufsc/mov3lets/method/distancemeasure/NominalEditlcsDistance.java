/**
 * 
 */
package br.ufsc.mov3lets.method.distancemeasure;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.model.aspect.Aspect;

/**
 * @author tarlisportela
 *
 */
public class NominalEditlcsDistance extends NominalLcsDistance {
	
	@Override
	public double distance(Aspect<String> asp0, Aspect<String> asp1, AttributeDescriptor attr) {
		double lcs = super.distance(asp0, asp1, attr);

        int m = asp0.getValue().length(), n = asp0.getValue().length();
        // Edit distance is delete operations +
        // insert operations.
        return ((m - lcs) + (n - lcs));
	}
	
}
