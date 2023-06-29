/**
 * 
 */
package br.ufsc.mov3lets.method.discovery.deprecated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import br.ufsc.mov3lets.method.discovery.UltraMoveletsDiscovery;
import br.ufsc.mov3lets.method.discovery.structures.ClassDiscovery;
import br.ufsc.mov3lets.method.filter.MoveletsFilterRanker;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.Subtrajectory;
import br.ufsc.mov3lets.model.aspect.Aspect;

/**
 * The Class HiperPivotsMoveletsDiscovery.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class Symt1MoveletsDiscovery<MO> extends UltraMoveletsDiscovery<MO> implements ClassDiscovery {

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
	public Symt1MoveletsDiscovery(List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(null, trajsFromClass, data, train, test, qualityMeasure, descriptor);
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

		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();

		trajectory = new MAT<MO>();
		trajectory.setMovingObject(this.trajsFromClass.get(0).getMovingObject());
		trajectory.setTid(0);
		
		// 1 - find all the unique values for each dimension in the dataset
		this.uniques = getDescriptor().hasParam("tau")? 
				uniqueValues(getDescriptor().getParamAsDouble("tau")) : uniqueValues();

		// Filter just by quality (not overlapping points):
		this.bestFilter = new MoveletsFilterRanker(0.0); 
		
		// This guarantees the reproducibility
		Random random = new Random(getDescriptor().hasParam("random_seed")? getDescriptor().getParamAsInt("random_seed") : 1);
		
		/** STEP 2.1: Starts at discovering movelets */
		List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.train, minSize, maxSize, random);
		
		/** STEP 2.4: SELECTING BEST CANDIDATES */		
		movelets.addAll(this.bestFilter.filter(candidates));
		
		setStats("");
		
		/** STEP 2.2: Runs the pruning process */
		if(getDescriptor().getFlag("last_pruning"))
			movelets = lastPrunningFilter(movelets);

		/** STEP 2.2: ---------------------------- */
		outputMovelets(movelets);
		/** -------------------------------------- */	
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
	 */
	public List<Subtrajectory> moveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajectories, int minSize, int maxSize, Random random) {
		List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();
		
//		int n = this.maxSizeOfCandidates; //trajectory.getPoints().size();

//		minSize = minSize(minSize, n);
//		maxSize = maxSize(maxSize, minSize, n);
		maxSize = maxSize(maxSize, minSize, this.maxSizeOfCandidates);
		
		if( minSize <= 1 ) minSize = 1;

		// It starts with the base case
		addStats("Class", trajectory.getMovingObject()); 
//		addStats("Trajectory", trajectory.getTid());
		addStats("Trajectory Size", maxSize);			

		List<Subtrajectory> candidatesOfSize = findPivotCandidates(trajectory, trajectories, 1);
//		computeQuality(candidatesOfSize, random, trajectory);
//		calculateProportion(candidatesOfSize, random);
		for (Subtrajectory subtrajectory : candidatesOfSize) {
//			computeDistances(subtrajectory, trajectories);
			assesQuality(subtrajectory, random);
		}
		total_size += candidatesOfSize.size();
		
		candidatesOfSize = this.bestFilter.filter(candidatesOfSize);
		addStats("Pivot Candidates", candidatesOfSize.size());
		for(Subtrajectory candidate : candidatesOfSize) {
			candidates.add(growPivot(candidate, trajectory, trajectories, minSize+1, maxSize, random, null));
		}

		addStats("Number of Candidates", total_size);
//		addStats("Selected Candidates", candidates.size());
		
		candidates = this.bestFilter.filter(candidates);
		
		addStats("Total of Movelets", candidates.size());
		addStats("Max Size", this.currentMaxSizeOfCandidates);
		addStats("Used Features", this.currentMaxCombinationOfFeatures);
		
		progressBar.plus(this.trajsFromClass.size(), getStats());
				
		return candidates;
	}
	
	/**
	 * [THE GREAT GAP].
	 *
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param size [IGNORED]
	 * @param mdist the mdist
	 * @return the list
	 */
	public List<Subtrajectory> findPivotCandidates(MAT<MO> trajectory, List<MAT<MO>> trajectories, int size) {
		
		int[][] combinations = addCombinations(1, 1);

		this.currentMaxSizeOfCandidates = size; // Size 1
		
		List<Subtrajectory> candidates = new ArrayList<>();
		
		for (int k = 0; k < combinations.length; k++) {
			
			for (Aspect<?> asp : uniques[combinations[k][0]]) {
				// Build Fiction Points
				Point dullPoint = dullPoint(trajectory);
				dullPoint.getAspects().add(asp);
				List<Point> points = new ArrayList<Point>();
				points.add(dullPoint);
				
				candidates.add(
					instantiateCandidate(0, 0, trajectory, trajectories, combinations[k], k, points)
				);
				
			}
		}

		this.maxDistances = new double[getDescriptor().getAttributes().size()];
		
		for (int i = 0; i < trajectories.size(); i++) {
			MAT<MO> T = trajectories.get(i);	
			for (Subtrajectory subtrajectory : candidates) {						
				double[] distances = bestAlignmentByPointFeatures(subtrajectory, T).getSecond();
				for (int j = 0; j < subtrajectory.getPointFeatures().length; j++) {
					subtrajectory.getDistances()[j][i] = distances[j]; //Math.sqrt(distances[j] / size);	
					
					if (maxDistances[subtrajectory.getPointFeatures()[j]] < subtrajectory.getDistances()[j][i] && subtrajectory.getDistances()[j][i] != MAX_VALUE)
						maxDistances[subtrajectory.getPointFeatures()[j]] = subtrajectory.getDistances()[j][i];
				}
			}
		}
		
		return candidates;
		
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
			int size, int maxSize, Random random, SortedSet<Integer> trajectory_marks) {
				
		Subtrajectory subtrajectoryOfSize = growSize(candidate, trajectory, trajectories, size, false, random, trajectory_marks);
		Subtrajectory subtrajectoryOfFeatures = growFeatures(candidate, trajectory, trajectories, random);

		subtrajectoryOfSize = subtrajectoryOfSize.best(subtrajectoryOfFeatures);
		
		if (subtrajectoryOfSize.equals(candidate))
			return candidate;
		else {
			if (size < maxSize) {
				this.currentMaxSizeOfCandidates = Integer.max(size, this.currentMaxSizeOfCandidates);
				return growPivot(subtrajectoryOfSize, trajectory, trajectories, size+1, maxSize, random, trajectory_marks);
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
			List<MAT<MO>> trajectories, int size, boolean left, Random random, SortedSet<Integer> trajectory_marks) {
		
		int start = candidate.getStart() - (left? 1 : 0);
		int end   = candidate.getEnd()   + (left? 0 : 1);
		
		List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();
		for (Aspect<?> asp : uniques[ candidate.getPointFeatures()[0] ]) {
			List<Point> points = copyPoints(candidate);
			// Build Fiction Points
			Point dullPoint = dullPoint(trajectory);
			dullPoint.getAspects().add(asp);
			points.add(dullPoint);
			
			Subtrajectory saux = instantiateCandidate(start, end, 
					trajectory, trajectories, candidate.getPointFeatures(), candidate.getK(), points);
			candidates.add(saux);
			
		}
		
		for (int j = 1; j < candidate.getPointFeatures().length; j++) {

			List<Subtrajectory> lsaux = new ArrayList<Subtrajectory>();
			for (Aspect<?> asp : uniques[ candidate.getPointFeatures()[j] ]) {
				for (Subtrajectory subtrajectory : candidates) {
					List<Point> points = copyPoints(subtrajectory);
					points.get(points.size()-1).getAspects().add(asp);
					
					Subtrajectory saux = instantiateCandidate(start, end, 
							trajectory, trajectories, subtrajectory.getPointFeatures(), subtrajectory.getK(), points);
					lsaux.add(saux);
				}
				
			}
			candidates = lsaux;
		}
		
		Subtrajectory best = candidate;
		for (Subtrajectory subtrajectory : candidates) {
			total_size += 1;

			computeDistances(subtrajectory, trajectories);
			assesQuality(subtrajectory, random);
			
			best = subtrajectory.best(best);
		}
		
		return best;
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
		if (combSize > this.maxNumberOfFeatures)
			return candidate;
		
		if (combSize > this.currentMaxCombinationOfFeatures) {
			addCombinations(combSize, combSize);
		}
		
		ArrayList<Integer> validCombs = new ArrayList<Integer>();
		for (int i = 0; i < combinations.length; i++) {
			if (combinations[i].length == combSize && 
				// combination starts with:
				Arrays.equals(candidate.getPointFeatures(), Arrays.copyOfRange(combinations[i], 0, combSize-1))) {
				validCombs.add(i);
			}
		}
		
		Subtrajectory best = candidate;
		
		for (int k : validCombs) {
			
			int x = combinations[k].length-1;
			int feature = combinations[k][x];
			
			List<Subtrajectory> candidates = new ArrayList<Subtrajectory>();
			candidates.add(candidate);
			
			for (int i = 0; i < candidate.getPoints().size(); i++) {
				Iterator<Aspect> it = uniques[feature].iterator();

				List<Subtrajectory> lsaux = new ArrayList<Subtrajectory>();
				while (it.hasNext()) {
					Aspect<?> asp = it.next();
					for (Subtrajectory subtrajectory : candidates) {
						Subtrajectory saux = instantiateCandidate(subtrajectory.getStart(), subtrajectory.getEnd(), 
								trajectory, trajectories, combinations[k], k, copyPoints(subtrajectory));
						saux.getPoints().get(i).getAspects().add(asp);
						lsaux.add(saux);
					}
				}
				candidates = lsaux;
			}

			for (Subtrajectory subtrajectory : candidates) {
				total_size += 1;

				computeDistances(subtrajectory, trajectories);
				assesQuality(subtrajectory, random);
				
				best = subtrajectory.best(best);
			}
			
		}
		
		return best;
	}

	protected List<Point> copyPoints(Subtrajectory candidate) {
		List<Point> points = new ArrayList<Point>();
		for (Point p: candidate.getPoints()) {
			points.add(p.copy());
		}
		return points;
	}

	protected Collection<Aspect>[] uniques;

//	protected Collection<Aspect>[] uniqueValues() {
//		Set<Aspect>[] uniques = new TreeSet[this.descriptor.getAttributes().size()];
//		for (int k = 0; k < this.descriptor.getAttributes().size(); k++) {
//			uniques[k] = new TreeSet<Aspect>();
//			for (MAT<MO> T : this.trajsFromClass) {
//				this.maxSizeOfCandidates = Math.max(this.maxSizeOfCandidates, T.getPoints().size());
//				for (Point p : T.getPoints()) {
//					uniques[k].add(p.getAspects().get(k));
//				}
//			}
//		}
//		return uniques;
//	}
	
	protected Collection<Aspect>[] uniqueValues() {
		Map<Aspect, Integer>[] uniques = new HashMap[this.descriptor.getAttributes().size()];
		for (int k = 0; k < this.descriptor.getAttributes().size(); k++) {
			uniques[k] = new HashMap<Aspect, Integer>();
			for (MAT<MO> T : this.trajsFromClass) {
				this.maxSizeOfCandidates = Math.max(this.maxSizeOfCandidates, T.getPoints().size());
				for (Point p : T.getPoints()) {
					uniques[k].put(p.getAspects().get(k), uniques[k].getOrDefault(p.getAspects().get(k), 0)+1);
				}
			}
		}
		
		Set<Aspect>[] uniquesKeys = new Set[this.descriptor.getAttributes().size()];
		int minCount = 2;
		for (int k = 0; k < this.descriptor.getAttributes().size(); k++) {
			uniquesKeys[k] = uniques[k].entrySet().stream()
					.filter(a->a.getValue() >= minCount)
					.map(Map.Entry::getKey)
					.collect(Collectors.toSet());
		}
		
		return uniquesKeys;
	}
	
	protected Collection<Aspect>[] uniqueValues(double tau) {
		Map<Aspect, Integer>[] uniques = new HashMap[this.descriptor.getAttributes().size()];
		for (int k = 0; k < this.descriptor.getAttributes().size(); k++) {
			uniques[k] = new HashMap<Aspect, Integer>();
			for (MAT<MO> T : this.trajsFromClass) {
				this.maxSizeOfCandidates = Math.max(this.maxSizeOfCandidates, T.getPoints().size());
				for (Point p : T.getPoints()) {
					uniques[k].put(p.getAspects().get(k), uniques[k].getOrDefault(p.getAspects().get(k), 0)+1);
				}
			}
		}
		
		Set<Aspect>[] uniquesKeys = new Set[this.descriptor.getAttributes().size()];
		int minCount = (int) (this.trajsFromClass.size() * tau);
		for (int k = 0; k < this.descriptor.getAttributes().size(); k++) {
			uniquesKeys[k] = uniques[k].entrySet().stream()
					.filter(a->a.getValue() > minCount)
					.map(Map.Entry::getKey)
					.collect(Collectors.toSet());
		}
		
		return uniquesKeys;
	}
	
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
			AttributeDescriptor attr = this.descriptor.getAttributes().get(k);
			
			distances[i++] = 
					calculateDistance(
							a.getAspects().get(j), 
							b.getAspects().get(k), 
							attr
					); // This also normalize and enhance distances
		}
		
		return distances;
		
	}

	protected Point dullPoint(MAT<MO> trajectory) {
		Point dullPoint = new Point();
		dullPoint.setTrajectory(trajectory);
		return dullPoint;
	}

}
