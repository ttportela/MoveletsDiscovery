package br.ufsc.mov3lets.method.structures.indexed;

import java.util.BitSet;

import br.ufsc.mov3lets.model.aspect.Aspect;

//------------------------------		
//Semantics --------------------
//------------------------------
public class SemanticMATIndex extends MATIndex<String, Aspect<?>> {
	
//	public final static String SEPARATOR=",";
	
	public String attribute;

	public SemanticMATIndex(String attribute) {
//		super(aId);
		this.attribute = attribute;
	}

//	public static Map<String, BitSet> mSemDictionary = new HashMap<String, BitSet>();
	
//	public static void addToDictionary(String semCompositeKey, int rId) {
//
//		BitSet rIds = mSemDictionary.get(semCompositeKey);
//
//		if (rIds == null) {
//			rIds = new BitSet();
//			rIds.set(rId);
//			mSemDictionary.put(semCompositeKey, rIds);
//		} else {
//			rIds.set(rId);
//			mSemDictionary.replace(semCompositeKey, rIds);
//		}
//
//	}

	public void addToIndex(Aspect<?> value, int rId) {
//		int i=3;
//		for (String attribute : attributes) {
			BitSet rIds = mIndex.get(value.getValue().toString());

			if (rIds == null) {
				rIds = new BitSet();
				rIds.set(rId);
				mIndex.put(value.getValue().toString(), rIds);
			} else {
				rIds.set(rId);
				mIndex.replace(value.getValue().toString(), rIds);
			}
//			++i;
//		}

	}
	
}
