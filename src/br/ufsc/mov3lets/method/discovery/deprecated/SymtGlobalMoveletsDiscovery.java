/**
 * 
 */
package br.ufsc.mov3lets.method.discovery.deprecated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import br.ufsc.mov3lets.method.discovery.structures.GlobalDiscovery;
import br.ufsc.mov3lets.method.filter.MoveletsFilterRanker;
import br.ufsc.mov3lets.method.output.OutputterAdapter;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.MSubtrajectory;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.Subtrajectory;
import br.ufsc.mov3lets.model.aspect.Aspect;

/**
 * The Class HiperPivotsMoveletsDiscovery.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class SymtGlobalMoveletsDiscovery<MO> extends SymtMoveletsDiscovery<MO> implements GlobalDiscovery {

	protected int currentMaxSizeOfCandidates = 0;
	
	/** The used max number of combination of features. */
	protected int currentMaxCombinationOfFeatures = 0;
	
	protected Integer total_size = 0;
	
//	/**
//	 * Instantiates a new hiper pivots movelets discovery.
//	 *
//	 * @param trajsFromClass the trajs from class
//	 * @param data the data
//	 * @param train the train
//	 * @param test the test
//	 * @param qualityMeasure the quality measure
//	 * @param descriptor the descriptor
//	 */
//	public SymtMoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
//			QualityMeasure qualityMeasure, Descriptor descriptor) {
//		super(null, trajsFromClass, data, train, test, qualityMeasure, descriptor);
//	}
	
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
	public SymtGlobalMoveletsDiscovery(List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(null, data, train, test, qualityMeasure, descriptor);
	}

	
	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.discovery.SuperMoveletsDiscovery#discover().
	 * 
	 * @return
	 */
	public List<Subtrajectory> discover() {

		int maxSize = getDescriptor().getParamAsInt("max_size");
		int minSize = getDescriptor().getParamAsInt("min_size");

		List<Subtrajectory> movelets= null;// = new ArrayList<Subtrajectory>();
		
		// 1 - find all the unique values for each dimension in the dataset
		this.uniques = getDescriptor().hasParam("tau")? 
				uniqueValues(train, getDescriptor().getParamAsDouble("tau")) : uniqueValues(train);

		// Filter just by quality (not overlapping points):
		this.bestFilter = new MoveletsFilterRanker(0.0); 
		
		// This guarantees the reproducibility
		Random random = new Random(getDescriptor().hasParam("random_seed")? getDescriptor().getParamAsInt("random_seed") : 1);
		
		/** STEP 2.1: Starts at discovering movelets */
		List<MO> classes = train.stream().map(e -> (MO) e.getMovingObject()).distinct().collect(Collectors.toList());
		for (MO myclass : classes) {
			
			trajsFromClass = train.stream().filter(e-> myclass.equals(e.getMovingObject())).collect(Collectors.toList());
			
			trajectory = new MAT<MO>();
			trajectory.setMovingObject(myclass);
			trajectory.setTid(0);
			
			if (outputers != null)
				for (OutputterAdapter output : outputers) {
					output.setMovingObject(myclass.toString());
				}
			
//			for (MAT<MO> trajectory : trajsFromClass) {
				movelets = new ArrayList<Subtrajectory>();
				
				List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.train, minSize, maxSize, random);
				
				/** STEP 2.4: SELECTING BEST CANDIDATES */		
				movelets.addAll(this.bestFilter.filter(candidates));
				
				setStats("");
				
				/** STEP 2.2: Runs the pruning process */
				if(getDescriptor().getFlag("last_prunning"))
					movelets = lastPrunningFilter(movelets);

				/** STEP 2.2: ---------------------------- */
				outputMovelets(movelets);
				/** -------------------------------------- */
//			}
			
		}	
