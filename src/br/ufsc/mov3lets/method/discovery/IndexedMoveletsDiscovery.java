/**
 * Mov3lets - Multiple Aspect Trajectory (MASTER) Classification Version 3. 
 * Copyright (C) 2019  Tarlis Portela <tarlis@tarlis.com.br>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package br.ufsc.mov3lets.method.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import br.ufsc.mov3lets.method.discovery.structures.DiscoveryAdapter;
import br.ufsc.mov3lets.method.discovery.structures.GlobalDiscovery;
import br.ufsc.mov3lets.method.output.OutputterAdapter;
import br.ufsc.mov3lets.method.qualitymeasure.QualityMeasure;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.method.structures.indexed.IndexPoint;
import br.ufsc.mov3lets.method.structures.indexed.MATIndex;
import br.ufsc.mov3lets.method.structures.indexed.SemanticMATIndex;
import br.ufsc.mov3lets.method.structures.indexed.SpatialMATIndex;
import br.ufsc.mov3lets.method.structures.indexed.TemporalMATIndex;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.Subtrajectory;

/**
 * The Class IndexedMoveletsDiscovery.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 * @param <MO> the generic type
 */
public class IndexedMoveletsDiscovery<MO> extends DiscoveryAdapter<MO> implements GlobalDiscovery {

	protected double spatialThreshold;
	protected int temporalThreshold;
	protected int featuresThreshold;
	
	protected Map<String, Map<String, BitSet>> combinationsIndex = new HashMap<String, Map<String, BitSet>>();
	
	/**
	 * Instantiates a new indexed movelets discovery.
	 *
	 * @param trajsFromClass the trajs from class
	 * @param data the data
	 * @param train the train
	 * @param test the test
	 * @param qualityMeasure the quality measure
	 * @param descriptor the descriptor
	 */
	public IndexedMoveletsDiscovery(List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
			QualityMeasure qualityMeasure, Descriptor descriptor) {
		super(null, null, data, train, test, descriptor);
//		super(null, null, data, train, test, qualityMeasure, descriptor);
		
		spatialThreshold 	= getDescriptor().hasParam("spatial_threshold")? 
				getDescriptor().getParamAsDouble("spatial_threshold") : 0.00142;
		temporalThreshold	= getDescriptor().hasParam("temporal_threshold")? 
				getDescriptor().getParamAsInt("temporal_threshold") : 30;
		featuresThreshold = (int) Math.ceil(Math.log(getDescriptor().getAttributes().size()));
		
		init();
	}

