/**
 * 
 */
package br.com.tarlis.mov3lets.model.distancemeasure;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import br.com.tarlis.mov3lets.model.mat.aspect.Aspect;
import br.com.tarlis.mov3lets.view.AttributeDescriptor;

/**
 * @author tarlis
 *
 */
public class LocaltimeDifferenceDistance extends DistanceInterface<Aspect<LocalTime>> {

	@Override
	public double distance(Aspect<LocalTime> asp0, Aspect<LocalTime> asp1, AttributeDescriptor attr) {
		return ChronoUnit.MILLIS.between(asp0.getValue(), asp1.getValue()) / 1000;
	}

}
