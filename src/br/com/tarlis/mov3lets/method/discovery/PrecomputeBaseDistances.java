package br.com.tarlis.mov3lets.method.discovery;

import java.util.List;
import java.util.concurrent.Callable;

import br.com.tarlis.mov3lets.method.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.method.structures.Matrix3D;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Point;
import br.com.tarlis.mov3lets.utils.ProgressBar;

public class PrecomputeBaseDistances<MO> implements Callable<Matrix3D> {
	
	private int fromIndex;
	private List<MAT<MO>> trajectories;
	private Matrix3D base;
	private Descriptor descriptor;
	private ProgressBar bar;
	
	public PrecomputeBaseDistances(int fromIndex, List<MAT<MO>> trajectories, Matrix3D base, Descriptor descriptor, ProgressBar bar) {
		this.fromIndex = fromIndex;
		this.trajectories = trajectories;
		this.base = base;
		this.descriptor = descriptor;
		this.bar = bar;
	}

	@Override
	public Matrix3D call() throws Exception {
		return computeBaseDistances(fromIndex, this.trajectories);
	}
	
	public Matrix3D computeBaseDistances(int fromIndex, List<MAT<MO>> trajectories){
		MAT<?> trajectory = trajectories.get(fromIndex);
//		int n = trajectory.getPoints().size();
//		int size = 1;
		
		for (Point a : trajectory.getPoints()) {
//		for (int start = 0; start <= (n - size); start++) {		
//			Point a = trajectory.getPoints().get(start);
			
			for (int k = fromIndex; k < trajectories.size(); k++) {
				MAT<?> T = trajectories.get(k);
				
				for (Point b : T.getPoints()) {
//				for (int j = 0; j <= (T.getPoints().size()-size); j++) {
//					Point b = T.getPoints().get(j);
					
					double[] distances = new double[this.descriptor.getAttributes().size()];
					
					for (int i = 0; i < this.descriptor.getAttributes().size(); i++) {
						AttributeDescriptor attr = this.descriptor.getAttributes().get(i);
						distances[i] = this.descriptor.getAttributes().get(i)
								.getDistanceComparator().calculateDistance(
								a.getAspects().get(i), 
								b.getAspects().get(i), 
								attr); // This also enhance distances
					}

					// For each possible *Number Of Features* and each combination of those:
					base.addDistances(a, b, distances);
					
				} // for (int j = 0; j <= (train.size()-size); j++)
				
			} //for (MAT<?> T : trajectories) { --//-- for (int i = 0; i < train.size(); i++)
			
			bar.plus();
			
		} // for (int start = 0; start <= (n - size); start++)

		return base;
	}
	
	
	
}
