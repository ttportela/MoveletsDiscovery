package br.ufsc.mov3lets.method.feature.extraction.point;

import java.util.List;

import br.ufsc.mov3lets.method.feature.extraction.PointFeature;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;

public class LagPointFeature extends PointFeature {
	
	@Override
	public void init(Descriptor descriptor, AttributeDescriptor feature) {
		for (int i = 0; i < descriptor.getAttributes().size(); i++) {
			if (descriptor.getAttributes().get(i).getText().equalsIgnoreCase(feature.getText())) {
				index = i;
				break;
			}
		}
		feature.setText("lag_"+feature.getText());
		if (index < 0)
			throw new RuntimeException("[Lag Feature] Descriptor must have attibute with same name.");
	}

	public Aspect<?> calculate(Point p1) {
				
		return p1.getAspects().get(index);	
		
	}

	@Override
	public void fillPoints(MAT<?> trajectory) {
				
		List<Point> points = trajectory.getPoints();
		
		/* The first element is filled with the same value */
		points.get(0).getAspects().add(calculate(points.get(0)));		
				
		for (int i = 1; i < points.size(); i++) {			
			points.get(i).getAspects().add(calculate(points.get(i-1)));		
		}
		
	}

}
