package br.ufsc.mov3lets.method.feature;

import java.util.List;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;

public class TraveleddistancePointFeature extends PointFeature {
	
	protected int indexSpace = -1;
	protected AttributeDescriptor spaceComparator;

	@Override
	public void init(Descriptor descriptor) {
		for (int i = 0; i < descriptor.getAttributes().size(); i++) {
			if (descriptor.getAttributes().get(i).getType().equalsIgnoreCase("space2d") ||
				descriptor.getAttributes().get(i).getType().equalsIgnoreCase("composit_space2d")) {
				indexSpace = i;
				spaceComparator = descriptor.getAttributes().get(i);
			}
		}
		if (indexSpace < 0)
			throw new RuntimeException("[Traveleddistance Feature] Descriptor must have space like for attibute type.");
	}

	public double calculate(Point p1, Point p2) {
		
		return spaceComparator.getDistanceComparator().calculateDistance(
				p1.getAspects().get(indexSpace), 
				p2.getAspects().get(indexSpace), 
				spaceComparator);	
		
	}

	@Override
	public void fillPoints(MAT<?> trajectory) {
				
		List<Point> points = trajectory.getPoints();
		
		/* The first and last elements were not filled because they have no previous
		 * values to calculate sinuosity, so its value is zero */
		points.get(0).getAspects().add(new Aspect<Double>(0.0));	
				
		for (int i = 1; i < points.size(); i++) {
			double traveledDistance = calculate(points.get(i-1), points.get(i));		
			points.get(i).getAspects().add(new Aspect<Double>(traveledDistance));		
		}
		
	}

}
