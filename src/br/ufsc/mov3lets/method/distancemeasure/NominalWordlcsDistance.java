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
public class NominalWordlcsDistance extends DistanceMeasure<Aspect<String>> {

	@Override
	public double distance(Aspect<String> asp0, Aspect<String> asp1, AttributeDescriptor attr) {
        // Find LCS by word
		String[] X = asp0.getValue().split(" ");
		String[] Y = asp1.getValue().split(" ");
        int m = X.length, n = Y.length;
        int L[][] = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0 || j == 0) {
                    L[i][j] = 0;
                } else if (X[i - 1].equalsIgnoreCase( Y[j - 1]) ) {
                    L[i][j] = L[i - 1][j - 1] + 1;
                } else {
                    L[i][j] = Math.max(L[i - 1][j], L[i][j - 1]);
                }
            }
        }
        
        return L[m][n];
    }

}
