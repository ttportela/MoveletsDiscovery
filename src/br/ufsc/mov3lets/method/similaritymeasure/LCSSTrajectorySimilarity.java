package br.ufsc.mov3lets.method.similaritymeasure;

import java.util.List;

import br.ufsc.mov3lets.method.output.SimilarityMatrixOutputter;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.utils.log.ProgressBar;

public class LCSSTrajectorySimilarity extends TrajectorySimilarityMeasure {
	
	public LCSSTrajectorySimilarity(List<MAT<?>> data, Double threshold, 
			Descriptor descriptor, ProgressBar progressBar, SimilarityMatrixOutputter outputer) {
		super(data, threshold, descriptor, progressBar, outputer);
	}

	@Override
	public double similarity(MAT<?> t1, MAT<?> t2) {
		int[][] matrix = new int[t1.getPoints().size() + 1][t2.getPoints().size() + 1];
		int i = 1;
		int j = 1;

		for (Point pointT1 : t1.getPoints()) {
			for (Point pointT2 : t2.getPoints()) {
				if (this.matches(pointT1, pointT2) == 1) {
					matrix[i][j] = matrix[i - 1][j - 1] + 1;
				} else {
					matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
				}

				j += 1;
			}

			i += 1;
			j = 1;
		}

		return matrix[t1.getPoints().size()][t2.getPoints().size()] / (double) Math.min(t1.getPoints().size(), t2.getPoints().size());
	}

}