	/**
	 * Looks for candidates in the trajectory, then compares with every other trajectory.
	 *
	 * @return the list
	 */
	public List<Subtrajectory> discover() {
		
		printStart();
		
		/** Define index structures: */
		makeCombinations(exploreDimensions, numberOfFeatures, maxNumberOfFeatures);
		MATIndex[] indexes = initIndexes();

		HashMap<MO, Integer> mClasses = new HashMap<MO, Integer>();
		List<IndexPoint> points = new ArrayList<IndexPoint>();
		// --------------------- LOAD ---------------------
		progressBar.reset(8+combinations.length);
		progressBar.trace("(1/5) Load: Starting...");
		long timer = System.currentTimeMillis();
		int trainId = load(indexes, points, 0, train, mClasses); // Load Train
		progressBar.plus("(1/5) Load: Train data. Train Trajectories: "+train.size()+". Train Points: "+trainId+".");
		
		int rId = load(indexes, points, trainId, test, mClasses);// Load Test
		progressBar.plus("(1/5) Load: Test data. Test Trajectories: "+test.size()+". Test Points: "+(rId-trainId)+".");
				
		// --------------------- COMBINE ---------------------
		// Combine is to count matches
		progressBar.trace("(2/5) Combine: Starting...");
		// Dictionary should be made only with best qualified keys:
		Map<String, BitSet> mCompositeIndex = new HashMap<String, BitSet>();
		
		for (int k = 0; k < combinations.length; k++) {
			progressBar.plus("(2/5) Combine: Combining Dimensions: " + Arrays.toString(combinations[k]) + ".");
			Map<String, BitSet> mCombDictionary = combine(k, indexes);
			
			// Filter:
			if (getDescriptor().hasParam("filter_strategy")) {				
				filterByQuality(mClasses, points, mCombDictionary);
			}
			
			// Add to dictionary:
			mCompositeIndex.putAll(mCombDictionary);
		}
		combinationsIndex = null; //System.gc();
				
		progressBar.plus("(2/5) Combine: Combined Keys: "+mCompositeIndex.size()+". Time for MAT-Index: "+timer+" milliseconds.");
		
		// --------------------- COMPRESS ---------------------
		progressBar.trace("(3/5) Compress: Starting...");
		int cId = data.size(); // train + test
		Map<String, BitSet> mCCompositeIndex = compress(data, mCompositeIndex, cId);
		
		timer = System.currentTimeMillis() - timer;
		progressBar.plus("(3/5) Compress: Compressed Semantic Keys: "+mCCompositeIndex.size()+".");

		
		// ------------------ POINTS MATRIX -------------------
		// F-Score selection:
		progressBar.trace("(4/5) Processing Results: Selected Indexed Keys: "+mCCompositeIndex.size()+".");
		progressBar.plus("(4/5) Processing Results: Saving train data...");
		// Save results on train data:
		timer = System.currentTimeMillis();
		output("train", train, points, 0, mCCompositeIndex);
		
		progressBar.plus("(5/5) Processing Results: Saving test data...");
		// Save results on test data:
		output("test", test, points, train.size(), mCCompositeIndex);
		timer = System.currentTimeMillis() - timer;
		progressBar.plus("(5/5) Processing Results: Time for Output: "+timer+" milliseconds.");

		/** STEP 2.1: Starts at discovering movelets */
		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();
		return movelets;
	}
	
	protected void printStart() {
		progressBar.trace("IndexedMovelets Discovery starting");
	}

	public MATIndex[] initIndexes() {
		numberOfFeatures = getDescriptor().getAttributes().size();
		MATIndex[] indexes = new MATIndex[numberOfFeatures];
		for (int i=0 ; i < numberOfFeatures; ++i) {
			if (getDescriptor().getAttributes().get(i).getType().equalsIgnoreCase("time")) {
				indexes[i] = new TemporalMATIndex(temporalThreshold);
			} else if (getDescriptor().getAttributes().get(i).getType().equalsIgnoreCase("space2d")) {
				indexes[i] = new SpatialMATIndex(spatialThreshold, i);
			} else {
				indexes[i] = new SemanticMATIndex(getDescriptor().getAttributes().get(i).getText());
			}
		}
		return indexes;
	}
	
	public void addToDictionary(Map<String, BitSet> mDictionary, String semCompositeKey, int rId) {

		BitSet rIds = mDictionary.get(semCompositeKey);

		if (rIds == null) {
			rIds = new BitSet();
			rIds.set(rId);
			mDictionary.put(semCompositeKey, rIds);
		} else {
			rIds.set(rId);
			mDictionary.replace(semCompositeKey, rIds);
		}

	}
	
	// --------------------- LOAD ---------------------
	public int load(MATIndex[] indexes, List<IndexPoint> points, int rId, 
			List<MAT<MO>> trajectories, HashMap<MO, Integer> mClasses) {

		boolean isTrain = rId == 0;
		int cId = 0;
		for (MAT<MO> trajectory : trajectories) {
			rId = loadTrajectory(indexes, points, rId, mClasses, cId, trajectory, isTrain);
			cId++;
		}
		
		return rId;
	}