//		System.gc();
		
		return movelets;
	}
	
	/**
	 * Movelets discovery.
	 *
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param minSize the min size
	 * @param maxSize the max size
	 * @param random the random
	 * @return the list
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public List<Subtrajectory> moveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajectories, int minSize, int maxSize, Random random) {

//		int N_THREADS = getDescriptor().getParamAsInt("nthreads");
		ExecutorService executor = (ExecutorService) 
				Executors.newFixedThreadPool(getDescriptor().getParamAsInt("nthreads"));
		List<Future<Subtrajectory>> resultList = new ArrayList<>();
		
		List<Subtrajectory> candidates = new ArrayList<>();

		maxSize = maxSize(maxSize, minSize, this.maxSizeOfCandidates);
		if( minSize <= 1 ) minSize = 1;

		// It starts with the base case
		addStats("Class", trajectory.getMovingObject());
		addStats("Trajectory Size", maxSize);			

		int[][] combinations = addCombinations(1, 1);
		this.currentMaxSizeOfCandidates = 1; // Size 1

		/* STEP 1: --------------------------------- */
		for (int k = 0; k < combinations.length; k++) {
			
			for (Aspect<?> asp : uniques[combinations[k][0]]) {
				
				total_size += 1;
				
				// Build Fiction Points
				Point dullPoint = new Point();
				dullPoint.setTrajectory(trajectory);
				dullPoint.getAspects().add(asp);
				List<Point> points = new ArrayList<Point>();
				points.add(dullPoint);
				
				Subtrajectory subtrajectory =
					instantiateCandidate(0, 0, trajectory, trajectories, combinations[k], k, points);
				
				computeDistances(subtrajectory, trajectories);
				assesQuality(subtrajectory, random);
				
				if (subtrajectory.getQuality().getValue() > 0.0 && 2 <= maxSize) {
					
					resultList.add(executor.submit(new GrowPivot<MO>(this, subtrajectory, trajectory, trajectories, 2, maxSize, random)));
					
//					candidates.add(growPivot(subtrajectory, trajectory, trajectories, 2, maxSize, random));
				} else {
					candidates.add(subtrajectory);
				}
				
			}
		}
		
		/* STEP 2: --------------------------------- */
		for (Future<Subtrajectory> future : resultList) {
			try {
				candidates.add(future.get());
//				progressBar.update(progress++, train.size());
				Executors.newCachedThreadPool();
				System.gc();
			} catch (InterruptedException | ExecutionException e) {
				e.getCause().printStackTrace();
			}
		}

		addStats("Number of Candidates", total_size);
		
		candidates = this.bestFilter.filter(candidates);
		
		addStats("Total of Movelets", candidates.size());
		addStats("Max Size", this.currentMaxSizeOfCandidates);
		addStats("Used Features", this.currentMaxCombinationOfFeatures);
		
		progressBar.plus(this.trajsFromClass.size(), getStats());
				
		return candidates;
	}

	

//	protected Collection<Aspect>[] uniques;
	
//	protected Collection<Aspect>[] uniqueValues(List<MAT<MO>> trajectories) {
//		Map<Aspect, Integer>[] uniques = new HashMap[this.descriptor.getAttributes().size()];
//		for (int k = 0; k < this.descriptor.getAttributes().size(); k++) {
//			uniques[k] = new HashMap<Aspect, Integer>();
//			for (MAT<MO> T : trajectories) {
//				this.maxSizeOfCandidates = Math.max(this.maxSizeOfCandidates, T.getPoints().size());
//				for (Point p : T.getPoints()) {
//					uniques[k].put(p.getAspects().get(k), uniques[k].getOrDefault(p.getAspects().get(k), 0)+1);
//				}
//			}
//		}
//		
//		Set<Aspect>[] uniquesKeys = new Set[this.descriptor.getAttributes().size()];
//		int minCount = 2;
//		for (int k = 0; k < this.descriptor.getAttributes().size(); k++) {
//			uniquesKeys[k] = uniques[k].entrySet().stream()
//					.filter(a->a.getValue() >= minCount)
//					.map(Map.Entry::getKey)
//					.collect(Collectors.toSet());
//		}
//		
//		return uniquesKeys;
//	}
//	
//	protected Collection<Aspect>[] uniqueValues(List<MAT<MO>> trajectories, double tau) {
//		Map<Aspect, Integer>[] uniques = new HashMap[this.descriptor.getAttributes().size()];
//		for (int k = 0; k < this.descriptor.getAttributes().size(); k++) {
//			uniques[k] = new HashMap<Aspect, Integer>();
//			for (MAT<MO> T : trajectories) {
//				this.maxSizeOfCandidates = Math.max(this.maxSizeOfCandidates, T.getPoints().size());
//				for (Point p : T.getPoints()) {
//					uniques[k].put(p.getAspects().get(k), uniques[k].getOrDefault(p.getAspects().get(k), 0)+1);
//				}
//			}
//		}
//		
//		Set<Aspect>[] uniquesKeys = new Set[this.descriptor.getAttributes().size()];
//		int minCount = (int) (this.trajsFromClass.size() * tau);
//		for (int k = 0; k < this.descriptor.getAttributes().size(); k++) {
//			uniquesKeys[k] = uniques[k].entrySet().stream()
//					.filter(a->a.getValue() > minCount)
//					.map(Map.Entry::getKey)
//					.collect(Collectors.toSet());
//		}
//		
//		return uniquesKeys;
//	}
	
	/**
	 * Gets the distances.
	 *
	 * @param a the a
	 * @param b the b
	 * @param comb the comb
	 * @return the distances
	 */
	public double[] getDistances(Point a, Point b, int[] comb) {
		double[] distances = new double[comb.length];
		
		int i = 0;
		for (int j = 0; j < comb.length; j++) {
			int k = comb[j];
			AttributeDescriptor attr = descriptor.getAttributes().get(k);
			
			distances[i++] = 
					calculateDistance(
							a.getAspects().get(j), 
							b.getAspects().get(k), 
							attr
					); // This also normalize and enhance distances
		}
		
		return distances;
		
	}

}

