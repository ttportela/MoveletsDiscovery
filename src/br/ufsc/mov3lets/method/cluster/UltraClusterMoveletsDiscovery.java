/**
 * 
 */
package br.ufsc.mov3lets.method.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Combinations;
import org.apache.commons.math3.util.Pair;

import br.ufsc.mov3lets.method.discovery.MasterMoveletsDiscovery;
import br.ufsc.mov3lets.method.discovery.structures.TrajectoryDiscovery;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.qualitymeasure.WeightedFrequencyQualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.MSubtrajectory;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class HiperPivotsMoveletsDiscovery.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class UltraClusterMoveletsDiscovery<MO> extends MasterMoveletsDiscovery<MO> implements TrajectoryDiscovery {

	/** The max number of combination of features. */
	protected int maxCombinationOfFeatures = 0;
	
	/** The max size of candidates. */
	protected int maxSizeOfCandidates = 0;
	
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
	public UltraClusterMoveletsDiscovery(MAT<MO> trajectory, List<MAT<MO>> trajsFromClass, List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(trajectory, trajsFromClass, data, train, test, qualityMeasure, descriptor);
//		this.trajectory = trajectory;
		
//		// Ultra default is Log^2:
//		BU 		= getDescriptor().hasParam("bucket_slice")? getDescriptor().getParamAsDouble("bucket_slice") : -2;
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

//		progressBar.trace("HiperT-Pivots Movelets Discovery for Class: " + trajsFromClass.get(0).getMovingObject());
		this.qualityMeasure = new WeightedFrequencyQualityMeasure<MO>(this.data);
				
		// This guarantees the reproducibility
		Random random = new Random(trajectory.getTid());
		/** STEP 2.1: Starts at discovering movelets */
		List<Subtrajectory> candidates = moveletsDiscovery(trajectory, this.data, minSize, maxSize, random);
		
		/** STEP 2.4: SELECTING BEST CANDIDATES */		
		movelets.addAll(this.bestFilter.filter(candidates));
		
		setStats("");
		
		/** STEP 2.2: Runs the pruning process */
		if(getDescriptor().getFlag("last_prunning"))
			movelets = lastPrunningFilter(movelets);

		/** STEP 2.2: ---------------------------- */
//		outputMovelets(movelets);
		outputClusters(movelets);
		/** -------------------------------------- */	
//		System.gc();
		
		return movelets;
	}
	
	private void outputClusters(List<Subtrajectory> movelets) {
		// TODO TEMP Method:
		for (Subtrajectory subtrajectory : movelets) {
//			if (subtrajectory.getCovered().size() < 2 ||
//				subtrajectory.getPointFeatures().length < 2 ||
//				subtrajectory.getSize() < 2) continue;
			
			System.out.println("Cluster [" +subtrajectory.getCovered().size() + "]:" );
			System.out.println(subtrajectory);

//			List<HashMap<String, Object>> features = new ArrayList<>();
			List<HashMap<String, Object>> used_features = new ArrayList<>();
			int[] list_features = subtrajectory.getPointFeatures();
			for(int i=0; i < subtrajectory.getPoints().size(); i++) {
				
				Point point = subtrajectory.getPoints().get(i);

//				HashMap<String, Object> features_in_point = new HashMap<>();
				HashMap<String, Object> used_features_in_point = new HashMap<>();
				
				for(int j=0; j < descriptor.getAttributes().size(); j++) {
//					features_in_point.put(descriptor.getAttributes().get(j).getText(), point.getAspects().get(j).getValue());				
					
					if(ArrayUtils.contains(list_features, j))
						used_features_in_point.put(descriptor.getAttributes().get(j).getText(), point.getAspects().get(j).getValue());
				}

//				features.add(features_in_point);
				used_features.add(used_features_in_point);
			}
			System.out.println(used_features);

			System.out.print("\t");
			for (MAT<?> T : subtrajectory.getCovered()) {
				System.out.print(" " + T.getTid());				
			}
			System.out.println("\n");
		}
	}

	protected Integer total_size = 0;
	
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
		
		int n = trajectory.getPoints().size();

		minSize = minSize(minSize, n);
		maxSize = maxSize(maxSize, minSize, n);
		
		if( minSize <= 1 ) minSize = 1;

		// It starts with the base case
		addStats("Class", trajectory.getMovingObject()); 
		addStats("Trajectory", trajectory.getTid());
		addStats("Trajectory Size", n);			

		List<Subtrajectory> candidatesOfSize = findPivotCandidates(trajectory, trajectories, minSize);
//		computeQuality(candidatesOfSize, random, trajectory);
//		calculateProportion(candidatesOfSize, random);
		for (Subtrajectory subtrajectory : candidatesOfSize) {
//			computeDistances(subtrajectory, trajectories);
			assesQuality(subtrajectory, random);
		}
		total_size += candidatesOfSize.size();

		candidatesOfSize = this.bestFilter.filter(candidatesOfSize);
		for(Subtrajectory candidate : candidatesOfSize) {
			candidates.add(growPivot(candidate, trajectory, trajectories, minSize+1, maxSize, random));
		}

		addStats("Number of Candidates", total_size);
//		addStats("Pivot Candidates", candidatesOfSize.size());
		addStats("Selected Candidates", candidates.size());
		
		candidates = this.bestFilter.filter(candidates);
		
		addStats("Total of Movelets", candidates.size());
		addStats("Max Size", this.maxSizeOfCandidates);
		addStats("Used Features", this.maxCombinationOfFeatures);
		
		progressBar.plus(getStats());
				
		return candidates;
	}
	
	/**
	 * Make combinations (by addition).
	 *
	 * @param exploreDimensions the explore dimensions
	 * @param numberOfFeatures the number of features
	 * @param maxNumberOfFeatures the max number of features
	 * @return the int[][]
	 */
	public int[][] addCombinations(int minNumberOfFeatures, int maxNumberOfFeatures) {
		
		this.maxCombinationOfFeatures = Integer.max(maxNumberOfFeatures, this.maxCombinationOfFeatures);
		
		int currentFeatures = minNumberOfFeatures;
		ArrayList<int[]> combaux = new ArrayList<int[]>();
		// Start in minimum size until max:
		for (;currentFeatures <= maxNumberOfFeatures; currentFeatures++) {
			for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) {					
				
				combaux.add(comb);
				
			}		
		}
		
		if (combinations != null) {
			combinations = ArrayUtils.addAll(combinations, combaux.stream().toArray(int[][]::new));
		} else {
			combinations = combaux.stream().toArray(int[][]::new);
		}
		
		return combinations;
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
	public Subtrajectory growPivot(Subtrajectory candidate, MAT<MO> trajectory,
			List<MAT<MO>> trajectories, int size, int maxSize, Random random) {
				
		Subtrajectory subtrajectoryOfSize = buildNewSize(candidate, trajectory, trajectories, size, false, random);
		Subtrajectory subtrajectoryOfFeatures = growFeatures(candidate, trajectory, trajectories, random);
		
		subtrajectoryOfSize = subtrajectoryOfSize.best(subtrajectoryOfFeatures);
		
		if (subtrajectoryOfSize.equals(candidate))
			return candidate;
		else {
			if (size < maxSize) {
				this.maxSizeOfCandidates = Integer.max(size, this.maxSizeOfCandidates);
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
	 * @return the subtrajectory
	 */
	public Subtrajectory buildNewSize(Subtrajectory candidate, MAT<MO> trajectory, 
			List<MAT<MO>> trajectories, int size, boolean left, Random random) {
		
		int start = candidate.getStart() - (left? 1 : 0);
		int end   = candidate.getEnd()   + (left? 0 : 1);
		
		if (start < 0 || end > trajectory.getPoints().size()-1)
			return candidate;
		
		total_size += 1;
		Subtrajectory subtrajectory = new MSubtrajectory(start, end, trajectory, trajectories.size(),
				candidate.getPointFeatures(), candidate.getK());
		
		// asses quality:
		computeDistances(subtrajectory, trajectories);
		assesQuality(subtrajectory, random);
//		proportionMeasure.assesClassQuality(subtrajectory, maxDistances, random);
		
		return subtrajectory.best(candidate);
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
		
		if (combSize > this.maxCombinationOfFeatures) {
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
		
		Subtrajectory subtrajectoryOfFeature = candidate;
		for (int k : validCombs) {
			Subtrajectory subtrajectory = new MSubtrajectory(candidate.getStart(), candidate.getEnd(), 
					trajectory, trajectories.size(), combinations[k], k);
			
			total_size += 1;
			
			// asses quality:
			computeDistances(subtrajectory, trajectories);
			assesQuality(subtrajectory, random);
//			proportionMeasure.assesClassQuality(subtrajectory, maxDistances, random);
			
			subtrajectoryOfFeature = subtrajectory.best(subtrajectoryOfFeature);
		}
		
		return subtrajectoryOfFeature;
	}
	
//	/**
//	 * @param candidate
//	 * @param random
//	 * @return
//	 */
//	public void assesQuality(Subtrajectory candidate, Random random) {
//		((ProportionQualityMeasure) qualityMeasure).assesClassQuality(candidate, this.maxDistances, random);
//	}

	/**
	 * [THE GREAT GAP].
	 *
	 * @param trajectory the trajectory
	 * @param trajectories the trajectories
	 * @param size the size
	 * @param mdist the mdist
	 * @return the list
	 */
	public List<Subtrajectory> findPivotCandidates(MAT<MO> trajectory, List<MAT<MO>> trajectories, int size) {
		
		int[][] combinations = addCombinations(1, 1);
		
		// Trajectory P size => n
		int n = trajectory.getPoints().size();
		this.maxSizeOfCandidates = size;
		
		// List of Candidates to extract from P:
		List<Subtrajectory> candidates = new ArrayList<>();

		this.maxDistances = new double[getDescriptor().getAttributes().size()];

		// From point 0 to (n - <candidate max. size>) 
		for (int start = 0; start <= (n - size); start++) {
//			Point p = trajectory.getPoints().get(start);
			
			// Extract possible candidates from P to max. candidate size:
			List<Subtrajectory> list = buildSubtrajectory(start, start + size - 1, trajectory, trajectories.size(), combinations);
									
			// For each trajectory in the database
			for (int i = 0; i < trajectories.size(); i++) {
				MAT<MO> T = trajectories.get(i);	
				
//				int limit = T.getPoints().size() - size + 1;
				
//				if (limit > 0)
					for (Subtrajectory subtrajectory : list) {						
						double[] distances = bestAlignmentByPointFeatures(subtrajectory, T).getSecond();
						for (int j = 0; j < subtrajectory.getPointFeatures().length; j++) {
							subtrajectory.getDistances()[j][i] = distances[j]; //Math.sqrt(distances[j] / size);	
							
							if (maxDistances[subtrajectory.getPointFeatures()[j]] < subtrajectory.getDistances()[j][i] && subtrajectory.getDistances()[j][i] != MAX_VALUE)
								maxDistances[subtrajectory.getPointFeatures()[j]] = subtrajectory.getDistances()[j][i];
						}
					}
				
			} // for (int currentFeatures = 1; currentFeatures <= numberOfFeatures; currentFeatures++)
			
			candidates.addAll(list);

		} // for (int start = 0; start <= (n - size); start++)
		
		((WeightedFrequencyQualityMeasure) this.qualityMeasure).setMaxDistances(maxDistances);
		
		return candidates;
		
	}
	
	/**
	 * Builds the subtrajectory.
	 *
	 * @param start the start
	 * @param end the end
	 * @param t the t
	 * @param numberOfTrajectories the number of trajectories
	 * @param combinations the combinations
	 * @return the list
	 */
	public List<Subtrajectory> buildSubtrajectory(
			int start, int end, MAT<MO> t, int numberOfTrajectories, int[][] combinations){
		
		List<Subtrajectory> list = new ArrayList<>();
		
		for (int k = 0; k < combinations.length; k++) {
			list.add(new MSubtrajectory(start, end, t, numberOfTrajectories, combinations[k], K++));
		}
		
//		for (Subtrajectory s : list) {
//			((MSubtrajectory) s).initDistances(t, (List<MAT<?>>) this.train, getDescriptor());
//		}
				
		return list;
	}
	
	/**
	 * Compute distances.
	 *
	 * @param candidate the candidate
	 * @param trajectories the trajectories
	 */
	public void computeDistances(Subtrajectory candidate, List<MAT<MO>> trajectories) {
		/* This pairs will store the subtrajectory of the best alignment 
		 * of the candidate into each trajectory and the distance 
		 * */
		Pair<Subtrajectory, double[]> distance;
		
		double[][] trajectoryDistancesToCandidate = new double[candidate.getPointFeatures().length]
															  [trajectories.size()];
		
		List<Subtrajectory> bestAlignments = new ArrayList<Subtrajectory>();
				
		/* It calculates the distance of trajectories to the candidate
		 */
		for (int i = 0; i < trajectories.size(); i++) {
			
			distance = bestAlignmentByPointFeatures(candidate, trajectories.get(i));
			
			for (int j = 0; j < candidate.getPointFeatures().length; j++) {
				trajectoryDistancesToCandidate[j][i] = distance.getSecond()[j];							
			}
						
			bestAlignments.add(distance.getFirst());		
		}
		
		candidate.setDistances(trajectoryDistancesToCandidate);
		candidate.setBestAlignments(bestAlignments);
	}
	
	/**
	 * Best alignment by point features.
	 *
	 * @param s the s
	 * @param t the t
	 * @return the pair
	 */
	public Pair<Subtrajectory, double[]> bestAlignmentByPointFeatures(Subtrajectory s, MAT<MO> t) {
		double[] maxValues = new double[numberOfFeatures];
		Arrays.fill(maxValues, MAX_VALUE);
				
		if (s.getSize() > t.getPoints().size())
			return new Pair<>(null, maxValues);

		List<Point> menor = s.getPoints();
		List<Point> maior = t.getPoints();
		
		int size =  s.getSize();
		int diffLength = maior.size() - size;	
		int limit = maior.size() - size + 1;		
				
		int[] comb = s.getPointFeatures();
		double[] currentSum; 
		double[] values = new double[numberOfFeatures];
		double[][] distancesForT = new double[comb.length][diffLength+1];
				
		for (int i = 0; i <= diffLength; i++) {

			currentSum = new double[comb.length];
						
			for (int j = 0; j < size; j++) {

				// Here we get from mdist:
				values = getDistances(menor.get(j), maior.get(i + j), s.getPointFeatures());

				for (int k = 0; k < comb.length; k++) {					
					if (currentSum[k] != MAX_VALUE && values[k] != MAX_VALUE)
						currentSum[k] += values[k];
					else {
						currentSum[k] = MAX_VALUE;
					}
				}											
				
			}
			
			for (int k = 0; k < comb.length; k++) {
				distancesForT[k][i] = currentSum[k];
			}
		}
		
		double[][] ranksForT = new double[distancesForT.length][];
		
		if (limit > 0)
			for (int k = 0; k < comb.length; k++) 
				ranksForT[k] = rankingAlgorithm.rank(Arrays.stream(distancesForT[k],0,limit).toArray());
		
		int bestPosition = (limit > 0) ? bestAlignmentByRanking(ranksForT,comb) : -1;
		
		double[] bestAlignment = new double[comb.length];
		
		for (int j = 0; j < comb.length; j++) {
			
			double distance = (bestPosition >= 0) ? distancesForT[j][bestPosition] : MAX_VALUE;
			
			bestAlignment[j] = (distance != MAX_VALUE) ? 
					Math.sqrt( distance / size ) : MAX_VALUE;
			
		}
		
		int start = bestPosition;
		int end = bestPosition + size - 1;
		
		return new Pair<>(new Subtrajectory(start, end , t), bestAlignment);
	}

}
