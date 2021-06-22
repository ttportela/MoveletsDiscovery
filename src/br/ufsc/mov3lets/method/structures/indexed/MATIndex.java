package br.ufsc.mov3lets.method.structures.indexed;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import br.ufsc.mov3lets.model.aspect.Aspect;

public abstract class MATIndex<T extends Object, K extends Aspect> {
	
//	public int aId;
//	
//	public MATIndex(int aId) {
//		this.aId = aId;
//	}
	
	public final static String SEPARATOR=",";
	
	public Map<T, BitSet> mIndex = new HashMap<T, BitSet>();
	
	public abstract void addToIndex(K value, int rId);
}
