/**
 * 
 */
package br.ufsc.mov3lets.method.similaritymeasure;

import java.util.List;

import br.ufsc.mov3lets.method.output.SimilarityMatrixOutputter;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.utils.log.ProgressBar;

/**
 * @author tarlisportela
 *
 */
public class EDRTrajectorySimilarity extends TrajectorySimilarityMeasure {

	public EDRTrajectorySimilarity(List<MAT<?>> data, Double threshold, 
			Descriptor descriptor, ProgressBar progressBar, SimilarityMatrixOutputter outputer) {
		super(data, threshold, descriptor, progressBar, outputer);
//		this.outputer = new SimilarityMatrixOutputter(descriptor.getParamAsText("respath"), "EDR");
	}

	@Override
	public double similarity(MAT<?> t1, MAT<?> t2) {
		int[][] matrix = new int[t1.getPoints().size() + 1][t2.getPoints().size() + 1];

		for (int k = 0; k < t1.getPoints().size() + 1; k++) {
			matrix[k][0] = k;
		}

		for (int k = 0; k < t2.getPoints().size() + 1; k++) {
			matrix[0][k] = k;
		}

		int i = 1;
		int j = 1;

		for (Point pointT1 : t1.getPoints()) {
			for (Point pointT2 : t2.getPoints()) {
				int cost = this.matches(pointT1, pointT2) == 0 ? 1 : 0;

				matrix[i][j] = Math.min(matrix[i - 1][j - 1] + cost,
						Math.min(matrix[i][j - 1] + 1, matrix[i - 1][j] + 1));

				j += 1;
			}

			i += 1;
			j = 1;
		}

		return 1 - matrix[t1.getPoints().size()][t2.getPoints().size()] / (double) Math.max(t1.getPoints().size(), t2.getPoints().size());
	}

}
