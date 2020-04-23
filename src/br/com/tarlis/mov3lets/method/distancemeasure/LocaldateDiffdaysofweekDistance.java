/**
 * 
 */
package br.com.tarlis.mov3lets.method.distancemeasure;

import java.time.LocalDate;

import br.com.tarlis.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.model.aspect.Aspect;

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
