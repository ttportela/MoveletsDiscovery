package br.com.tarlis.mov3lets.method.discovery;

import java.util.List;
import java.util.concurrent.Callable;

import br.com.tarlis.mov3lets.method.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.method.structures.Matrix3D;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Point;
import br.com.tarlis.mov3lets.utils.StatusBar;

public class PrecomputeBaseDistances<MO> implements Callable<Matrix3D> {
	
	private MAT<?> trajectory;
	private List<MAT<MO>> trajectories;
	private Matrix3D base;
	private Descriptor descriptor;
	private StatusBar bar;
	
	public PrecomputeBaseDistances(MAT<?> trajectory, List<MAT<MO>> trajectories, Matrix3D base, Descriptor descriptor, StatusBar bar) {
		this.trajectory = trajectory;
		this.trajectories = trajectories;
		this.base = base;
		this.descriptor = descriptor;
		this.bar = bar;
	}

	@Override
	public Matrix3D call() throws Exception {
		return computeBaseDistances(trajectory, this.trajectories);
	}
	
	public Matrix3D computeBaseDistances(MAT<?> trajectory, List<MAT<MO>> trajectories){
//		int index = trajectories.indexOf(trajectory);
		int n = trajectory.getPoints().size();
		int size = 1;
		
		for (int start = 0; start <= (n - size); start++) {
//			base[start] = new double[train.size()][][];				
			
//			for (MAT<?> T : trajectories) {
			for (int k = 0; k < trajectories.size(); k++) {
						
				MAT<?> T = trajectories.get(k);
				Point a = trajectory.getPoints().get(start);
				
				for (int j = 0; j <= (T.getPoints().size()-size); j++) {
					Point b = T.getPoints().get(j);
					
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
					base.addCombinations(a, b, distances);
					
				} // for (int j = 0; j <= (train.size()-size); j++)
				
				bar.plusOne();
				
			} //for (MAT<?> T : trajectories) { --//-- for (int i = 0; i < train.size(); i++)
			
		} // for (int start = 0; start <= (n - size); start++)

		return base;
	}
	
}