class GrowPivot<MO> implements Callable<Subtrajectory> {
	
	private Subtrajectory subtrajectory;
	private MAT<MO> trajectory;
	private List<MAT<MO>> trajectories;
	private int i;
	private int maxSize;
	private Random random;
	
	private SymtGlobalMoveletsDiscovery<MO> father;

	public GrowPivot(SymtGlobalMoveletsDiscovery<MO> father, Subtrajectory subtrajectory, MAT<MO> trajectory, List<MAT<MO>> trajectories,
			int i, int maxSize, Random random) {
		this.father = father;
		this.subtrajectory = subtrajectory;
		this.trajectory = trajectory;
		this.trajectories = trajectories;
		this.i = i;
		this.maxSize = maxSize;
		this.random = random;
	}
	
	@Override
	public Subtrajectory call() throws Exception {
		return growPivot(subtrajectory, trajectory, trajectories, i, maxSize, random);
	}
	
	/**
	 * Grow pivots (recursively).
	 *
	 * @param candidatesOfSize the candidates of size
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param base the base
	 * @param newSize the new size
	 * @param size the size
	 * @param maxSize 
	 * @param random 
	 * @return the list
	 */
	public Subtrajectory growPivot(Subtrajectory candidate, MAT<MO> trajectory, List<MAT<MO>> trajectories, 
			int size, int maxSize, Random random) {
				
		Subtrajectory subtrajectoryOfSize = growSize(candidate, trajectory, trajectories, size, random);
		Subtrajectory subtrajectoryOfFeatures = growFeatures(candidate, trajectory, trajectories, random);

		subtrajectoryOfSize = subtrajectoryOfSize.best(subtrajectoryOfFeatures);
		
		if (subtrajectoryOfSize.equals(candidate))
			return candidate;
		else {
			if (size < maxSize) {
				father.currentMaxSizeOfCandidates = Integer.max(size+1, father.currentMaxSizeOfCandidates);
				return growPivot(subtrajectoryOfSize, trajectory, trajectories, size+1, maxSize, random);
			} else
				return subtrajectoryOfSize;
		}
	}

	/**
	 * Builds the new size.
	 *
	 * @param candidate the candidate
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param mdist the mdist
	 * @param size the size
	 * @param left the left
	 * @param trajectory_marks 
	 * @return the subtrajectory
	 */
	public Subtrajectory growSize(Subtrajectory candidate, MAT<MO> trajectory, 
			List<MAT<MO>> trajectories, int size, Random random) {
		
		int start = candidate.getStart();// - (left? 1 : 0);
		int end   = candidate.getEnd() + 1;//   + (left? 0 : 1);
		
		List<Point> points = copyPoints(candidate);
		// Build Fiction Points
		Point dullPoint = dullPoint(trajectory);
		points.add(dullPoint);
		
		return replicateByPoint(
				candidate,
				instantiateCandidate(start, end, trajectory, trajectories, candidate.getPointFeatures(), candidate.getK(), points), 
				0, trajectories, random);
	}

