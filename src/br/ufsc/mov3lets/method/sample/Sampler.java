/**
 * 
 */
package br.ufsc.mov3lets.method.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.ufsc.mov3lets.model.MAT;

/**
 * Stratified Random Sampler.
 * 
 * Adapted from the UEA Time Series Machine Learning (TSML) toolbox.
 * 
 * @author tarlis
 *
 */
public abstract class Sampler<MO> {
	
	protected List<MAT<MO>> all;

//	protected TreeMap<MO, Integer> trainDistribution;
	protected Map<MO, List<MAT<MO>>> classBins;
	
	protected int sample = 0;
	
//	protected Integer trainSize, testSize;
	
	public Sampler(List<MAT<MO>> train, List<MAT<MO>> test) {
//		trainSize = train.size();
//		testSize  = test.size();
		if (test != null && !test.isEmpty())
			this.all = Stream.concat(train.stream(), test.stream()).collect(Collectors.toList());
		else
			this.all = train;
		
//		trainDistribution = createClassDistribution(train);
		classBins = createClassBinMap(all);
	}

	public TreeMap<MO, Integer> createClassDistribution(List<MAT<MO>> trajectories) {
		TreeMap<MO, Integer> classDistribution = new TreeMap<MO, Integer>();
        MO classValue;
        for (MAT<MO> T : trajectories) {
            classValue = T.getMovingObject();
            Integer val = classDistribution.getOrDefault(T.getMovingObject(), 0) + 1;
            classDistribution.put(classValue, val);
        }
        return classDistribution;
	}
	
	public Map<MO, List<MAT<MO>>> createClassBinMap(List<MAT<MO>> trajectories) {
		Map<MO, List<MAT<MO>>> instancesMap = new TreeMap<>();
        
		MO classValue;
        for (MAT<MO> T : trajectories) {
            classValue = T.getMovingObject();

            List<MAT<MO>> val = instancesMap.get(classValue);
            if(val == null)
                val = new ArrayList<MAT<MO>>();
            val.add(T);
            instancesMap.put(classValue, val);
        }
        
        return instancesMap;
	}
	
	public abstract List<MAT<MO>>[] nextSample();
	
	public int getSample() {
		return sample;
	}
}
