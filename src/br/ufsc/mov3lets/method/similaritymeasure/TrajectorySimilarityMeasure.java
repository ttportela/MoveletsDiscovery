/**
 * 
 */
package br.ufsc.mov3lets.method.similaritymeasure;

import java.util.List;
import java.util.concurrent.Callable;

import br.ufsc.mov3lets.method.output.SimilarityMatrixOutputter;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;
import br.ufsc.mov3lets.utils.log.ProgressBar;

/**
 * @author tarlisportela
 *
 */
public abstract class TrajectorySimilarityMeasure implements Callable<Integer> {

	/** The descriptor. */
	protected Descriptor descriptor;
	
	/** The progress bar. */
	protected ProgressBar progressBar;
	
	/** The data. */
	protected List<MAT<?>> data;
	
	/** The outputers. */
	protected SimilarityMatrixOutputter outputer;
	
	protected double threshold;
	

	public TrajectorySimilarityMeasure(List<MAT<?>> data, Double threshold, 
			Descriptor descriptor, ProgressBar progressBar, SimilarityMatrixOutputter outputer) {
		super();
		this.threshold = threshold;
		this.descriptor = descriptor;
		this.progressBar = progressBar;
		this.data = data;
		this.outputer = outputer;
	}

	public abstract double similarity(MAT<?> t1, MAT<?> t2);
	
	public double calculateDistance(Aspect<?> a, Aspect<?> b, AttributeDescriptor attr) {
		return attr.getDistanceComparator().enhance(
				attr.getDistanceComparator().normalizeDistance(
				attr.getDistanceComparator().calculateDistance(a, b, attr),
				attr.getComparator().getMaxValue()
		));
	}
	
	public double[][] computeScores(List<MAT<?>> data, boolean similarity, ProgressBar progressBar) {
		final int trajSize = data.size();
		
		String mxType = similarity ? "similarity" : "distance";
		progressBar.plus("Computing " + mxType + " matrix...");
		
		final int totalComp = (trajSize * trajSize) / 2 - trajSize;
		final int add = similarity ? 0 : -1;

		double[][] matrix = new double[trajSize][trajSize];
		int count = 0;
		
		for (int i = 0; i < trajSize; i++) {
			for (int j = 0; j <= i; j++) {
				matrix[i][j] = Math.abs(this.similarity(data.get(i), data.get(j)) + add);
				count++;
			}
//			int perc = (int) (count * 100.0 / totalComp);
			progressBar.plus(count + " / " + totalComp + " computations done.");
		}
		
		// Complete the upper half of the full matrix
		for (int i = 0; i < matrix.length; i++) {
			for (int j = i + 1; j < matrix[0].length; j++)
				matrix[i][j] = matrix[j][i];
		}

		progressBar.plus("Computing " + mxType + " matrix... DONE!");
		return matrix;
	}
	
	public int matches(Point p1, Point p2) {
		for (int k = 0; k < getDescriptor().getAttributes().size(); k++) {
			if (match(p1, p2, k))
				return 0;
		}

		return 1;
	}
	
	public boolean match(Point p1, Point p2, int k) {
		double distance = calculateDistance(
				p1.getAspects().get(k), 
				p2.getAspects().get(k), 
				getDescriptor().getAttributes().get(k)
		);
		return distance <= this.threshold;
	}
	
//	public double[][] concurrentComputeScores(List<MAT<?>> data, boolean similarity, ProgressBar progressBar) {
//		
//	}

	@Override
	public Integer call() throws Exception {

		boolean similarity = !getDescriptor().getFlag("compute_distances");
		
		double[][] scores = this.computeScores(data, similarity, progressBar);

		String mxType = similarity ? "Similarity" : "Distance";
		outputer.write(mxType+"Matrix", data, scores);
		
		return 0;
	}

	/**
	 * Gets the descriptor.
	 *
	 * @return the descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}
}