	protected Subtrajectory replicateByPoint(Subtrajectory candidate, Subtrajectory subtrajectory, int j, 
			List<MAT<MO>> trajectories, Random random) {
		
		if (j < subtrajectory.getPointFeatures().length) {
			
			Subtrajectory best = candidate;
			for (Aspect<?> asp : father.uniques[ subtrajectory.getPointFeatures()[j] ]) {
				List<Point> points = copyPoints(subtrajectory);
				// Build Fiction Points
				points.get(points.size()-1).getAspects().add(asp);
				
				Subtrajectory saux = instantiateCandidate(subtrajectory.getStart(), subtrajectory.getEnd(), 
						trajectory, trajectories, subtrajectory.getPointFeatures(), subtrajectory.getK(), points);
				
				best = replicateByPoint(candidate, saux, j+1, trajectories, random)
						.best(best);
			}
			return best;
			
		} else {
			father.total_size += 1;

			father.computeDistances(subtrajectory, trajectories);
			father.assesQuality(subtrajectory, random);
			
			return subtrajectory.best(candidate);
		}
		
	}


	/**
	 * Builds the new size.
	 *
	 * @param candidate the candidate
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param random the random method
	 * @return the subtrajectory
	 */
	public Subtrajectory growFeatures(Subtrajectory candidate, MAT<MO> trajectory, 
			List<MAT<MO>> trajectories, Random random) {
		int combSize = candidate.getPointFeatures().length+1;
		if (combSize > father.maxNumberOfFeatures)
			return candidate;
		
		if (combSize > father.currentMaxCombinationOfFeatures) {
			father.addCombinations(combSize, combSize);
		}
		
		ArrayList<Integer> validCombs = new ArrayList<Integer>();
		for (int i = 0; i < father.combinations.length; i++) {
			if (father.combinations[i].length == combSize && 
				// combination starts with:
				Arrays.equals(candidate.getPointFeatures(), Arrays.copyOfRange(father.combinations[i], 0, combSize-1))) {
				validCombs.add(i);
			}
		}
		
		Subtrajectory best = candidate;
		
		for (int k : validCombs) {
			
			Subtrajectory subtrajectory = instantiateCandidate(candidate.getStart(), candidate.getEnd(), 
					trajectory, trajectories, father.combinations[k], k, copyPoints(candidate));
			
			best = replicateByFeature(candidate, subtrajectory, 0, trajectories, random)
					.best(best);
			
		}
		
		return best;
	}
	
	protected Subtrajectory replicateByFeature(Subtrajectory candidate, Subtrajectory subtrajectory, int i, 
			List<MAT<MO>> trajectories, Random random) {
		
		if (i < subtrajectory.getPoints().size()) {

			int x = father.combinations[subtrajectory.getK()].length-1;
			int feature = father.combinations[subtrajectory.getK()][x];
			
			Subtrajectory best = candidate;
			for (Aspect<?> asp : father.uniques[ feature ]) {
				List<Point> points = copyPoints(subtrajectory);
				// Build Fiction Points
				points.get(i).getAspects().add(asp);
				
				Subtrajectory saux = instantiateCandidate(subtrajectory.getStart(), subtrajectory.getEnd(), 
						trajectory, trajectories, subtrajectory.getPointFeatures(), subtrajectory.getK(), points);
				
				best = replicateByFeature(candidate, saux, i+1, trajectories, random)
						.best(best);
			}
			return best;
			
		} else {
			father.total_size += 1;

			father.computeDistances(subtrajectory, trajectories);
			father.assesQuality(subtrajectory, random);
			
			return subtrajectory.best(candidate);
		}
		
	}

	protected List<Point> copyPoints(Subtrajectory candidate) {
		List<Point> points = new ArrayList<Point>();
		for (Point p: candidate.getPoints()) {
			points.add(p.copy());
		}
		return points;
	}

	protected Point dullPoint(MAT<MO> trajectory) {
		Point dullPoint = new Point();
		dullPoint.setTrajectory(trajectory);
		return dullPoint;
	}

	protected MSubtrajectory instantiateCandidate(int start, int end, MAT<MO> trajectory, List<MAT<MO>> trajectories,
			int[] pointFeatures, int k, List<Point> points) {
		return new MSubtrajectory(start, end, 
				trajectory, trajectories.size(), pointFeatures, k, points);
	}
	
}
