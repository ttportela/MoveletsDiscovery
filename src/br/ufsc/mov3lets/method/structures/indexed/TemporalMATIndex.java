package br.ufsc.mov3lets.method.structures.indexed;

import java.util.BitSet;

import br.ufsc.mov3lets.model.aspect.Aspect;

//------------------------------	
//Temporal ---------------------
//------------------------------
public class TemporalMATIndex extends MATIndex<Integer, Aspect<Integer>> {

	public int temporalThreshold;
	
	public TemporalMATIndex() {
//		super(aId);
//		this.temporalThreshold = temporalThreshold;
	}
	
	public TemporalMATIndex(int temporalThreshold) {
		this.temporalThreshold = temporalThreshold;
	}

//	public Map<Integer, BitSet> mTemporalIndex = new HashMap<Integer, BitSet>();
	
	public void addToIndex(Aspect<Integer> time, int rId) {

		Integer key = getCellPosition( time.getValue() );
		BitSet rIds = mIndex.get(key);

		if (rIds == null) {
			rIds = new BitSet();
			rIds.set(rId);
			mIndex.put(key, rIds);
		} else {
			rIds.set(rId);
			mIndex.replace(key, rIds);
		}
		
	}

	public Integer getCellPosition(Integer time) {
		return ((int) Math.floor((time-temporalThreshold) / temporalThreshold)) + 
			   ((int) Math.floor((time+temporalThreshold) / temporalThreshold));
	}
	
}
