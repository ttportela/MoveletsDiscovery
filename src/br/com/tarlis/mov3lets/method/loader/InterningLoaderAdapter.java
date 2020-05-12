/**
 * 
 */
package br.com.tarlis.mov3lets.method.loader;

import java.util.HashMap;

import org.apache.commons.math3.util.Pair;

import br.com.tarlis.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.aspect.Aspect;

/**
 * @author tarlisportela
 *
 */
public interface InterningLoaderAdapter<T extends MAT<?>> extends LoaderAdapter<T> {
	
	HashMap<Pair<String, String>, Aspect<?>> MEM = new HashMap<Pair<String,String>, Aspect<?>>();
	
	public default Aspect<?> instantiateAspect(AttributeDescriptor attr, String value) {
		
		Pair<String, String> key = new Pair<String, String>(attr.getText(), value.intern());
		
		if (MEM.containsKey(key))
			return MEM.get(key);
		else {
			Aspect<?> asp = LoaderAdapter.super.instantiateAspect(attr, key.getValue());
			MEM.put(key, asp);
			return asp;
		}
	}
	
	/**
	 * @return the mEM
	 */
	public default HashMap<Pair<String, String>, Aspect<?>> getMEM() {
		return MEM;
	}
	
//	/**
//	 * @param mEM the mEM to set
//	 */
//	public default void setMEM(HashMap<Pair<String, String>, Aspect<?>> mEM) {
//		MEM = mEM;
//	}

}
