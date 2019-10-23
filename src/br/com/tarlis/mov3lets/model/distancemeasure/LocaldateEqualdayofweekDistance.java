/**
 * 
 */
package br.com.tarlis.mov3lets.model.distancemeasure;

import java.time.LocalDate;

import br.com.tarlis.mov3lets.method.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.model.mat.aspect.Aspect;

/**
 * @author tarlis
 *
 */
public class LocaldateEqualdayofweekDistance extends DistanceMeasure<Aspect<LocalDate>> {

	@Override
	public double distance(Aspect<LocalDate> asp0, Aspect<LocalDate> asp1, AttributeDescriptor attr) {
		return (asp0.getValue().getDayOfWeek() == asp1.getValue().getDayOfWeek())? 0 : 1;
	}

}
