/**
 * 
 */
package br.ufsc.mov3lets.method;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.ufsc.mov3lets.method.output.SimilarityMatrixOutputter;
import br.ufsc.mov3lets.method.similaritymeasure.TrajectorySimilarityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.utils.log.ProgressBar;

/**
 * @author tarlisportela
 *
 */
public class Similarity<MO> extends Mov3lets<MO> {

	public Similarity(String descriptorFile, Map<String, Object> params) 
			throws UnsupportedEncodingException, FileNotFoundException {
		super(descriptorFile, params);
	}

	public Similarity(Map<String, Object> params) {
		super(params);
	}

	/**
	 * Trajectory Similarity.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public void similarity() throws Exception {
		int N_THREADS = getDescriptor().getParamAsInt("nthreads");

		// STEP 1 - Load Trajectories: is done before this method starts.
		data = Collections.unmodifiableList(
						Stream.concat(train.stream(), test.stream()).collect(Collectors.toList())
					);

		String[] measures = getDescriptor().getParamAsText("similarity").split(",");

		/* Keeping up with Progress output */
		progressBar.setPrefix("[2] >> Trajectory Similarity");
		progressBar.setTotal(data.size() * measures.length);
		int progress = 0;
		progressBar.update(progress, data.size() * measures.length);
		
		scheduleIfTimeContractable();
		
		List<TrajectorySimilarityMeasure> ls = instantiate(measures);	

		if (N_THREADS > 1) {
			ExecutorService executor = (ExecutorService) 
					Executors.newFixedThreadPool(N_THREADS);
			List<Future<Integer>> resultList = new ArrayList<>();

			/** STEP 2.1: */
			for (TrajectorySimilarityMeasure sim : ls) {
				resultList.add(executor.submit(sim));
			}
			ls = null;
			
			/** STEP 2.1: --------------------------------- */
			for (Future<Integer> future : resultList) {
				try {
					future.get();
//					progressBar.update(progress++, train.size());
					Executors.newCachedThreadPool();
					System.gc();
				} catch (InterruptedException | ExecutionException e) {
					e.getCause().printStackTrace();
				}
			}
			executor.shutdown();
		} else {
			
			/** STEP 2.1: */
			Iterator<TrajectorySimilarityMeasure> i = ls.iterator();
			ls = null;
			while (i.hasNext()) {
				TrajectorySimilarityMeasure moveletsDiscovery = i.next();
				moveletsDiscovery.call();
				i.remove();
				System.gc();
			}
			
		}
		
		stopIfTimeContractable();
	}

	protected List<TrajectorySimilarityMeasure> instantiate(String[] measures) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		List<TrajectorySimilarityMeasure> ls = new ArrayList<TrajectorySimilarityMeasure>();
		
		for (String m : measures) {
			Class c = Class.forName("br.ufsc.mov3lets.method.similaritymeasure." + 
					m + "TrajectorySimilarity");
			TrajectorySimilarityMeasure sim = (TrajectorySimilarityMeasure) 
					c.getConstructor(List.class, Double.class, Descriptor.class, ProgressBar.class, SimilarityMatrixOutputter.class)
					.newInstance(data, 0.0, getDescriptor(), progressBar,
							new SimilarityMatrixOutputter(descriptor.getParamAsText("respath"), m));
			ls.add(sim);
		}
		return ls;
	}

}
