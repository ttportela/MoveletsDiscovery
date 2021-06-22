package br.ufsc.mov3lets.method.feature.extraction.point;

import java.util.List;

import br.ufsc.mov3lets.method.feature.extraction.PointFeature;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;
import br.ufsc.mov3lets.model.aspect.Space2DAspect;

public class TurnanglePointFeature extends PointFeature {
	
	protected AttributeDescriptor spaceComparator;

	@Override
	public void init(Descriptor descriptor, AttributeDescriptor feature) {
		for (int i = 0; i < descriptor.getAttributes().size(); i++) {
			if (descriptor.getAttributes().get(i).getType().equalsIgnoreCase("space2d") ||
				descriptor.getAttributes().get(i).getType().equalsIgnoreCase("composit_space2d")) {
				index = i;
				spaceComparator = descriptor.getAttributes().get(i);
			}
		}
		if (index < 0)
			throw new RuntimeException("[Turnangle Feature] Descriptor must have space like for attibute type.");
	}

	public double calculateAngle(Point p1, Point p2){
		
		Space2DAspect p1Space = ((Space2DAspect) p1.getAspects().get(index));
		Space2DAspect p2Space = ((Space2DAspect) p2.getAspects().get(index));
		
		double diffX = p2Space.getX() - p1Space.getX();  
		double diffY = p2Space.getY() - p1Space.getY();
		
		double angle = Math.toDegrees(Math.atan2(diffY,diffX));		
		
		return angle;		
		
	}

	public double calculate(Point p1, Point p2, Point p3){
		
		double angle1 = calculateAngle(p1, p2);
		double angle2 = calculateAngle(p2, p3);
		double diff = angle2 - angle1;
		
		if (diff >  180) return diff - 360;
		if (diff < -180) return diff + 360;				
		return diff;		
		
	}

	@Override
	public void fillPoints(MAT<?> trajectory) {
				
		List<Point> points = trajectory.getPoints();
		
		/* The first and last elements were not filled because they have no previous
		 * values to calculate sinuosity, so its value is zero */
		points.get(0).getAspects().add(new Aspect<Double>(0.0));	
		points.get(points.size()-1).getAspects().add(new Aspect<Double>(0.0));
				
		for (int i = 1; i < points.size()-1; i++) {
			double turnangle = calculate(points.get(i-1), points.get(i), points.get(i+1));			
			points.get(i).getAspects().add(new Aspect<Double>(turnangle));		
		}
		
	}

}
