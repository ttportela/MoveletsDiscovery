/**
 * 
 */
package br.com.tarlis.mov3lets.model.distancemeasure;

import java.time.LocalDate;

import br.com.tarlis.mov3lets.model.mat.aspect.Aspect;
import br.com.tarlis.mov3lets.view.AttributeDescriptor;

/**
 * @author tarlis
 *
 */
public class LocaldateDiffdaysofweekDistance extends LocaldateDifferenceDistance {
	
	/** 
	 * Not DIFFERENCE
	 */
	@Override
	public double distance(Aspect<LocalDate> asp0, Aspect<LocalDate> asp1, AttributeDescriptor attr) {
		return Math.abs(asp0.getValue().getDayOfWeek().getValue() - asp1.getValue().getDayOfWeek().getValue());
	}

}