	public int loadTrajectory(MATIndex[] indexes, List<IndexPoint> points, int rId, HashMap<MO, Integer> mClasses,
			int cId, MAT<MO> trajectory, boolean isTrain) {
		
		for (int k=0 ; k < trajectory.getPoints().size() ; ++k) {
			Point p = trajectory.getPoints().get(k);
			
			for (int i=0 ; i < getDescriptor().getAttributes().size() ; ++i) {
				indexes[i].addToIndex(p.getAspects().get(i), rId);
			}
			
			if (isTrain) {
				points.add( new IndexPoint(cId, rId, p, numberOfFeatures) );
				
				// Map class trajectories:
				Integer clsCount = mClasses.getOrDefault(trajectory.getMovingObject(), 0);
				clsCount++;
				mClasses.put(trajectory.getMovingObject(), clsCount);
			}
			rId++;
			
		}
		return rId;
	}

	// --------------------- COMBINE ---------------------
	public Map<String, BitSet> combine(int k, MATIndex[] indexes) {
		Map<String, BitSet> mCompositeIndex = new HashMap<String, BitSet>();
		
		int[] combination = combinations[k];
		
		if (combination.length == 1) {		
			for (Entry<?, BitSet> pair : (Set<Entry<?, BitSet>>) indexes[combination[0]].mIndex.entrySet()) {
				BitSet matches = (BitSet) pair.getValue().clone();
				
				String key = String.valueOf(k) + MATIndex.SEPARATOR + String.valueOf(pair.getKey());
				
				if (matches.cardinality() > 2)
					mCompositeIndex.put(key, matches);
			}
			
			combinationsIndex.put(Arrays.toString(combination), mCompositeIndex);
		} else {
			mCompositeIndex = combinationsIndex.get(Arrays.toString(Arrays.copyOfRange(combination, 0, combination.length-1)));
			Map<String, BitSet> mCompositeIndexAux = combinationsIndex.get(Arrays.toString(
					Arrays.copyOfRange(combination, combination.length-1, combination.length)));
			
			mCompositeIndex = integrate(mCompositeIndex, mCompositeIndexAux);
//			for (int i = 1; i < combination.length; i++) {
//				mCompositeIndex = integrate(mCompositeIndex, indexes[combination[i]]);
//			}
			combinationsIndex.put(Arrays.toString(combination), mCompositeIndex);
		}
		
		return mCompositeIndex;
	}
	
	public Map<String, BitSet> integrate(Map<String, BitSet> mCompositeIndexA, Map<String, BitSet> mCompositeIndexB) {
		Map<String, BitSet> mCompositeIndex = new HashMap<String, BitSet>();
		for (Entry<String, BitSet> paira : mCompositeIndexA.entrySet()) {
			for (Entry<String, BitSet> pairb : mCompositeIndexB.entrySet()) {
				BitSet matches = (BitSet) paira.getValue().clone();
				matches.and(pairb.getValue());

				if (matches.cardinality() > 2)
					mCompositeIndex.put(paira.getKey() + MATIndex.SEPARATOR + String.valueOf(pairb.getKey()), matches);
			}
		}
		
		return mCompositeIndex;
	}
	
	// --------------------- COMPRESS ---------------------
	public Map<String, BitSet> compress(List<MAT<MO>> trajectories, Map<String, BitSet> mIndex, int cId) {

		Map<String, BitSet> mCIndex = new HashMap<String, BitSet>();
		
		for (Entry<String, BitSet> pair : mIndex.entrySet()) {
			mCIndex.put(pair.getKey(), compressConfiguration(trajectories, pair.getValue(), cId));
		}
		
		return mCIndex;

	}
	
	public BitSet compressConfiguration(List<MAT<MO>> trajectories, BitSet configuration, int cId) {

		BitSet cConfiguration = new BitSet();

		//		trajectory[0]: cId trajectory[1]: first rId trajectory[2]: last rId
		int rId = 0;
		for (int k=0 ; k < trajectories.size() ; ++k) {
			//	k: cId             rId: first rId 			rId + poins: last rId
			MAT<MO> trajectory = trajectories.get(k);
			boolean isset = false;
			for (int currentRId = rId; currentRId <= (rId+trajectory.getPoints().size()-1); ++currentRId) {
				isset = configuration.get(currentRId) | isset;
			}
			if (isset) cConfiguration.set(k);
		}
		
		return cConfiguration;
	}

