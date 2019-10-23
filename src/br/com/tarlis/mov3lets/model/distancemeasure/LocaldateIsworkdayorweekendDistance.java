/**
 * 
 */
package br.com.tarlis.mov3lets.model.distancemeasure;

import java.time.DayOfWeek;
import java.time.LocalDate;

import br.com.tarlis.mov3lets.method.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.model.mat.aspect.Aspect;

/**
 * @author tarlis
 *
 */
public class LocaldateIsworkdayorweekendDistance extends LocaldateEqualdayofweekDistance {
	
	@Override
	public double distance(Aspect<LocalDate> asp0, Aspect<LocalDate> asp1, AttributeDescriptor attr) {
		if (isWeekend(asp0.getValue()) && isWeekend(asp1.getValue()))
			return 0;
		else if (!isWeekend(asp0.getValue()) && !isWeekend(asp1.getValue()))
			return 0;
		else
			// Both days are not weekdays or weekend
			return 1;
	}
	
	public boolean isWeekend(LocalDate localDate){
		// considera que os dias vao de 1 (Monday) ate 7 (Sunday)
		// Entao se o dia é maior ou igual a 6 (Satuday) 
		return (localDate.getDayOfWeek().getValue() >= DayOfWeek.SATURDAY.getValue());
	}

}
