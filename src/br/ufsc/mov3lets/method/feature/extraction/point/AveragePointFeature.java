package br.ufsc.mov3lets.method.feature.extraction.point;

import java.util.List;

import br.ufsc.mov3lets.method.feature.extraction.PointFeature;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;

public class AveragePointFeature extends PointFeature {
	
	protected int size = 3;

	@Override
	public void init(Descriptor descriptor, AttributeDescriptor feature) {
		for (int i = 0; i < descriptor.getAttributes().size(); i++) {
			if (descriptor.getAttributes().get(i).getText().equalsIgnoreCase(feature.getText())) {
				index = i;
				break;
			}
		}
		feature.setText("avg_"+feature.getText());
		if (index < 0)
			throw new RuntimeException("[Average Feature] Descriptor must have attibute with same name.");
		
		if (feature.hasOption("size"))
			this.size = Integer.valueOf(feature.getOptions().get("size"));
	}

	public double calculate(int i, List<Point> points) {
		int start = i-(size/2); // > points.size()-1? points.size()-1 : i+size;
		int end = i+(size/2); // > points.size()-1? points.size()-1 : i+size;
		
		if (points.size() < size) {
			start = 0;
			end = points.size() -1;
		} else {
			if (start < 0) {
				end += start * -1;
				start = 0;
			}
			if (end > points.size()-1) {
				start -= end - (points.size()-1);
				end = points.size()-1;
			}
		}
		
		double sum = 0.0; double ct = 0.0;
		for (int j = start; j <= end; j++) {
			Object value = points.get(j).getAspects().get(index).getValue();
			if (value != null) {
				sum += cast(value);
				ct += 1.0;
			}
		}
		
		return sum / ct;
	}
	
	protected double cast(Object a) {
		return a instanceof Integer? (double) (Integer) a : (double) a;
	}

	@Override
	public void fillPoints(MAT<?> trajectory) {
				
		List<Point> points = trajectory.getPoints();
		
		/* Filled with a rolling window */	
		for (int i = 0; i < points.size(); i++) {
			double average = calculate(i, points);		
			points.get(i).getAspects().add(new Aspect<Double>(average));		
		}
		
	}

}
