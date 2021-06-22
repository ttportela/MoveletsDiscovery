package br.ufsc.mov3lets.method.feature.extraction.point;

import java.util.List;

import br.ufsc.mov3lets.method.feature.extraction.PointFeature;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;

public class StaytimePointFeature extends PointFeature {
	
	protected AttributeDescriptor timeComparator;

	@Override
	public void init(Descriptor descriptor, AttributeDescriptor feature) {
		for (int i = 0; i < descriptor.getAttributes().size(); i++) {
			if (descriptor.getAttributes().get(i).getType().equalsIgnoreCase("time")) {
				index = i;
				timeComparator = descriptor.getAttributes().get(i);
			}
		}
		if (index < 0)
			throw new RuntimeException("[Staytime Feature] Descriptor must have time like for attibute type.");
	}

	public double calculate(Point p1, Point p2) {
				
		double diffTime = timeComparator.getDistanceComparator().calculateDistance(
				p1.getAspects().get(index), 
				p2.getAspects().get(index), 
				timeComparator);	

		return diffTime / 2.0;
	}

	@Override
	public void fillPoints(MAT<?> trajectory) {
				
		List<Point> points = trajectory.getPoints();
		
		/* According to Patel we have 3 options to measure the weight wi for a point pi
		 * wi = (t2-t1), i = 1 (first)
		 * wi = (tn-tn-1), i = N (last)
		 * wi = (ti-ti+1), otherwise
		 * */
		int n = points.size();
		double staytimeFirst = calculate(points.get(0), points.get(1));
		double staytimeLast  = calculate(points.get(n-1), points.get(n-2));
		points.get(0).getAspects().add(new Aspect<Double>(staytimeFirst));
		points.get(n-1).getAspects().add(new Aspect<Double>(staytimeLast));		
		
		for (int i = 1; i < (points.size()-1); i++) {
			double staytime = calculate(points.get(i-1), points.get(i+1));			
			points.get(i).getAspects().add(new Aspect<Double>(staytime));
		}
		
	}

}
