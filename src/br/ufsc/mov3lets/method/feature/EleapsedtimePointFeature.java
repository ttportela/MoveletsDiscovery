package br.ufsc.mov3lets.method.feature;

import java.util.List;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;

public class EleapsedtimePointFeature extends PointFeature {
	
	protected int indexTime = -1;
	protected AttributeDescriptor timeComparator;

	@Override
	public void init(Descriptor descriptor) {
		for (int i = 0; i < descriptor.getAttributes().size(); i++) {
			if (descriptor.getAttributes().get(i).getType().equalsIgnoreCase("time")) {
				indexTime = i;
				timeComparator = descriptor.getAttributes().get(i);
			}
		}
		if (indexTime < 0)
			throw new RuntimeException("[Elapsedtime Feature] Descriptor must have time like for attibute type.");
	}

	public double calculate(Point p1, Point p2) {
				
		return timeComparator.getDistanceComparator().calculateDistance(
				p1.getAspects().get(indexTime), 
				p2.getAspects().get(indexTime), 
				timeComparator);	
		
	}

	@Override
	public void fillPoints(MAT<?> trajectory) {
				
		List<Point> points = trajectory.getPoints();
		
		/* The first element was not filled because it has not previous
		 * values to calculate eleapsed time, so its value is zero */
		double elapsedtime = 0.0;
		points.get(0).getAspects().add(new Aspect<Double>(elapsedtime));		
				
		for (int i = 1; i < points.size(); i++) {
			elapsedtime += calculate(points.get(i-1), points.get(i));			
			points.get(i).getAspects().add(new Aspect<Double>(elapsedtime));		
		}
		
	}

}
