package br.ufsc.mov3lets.method.loader;

import br.ufsc.mov3lets.model.MAT;

/**
 * The Class CSVInternLoader.
 *
 * @param <T> the generic type
 */
public class CSVInternLoader<T extends MAT<?>> extends CSVLoader<T> implements InterningLoaderAdapter<T> {

}
