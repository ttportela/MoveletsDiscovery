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
public class LocaldateIsworkdayDistance extends LocaldateIsworkdayorweekendDistance {
	
	@Override
	public double distance(Aspect<LocalDate> asp0, Aspect<LocalDate> asp1, AttributeDescriptor attr) {
		return (isWeekend(asp0.getValue()) && isWeekend(asp1.getValue()))? 1 : 0;
	}

}
