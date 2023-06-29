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
public class MSMTrajectorySimilarity extends TrajectorySimilarityMeasure {
	
	public MSMTrajectorySimilarity(List<MAT<?>> data, Double threshold, 
			Descriptor descriptor, ProgressBar progressBar, SimilarityMatrixOutputter outputer) {
		super(data, threshold, descriptor, progressBar, outputer);
	}

	@Override
	public double similarity(MAT<?> t1, MAT<?> t2) {
		double[][] scores = new double[t1.getPoints().size()][t2.getPoints().size()];
		double parityT1T2 = 0;
		double parityT2T1 = 0;
		
		for(int i = 0; i < t1.getPoints().size(); i++) {
			double maxCol = 0;
			
			for(int j = 0; j < t2.getPoints().size(); j++) {
				scores[i][j] = this.score(t1.getPoints().get(i), t2.getPoints().get(j));
				maxCol = scores[i][j] > maxCol ? scores[i][j] : maxCol;
			}
			
			parityT1T2 += maxCol;
		}
		
		for(int j = 0; j < t2.getPoints().size(); j++) {
			double maxRow = 0;
			
			for(int i = 0; i < t1.getPoints().size(); i++) {
				scores[i][j] = this.score(t1.getPoints().get(i), t2.getPoints().get(j));
				maxRow = scores[i][j] > maxRow ? scores[i][j] : maxRow;
			}
			
			parityT2T1 += maxRow;
		}
		
		return (parityT1T2 + parityT2T1) / (t1.getPoints().size() + t2.getPoints().size());
	}

	protected double score(Point p1, Point p2) {
		double total = 0;

		for (int k = 0; k < getDescriptor().getAttributes().size(); k++) {
			int match = 1;
			if (match(p1, p2, k))
				match = 0; 
			
			total += match * (1.0 / getDescriptor().getAttributes().size()); // TODO Weight;
		}

		return total;
	}

}
