package br.ufsc.mov3lets.method.loader;

import br.ufsc.mov3lets.model.MAT;

/**
 * The Class ZippedInternLoader.
 *
 * @param <T> the generic type
 */
public class CZIPInternLoader<T extends MAT<?>> extends CZIPLoader<T> implements InterningLoaderAdapter<T> {

}
