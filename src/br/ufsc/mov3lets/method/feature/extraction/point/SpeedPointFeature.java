package br.ufsc.mov3lets.method.feature.extraction.point;

import java.util.List;

import br.ufsc.mov3lets.method.feature.extraction.PointFeature;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;

public class SpeedPointFeature extends PointFeature {
	
	protected int indexTime = -1;
	protected AttributeDescriptor spaceComparator;
	protected AttributeDescriptor timeComparator;

	@Override
	public void init(Descriptor descriptor, AttributeDescriptor feature) {
		for (int i = 0; i < descriptor.getAttributes().size(); i++) {
			if (descriptor.getAttributes().get(i).getType().equalsIgnoreCase("space2d") ||
				descriptor.getAttributes().get(i).getType().equalsIgnoreCase("composit_space2d")) {
				index = i;
				spaceComparator = descriptor.getAttributes().get(i);
			} else if (descriptor.getAttributes().get(i).getType().equalsIgnoreCase("time")) {
				indexTime = i;
				timeComparator = descriptor.getAttributes().get(i);
			}
		}
		if (index < 0 || indexTime < 0)
			throw new RuntimeException("[Speed Feature] Descriptor must have space and time like for attibute types.");
	}

	public double calculate(Point p1, Point p2) {
		
		double distanceSpace = spaceComparator.getDistanceComparator().calculateDistance(
				p1.getAspects().get(index), 
				p2.getAspects().get(index), 
				spaceComparator);
				
		double distanceTime = timeComparator.getDistanceComparator().calculateDistance(
				p1.getAspects().get(indexTime), 
				p2.getAspects().get(indexTime), 
				timeComparator);
				
		return (distanceTime == 0) ? 0 : distanceSpace / distanceTime;		
		
	}

	@Override
	public void fillPoints(MAT<?> trajectory) {
				
		List<Point> points = trajectory.getPoints();
		
		/* The first element was not filled because it has not previous
		 * values to calculate speed, so its value is zero */
		points.get(0).getAspects().add(new Aspect<Double>(0.0));		
				
		for (int i = 1; i < points.size(); i++) {
			double speed = calculate(points.get(i-1), points.get(i));			
			points.get(i).getAspects().add(new Aspect<Double>(speed));		
		}
		
	}
	
	public int getIndexSpace() {
		return index;
	}
	
	public void setIndexSpace(int index) {
		super.index = index;
	}
	
	public int getIndexTime() {
		return indexTime;
	}
	
	public void setIndexTime(int indexTime) {
		this.indexTime = indexTime;
	}

}
