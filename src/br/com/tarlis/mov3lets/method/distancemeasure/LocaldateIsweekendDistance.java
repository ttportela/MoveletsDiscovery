/**
 * 
 */
package br.com.tarlis.mov3lets.method.distancemeasure;

import java.time.LocalDate;

import br.com.tarlis.mov3lets.method.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.model.aspect.Aspect;

/**
 * @author tarlis
 *
 */
public class LocaldateIsweekendDistance extends LocaldateIsworkdayorweekendDistance {

	@Override
	public double distance(Aspect<LocalDate> asp0, Aspect<LocalDate> asp1, AttributeDescriptor attr) {
		return (isWeekend(asp0.getValue()) && isWeekend(asp1.getValue()))? 0 : 1;
	}
	
}
