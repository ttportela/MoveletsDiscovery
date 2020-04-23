/**
 * 
 */
package br.com.tarlis.mov3lets.method.distancemeasure;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import br.com.tarlis.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.model.aspect.Aspect;

/**
 * @author tarlis
 *
 */
public class LocaltimeDifferenceDistance extends DistanceMeasure<Aspect<LocalTime>> {

	@Override
	public double distance(Aspect<LocalTime> asp0, Aspect<LocalTime> asp1, AttributeDescriptor attr) {
		return ChronoUnit.MILLIS.between(asp0.getValue(), asp1.getValue()) / 1000;
	}

}