	public void filterByQuality(HashMap<MO, Integer> mClasses, List<IndexPoint> points, Map<String, BitSet> mMATIndex) {

		ArrayList<String> toRemove = new ArrayList<String>();
		for (Entry<String, BitSet> e : mMATIndex.entrySet()) {
			
			HashMap<MO, Integer> cClasses = new HashMap<MO, Integer>();
			int total = 0;
			for (int pointId = e.getValue().nextSetBit(0); pointId >= 0; pointId = e.getValue().nextSetBit(pointId + 1)) {
				if (points.size() <= pointId) break;  // NO match to test points
				MO ckey = (MO) points.get(pointId).point.getTrajectory().getMovingObject();
				Integer counter = cClasses.getOrDefault(ckey, 0);
				counter++;
				total++;
				cClasses.put(ckey, counter);
			}
			
			boolean keep = false;
			for (Entry<MO, Integer> cc : cClasses.entrySet()) {
				double TP = 0.0, TN = 0.0, FP = 0.0, FN = 0.0;
				int clsTotal = mClasses.get(cc.getKey());
				int negTotal = points.size() - mClasses.get(cc.getKey());
				TP = cc.getValue();
				FN = clsTotal - TP;
				FP = total - TP;
				TN = negTotal - FP;
				
				double quality = 0;
				if ("proportion".equalsIgnoreCase(getDescriptor().getParamAsText("filter_strategy")))
					quality = TP/clsTotal;
				else if ("error".equalsIgnoreCase(getDescriptor().getParamAsText("filter_strategy")))
					quality = 1.0 - FP/negTotal;
				else if ("globalprop".equalsIgnoreCase(getDescriptor().getParamAsText("filter_strategy")))
					quality = ((TP/clsTotal) + (1.0 - FP/negTotal)) / 2.0;
				else if ("acc".equalsIgnoreCase(getDescriptor().getParamAsText("filter_strategy")))
					quality = (TP + TN) / (TP + TN + FP + FN);
				else {
					double precision = (TP + FP) > 0 ? TP / ((TP + FP) * 1.0d) : 0.0;
					double recall = (TP + FN) > 0 ? TP / ((TP + FN) * 1.0d) : 0.0;
					quality = (precision > 0 && recall > 0) ? 2.0 / (1 / precision + 1 / recall) : 0.0;
				}
				if (!Double.isNaN(quality) && quality > 0.0) {
					keep = true;
					break;
				}
			}
			
			// REMOVE
			if (!keep) {
				toRemove.add(e.getKey());
			}
		}
		
		for (String key : toRemove) {
			mMATIndex.remove(key);
		}
		
	}

	public void output(String filename, List<MAT<MO>> trajectories, List<IndexPoint> bestPoints, int startId, Map<String, BitSet> mCCompositeIndex) {
		if (outputers != null)
			for (OutputterAdapter output : outputers) {
				output.write(filename, trajectories, bestPoints, false, startId, mCCompositeIndex);			
			}
	}

	public List<IndexPoint> bestPoints(List<IndexPoint> points, Map<Integer, int[]> mMATIndex) {
		points.sort(new Comparator<IndexPoint>() {
			@Override
			public int compare(IndexPoint o1, IndexPoint o2) {
				
				return Double.compare(o1.fscore(), o2.fscore()) *-1;				
				
			}
		});
		
		// Realiza o loop até que acabem os atributos ou até que atinga o número
		// máximo de nBestShapelets
		// Isso é importante porque vários candidatos bem rankeados podem ser
		// selfsimilares com outros que tiveram melhor score;
		List<IndexPoint> bestPoints = new ArrayList<IndexPoint>();
		for (int i = 0; (i < points.size()); i++) {

			// Se a shapelet candidata tem score 0 então já termina o processo
			// de busca
			if (points.get(i).fscore() <= 0.0)
				mMATIndex.remove(i);
			else 
				bestPoints.add(points.get(i));

			// Removing self similar or other filters?
		}

		return bestPoints;
	}

}
