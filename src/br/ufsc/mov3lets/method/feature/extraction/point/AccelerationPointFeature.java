package br.ufsc.mov3lets.method.feature.extraction.point;

import java.util.List;

import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;

public class AccelerationPointFeature extends SpeedPointFeature {
		
	public double calculateAcceleration(Point p1, Point p2, double speedp1, double speedp2){
		
		double differenceSpeed = speedp2 - speedp1;
				
		double differenceTime = timeComparator.getDistanceComparator().calculateDistance(
				p2.getAspects().get(indexTime), 
				p1.getAspects().get(indexTime), 
				timeComparator);
				
		return (differenceTime != 0) ? differenceSpeed / differenceTime : 0;						
		
	}

	@Override
	public void fillPoints(MAT<?> trajectory) {
				
		List<Point> points = trajectory.getPoints();
		
		/* The first element was not filled because it has not previous
		 * values to calculate acceleration, so its value is zero */
		points.get(0).getAspects().add(new Aspect<Double>(0.0));		
				
		double speedp1 = 0.0;
		for (int i = 1; i < points.size(); i++) {
			double speedp2 = super.calculate(points.get(i-1), points.get(i));		
			double acceleration = calculateAcceleration(points.get(i-1), points.get(i), speedp1, speedp2);
			points.get(i).getAspects().add(new Aspect<Double>(acceleration));
			
			speedp1 = speedp2;
		}
		
	}

}
