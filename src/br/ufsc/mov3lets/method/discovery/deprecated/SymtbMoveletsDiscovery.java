/**
 * 
 */
package br.ufsc.mov3lets.method.discovery.deprecated;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import br.ufsc.mov3lets.method.discovery.structures.ClassDiscovery;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;

/**
 * The Class HiperPivotsMoveletsDiscovery.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class SymtbMoveletsDiscovery<MO> extends SymtMoveletsDiscovery<MO> implements ClassDiscovery {

	protected int currentMaxSizeOfCandidates = 0;
	
	/**
	 * Instantiates a new hiper pivots movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor the descriptor
	 */
	public SymtbMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajsFromClass, data, train, test, qualityMeasure, descriptor);
	}
	
	protected Collection<Aspect>[] uniqueValues(List<MAT<MO>> trajectories) {
		return uniqueValues(trajectories, this.trajsFromClass.size() * 0.25);
	}
	
	protected Collection<Aspect>[] uniqueValues(List<MAT<MO>> trajectories, double tau) {
		int minCount = (int) (this.trajsFromClass.size() * tau); // Percentual presence in trajectories
		return uniqueValues(trajectories, minCount);
	}
	
	protected Collection<Aspect>[] uniqueValues(List<MAT<MO>> trajectories, int minCount) {
		// Count presence in trajectories:
		Map<Aspect, BitSet>[] uniques = new HashMap[this.descriptor.getAttributes().size()];
		for (int k = 0; k < this.descriptor.getAttributes().size(); k++) {
			uniques[k] = new HashMap<Aspect, BitSet>();
			for (MAT<MO> T : trajectories) {
				this.maxSizeOfCandidates = Math.max(this.maxSizeOfCandidates, T.getPoints().size());
				for (Point p : T.getPoints()) {
					BitSet bs = uniques[k].getOrDefault(p.getAspects().get(k), new BitSet());
					bs.set(T.getTid());
					uniques[k].put(p.getAspects().get(k), bs);
				}
			}
		}
		
		Set<Aspect>[] uniquesKeys = new Set[this.descriptor.getAttributes().size()];
		for (int k = 0; k < this.descriptor.getAttributes().size(); k++) {
			uniquesKeys[k] = uniques[k].entrySet().stream()
					.filter(a->a.getValue().cardinality() >= minCount || a.getValue().cardinality() <= minCount)
					.map(Map.Entry::getKey)
					.collect(Collectors.toSet());
		}
		
		return uniquesKeys;
	}

}
