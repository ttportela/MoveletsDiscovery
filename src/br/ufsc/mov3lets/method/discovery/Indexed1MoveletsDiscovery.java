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
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import br.ufsc.mov3lets.model.aspect.Aspect;
import br.ufsc.mov3lets.model.aspect.Space2DAspect;

/**
 * The Class IndexedMoveletsDiscovery.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 * @param <MO> the generic type
 */
public class Indexed1MoveletsDiscovery<MO> extends DiscoveryAdapter<MO> implements GlobalDiscovery {

	protected double spatialThreshold;
	protected int temporalThreshold;
	protected int featuresThreshold;
	
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
	public Indexed1MoveletsDiscovery(List<MAT<MO>> data, List<MAT<MO>> train, List<MAT<MO>> test,
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
		MATIndex[] indexes = initIndexes();

		HashMap<MO, BitSet> mClasses = new HashMap<MO, BitSet>();
		List<IndexPoint> points = new ArrayList<IndexPoint>();
		// --------------------- LOAD ---------------------
		progressBar.reset(9);
		progressBar.trace("(1/5) Load: Starting...");
		long timer = System.currentTimeMillis();
		
		Map<String, BitSet> mDictionary = new HashMap<String, BitSet>();
		int trainId = load(indexes, points, mDictionary, 0, train, mClasses); // Load Train
		progressBar.plus("(1/5) Load: Train data. Train Trajectories: "+train.size()+". Train Points: "+trainId+".");
		
		int rId = load(indexes, points, mDictionary, trainId, test, mClasses);// Load Test
		progressBar.plus("(1/5) Load: Test data. Train Trajectories: "+test.size()+". Train Points: "+(rId-trainId)+".");
				
		// --------------------- COMBINE ---------------------
		progressBar.trace("(2/5) Combine: Starting...");
		Map<String, int[]> mSemCompositeIndex = combine(indexes, mDictionary, rId); //-> movelets here?

//		for (MATIndex index : indexes) {
//			if (index instanceof TemporalMATIndex) {
//				computeTemporalMatches((TemporalMATIndex) index, points);
//			} else if (index instanceof SpatialMATIndex) {
//				computeSpatialMatches((SpatialMATIndex) index, points);
//			}
//		}
				
		progressBar.plus("(2/5) Combine: Combined Semantic Keys: "+mSemCompositeIndex.size()+".");
		
		// --------------------- COMPRESS ---------------------
		progressBar.trace("(3/5) Compress: Starting...");
		int cId = data.size(); // train + test
		Map<String, int[]> mCSemCompositeIndex = compress(data, mSemCompositeIndex, cId);

		progressBar.plus("(3/5) Compress: Compressed Semantic Keys: "+mCSemCompositeIndex.size()+".");
		
		// ------------------- INTEGRATION --------------------	
		progressBar.trace("(4/5) Integration: Starting...");	
		for (MATIndex index : indexes) {
			if (index instanceof TemporalMATIndex) {
				computeTemporalMatches((TemporalMATIndex) index, points);
			} else if (index instanceof SpatialMATIndex) {
				computeSpatialMatches((SpatialMATIndex) index, points);
			}
		}
		Map<Integer, int[]> mMATIndex = integrateDimensions(indexes, points, trainId, mSemCompositeIndex, mCSemCompositeIndex);
		
		indexes = null; mSemCompositeIndex = null; mCSemCompositeIndex = null; //System.gc();	
		
		// Removes test data from index:
//		for (int i = trainId; i < rId; i++) {
//			mMATIndex.remove(i);
//		}
		timer = System.currentTimeMillis() - timer;
		progressBar.plus("(4/5) Integration: Indexed Keys: "+mMATIndex.size()+". Time for MAT-Index: "+timer+" milliseconds.");
		
		// ------------------ POINTS MATRIX -------------------
		progressBar.trace("(5/5) Processing Results: Starting...");
		// F-Score selection:
		timer = System.currentTimeMillis();
		List<IndexPoint> bestPoints = points; //.subList(0, trainId);
		if (getDescriptor().hasParam("filter_strategy")) {
			progressBar.trace("(5/5) Processing Results: Assessing Quality");
			points = null; //System.gc();
			
			if ("MSM1".equalsIgnoreCase(getDescriptor().getParamAsText("filter_strategy")))
				assessQualityMSM1(mClasses, bestPoints, mMATIndex);
			else
				assessQuality(mClasses, bestPoints, mMATIndex);
			
			bestPoints = bestPoints(bestPoints, mMATIndex); // Removes points from index
		}// else {
//			for (int i = 0; i < points.size(); ++i) {
//				points.get(i).matches = mMATIndex.get(i);
//			}
//		}
		
		progressBar.trace("(5/5) Processing Results: Selected Indexed Keys: "+bestPoints.size()+".");
		progressBar.plus("(5/5) Processing Results: Saving train data...");
		// Save results on train data:
		output("train", train, bestPoints, 0, mMATIndex);
		
		progressBar.plus("(5/5) Processing Results: Saving test data...");
		// Save results on test data:
		output("test", test, bestPoints, train.size(), mMATIndex);
		timer = System.currentTimeMillis() - timer;
		progressBar.plus("(5/5) Processing Results: Time for Output: "+timer+" milliseconds.");

		// --------------------- MOVELETS ---------------------		
//		int maxSize = getDescriptor().getParamAsInt("max_size");
//		int minSize = getDescriptor().getParamAsInt("min_size");
		
			// This guarantees the reproducibility
//			Random random = new Random(trajectory.getTid());

//			/** STEP 2.4: SELECTING BEST CANDIDATES */		
//			movelets.addAll(filterMovelets(candidates));
		
//		/** STEP 2.2: Runs the pruning process */
//		if(getDescriptor().getFlag("last_prunning"))
//			movelets = lastPrunningFilter(movelets);
//
//		/** STEP 2.3: ---------------------------- */
//		outputMovelets(movelets);
//		/** -------------------------------------- */

		/** STEP 2.1: Starts at discovering movelets */
		List<Subtrajectory> movelets = new ArrayList<Subtrajectory>();
		return movelets;
	}

	protected void printStart() {
		progressBar.trace("MAT-IndexMovelets Discovery starting");
	}

	public MATIndex[] initIndexes() {
		numberOfFeatures = getDescriptor().getAttributes().size();
		MATIndex[] indexes = new MATIndex[numberOfFeatures];
		for (int i=0 ; i < numberOfFeatures; ++i) {
			if (getDescriptor().getAttributes().get(i).getType().equalsIgnoreCase("time")) {
				indexes[i] = new TemporalMATIndex();
			} else if (getDescriptor().getAttributes().get(i).getType().equalsIgnoreCase("space2d")) {
				indexes[i] = new SpatialMATIndex(spatialThreshold, i);
			} else {
				indexes[i] = new SemanticMATIndex(getDescriptor().getAttributes().get(i).getText());
			}
		}
		return indexes;
	}
	
	// --------------------- LOAD ---------------------
	public int load(MATIndex[] indexes, List<IndexPoint> points, Map<String, BitSet> mDictionary, int rId, 
			List<MAT<MO>> trajectories, HashMap<MO, BitSet> mClasses) {
//		progressBar.setPrefix("[2.1] >> Load");
		boolean isTrain = rId == 0;
		int cId = 0;
		for (MAT<MO> trajectory : trajectories) {
			rId = loadTrajectory(indexes, points, mDictionary, rId, mClasses, cId, trajectory, isTrain);
			cId++;
//			progressBar.plus("Class: " + trajectory.getMovingObject() + ". Trajectory: " + trajectory.getTid() + ".");
		}
		
		return rId;
	}

	public int loadTrajectory(MATIndex[] indexes, List<IndexPoint> points, Map<String, BitSet> mDictionary, int rId, HashMap<MO, BitSet> mClasses,
			int cId, MAT<MO> trajectory, boolean isTrain) {
		for (int k=0 ; k < trajectory.getPoints().size() ; ++k) {
			Point p = trajectory.getPoints().get(k);
			
			ArrayList<String> semCompositeKey = new ArrayList<>();
			for (int i=0 ; i < getDescriptor().getAttributes().size() ; ++i) {
				Aspect aspect = p.getAspects().get(i);
				if (indexes[i] instanceof SemanticMATIndex)
					semCompositeKey.add(aspect.getValue().toString());
//					indexes[i]: mTwoLevelIndex 
//					indexes[i].put(getDescriptor().getAttributes().get(i).getText(), new HashMap<String, BitSet>());
				indexes[i].addToIndex(aspect, rId);
			}
			
			String semCompositeKeyStr = String.join(MATIndex.SEPARATOR, semCompositeKey);
			addToDictionary(mDictionary, semCompositeKeyStr, rId);
			if (isTrain) points.add( new IndexPoint(cId, rId, p, semCompositeKeyStr) );
			rId++;
			
			// Map class trajectories:
			BitSet clsIds = mClasses.get(trajectory.getMovingObject());
			if (clsIds == null) {
				clsIds = new BitSet();
				clsIds.set(rId);
				mClasses.put(trajectory.getMovingObject(), clsIds);
			} else {
				clsIds.set(rId);
				mClasses.replace(trajectory.getMovingObject(), clsIds);
			}
		}
		return rId;
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

	// --------------------- COMBINE ---------------------
	protected Map<String, int[]> combine(MATIndex[] indexes, Map<String, BitSet> mDictionary, int rId) {
		Map<String, int[]> mSemCompositeIndex = new HashMap<String, int[]>();

//		progressBar.setPrefix("[2.2] >> Combine");
//		progressBar.reset(SemanticMATIndex.mSemDictionary.entrySet().size());
		for (Entry<String, BitSet> pair : mDictionary.entrySet()) {
			mSemCompositeIndex.put(pair.getKey(), 
					getMatchCounter(indexes, pair.getKey().split(MATIndex.SEPARATOR), rId)
			);
//			progressBar.plus("Combine key: " + pair.getKey() + ".");
		}
		
//		SemanticMATIndex.mSemDictionary.clear();
		return mSemCompositeIndex;

	}	
	
	public int[] getMatchCounter(MATIndex[] indexes, String[] attributeValues, int rId) {

		int[] matchCounter = new int[rId + 1];

		int i=0;
//		for (int i=0 ; i < getDescriptor().getAttributes().size() ; ++i) {
		for (MATIndex index : indexes) {

			if (index instanceof SemanticMATIndex) {
				BitSet rIds = ((SemanticMATIndex) index).mIndex.get(attributeValues[i]);
	
				for (int id = rIds.nextSetBit(0); id >= 0; id = rIds.nextSetBit(id + 1)) {
					++matchCounter[id];
				}
				
				++i;
			}
			
		}

		return matchCounter;

	}
	
	// --------------------- COMPRESS ---------------------
	public Map<String, int[]> compress(List<MAT<MO>> trajectories, Map<String, int[]> mSemCompositeIndex, int cId) {

		Map<String, int[]> mCSemCompositeIndex = new HashMap<String, int[]>();
		
//		progressBar.setPrefix("[2.3] >> Compress");
//		progressBar.reset(mSemCompositeIndex.entrySet().size());
		for (Map.Entry<String, int[]> pair : mSemCompositeIndex.entrySet()) {
			mCSemCompositeIndex.put(pair.getKey(), compressConfiguration(trajectories, pair.getValue(), cId));
//			progressBar.plus("Compress key: " + pair.getKey() + ".");
		}
		
		return mCSemCompositeIndex;

	}
	
	public int[] compressConfiguration(List<MAT<MO>> trajectories, int[] configuration, int cId) {

		int[] cConfiguration = new int[cId + 1];

		//		trajectory[0]: cId trajectory[1]: first rId trajectory[2]: last rId
		int rId = 0;
		for (int k=0 ; k < trajectories.size() ; ++k) {
			//	k: cId             rId: first rId 			rId + poins: last rId
			MAT<MO> trajectory = trajectories.get(k);
			for (int currentRId = rId; currentRId <= (rId+trajectory.getPoints().size()-1); ++currentRId) {
				cConfiguration[k] = (int) Math.max(cConfiguration[k], configuration[currentRId]);
			}
		}
		
		return cConfiguration;
	}

	// ------------------- INTEGRATION --------------------
	public Map<Integer, int[]> integrateDimensions(MATIndex[] indexes, List<IndexPoint> points, int trainRId, 
			Map<String, int[]> mSemCompositeIndex, Map<String, int[]> mCSemCompositeIndex) {
		
		Map<Integer, int[]> mMATIndex = new HashMap<Integer, int[]>();
		
		int[] auxScores;

		/* Merging semantic with the space by reading the points (list) */
		for (int i = 0; i < points.size() ; ++i) {
			
			IndexPoint p = points.get(i);
		
			// Integrates semantic AND space --------------

			int[] auxCScores = mCSemCompositeIndex.get(p.semanticCompositeKey).clone();

			BitSet auxMatchBoth = (BitSet) p.spatialMatches.clone();
			auxMatchBoth.and(p.temporalMatches);
			
			auxScores = mSemCompositeIndex.get(p.semanticCompositeKey);
			
			// Matches Space & Time
			for (int pointId = auxMatchBoth.nextSetBit(0); pointId >= 0; pointId = auxMatchBoth.nextSetBit(pointId + 1)) {
				if (points.size() <= pointId) break; // NO match to test points
				auxCScores[points.get(pointId).cId] = (int) Math.max(auxCScores[points.get(pointId).cId], auxScores[pointId]+2);
			}
			// --------------------------------------------

			BitSet auxOneMatch = (BitSet) p.spatialMatches.clone();
			auxOneMatch.xor(p.temporalMatches);
			
			// Update Time OR Space
			for (int pointId = auxOneMatch.nextSetBit(0); pointId >= 0; pointId = auxOneMatch.nextSetBit(pointId + 1)) {
				if (points.size() <= pointId) break; // NO match to test points
				auxCScores[points.get(pointId).cId] = (int) Math.max(auxCScores[points.get(pointId).cId], auxScores[pointId]+1);
			}
			// --------------------------------------------
			
			mMATIndex.put(p.rId, auxCScores);

//			progressBar.plus("Indexed point: " + p.rId + ".");
		}
		
		return mMATIndex;	
	}
	
	public void computeTemporalMatches(TemporalMATIndex index, List<IndexPoint> points) {

		/* TO DO --- Review this Iterator. This process looks to be too heavy */
		Iterator<Integer> cell = index.mIndex.keySet().iterator();

		while (cell.hasNext()) {

			Integer cellAnalyzed = cell.next();
			
			BitSet auxTemporalMatches = new BitSet();
					
			for (int neighbornCell = cellAnalyzed - temporalThreshold ; neighbornCell <= cellAnalyzed + temporalThreshold; ++neighbornCell) {
				BitSet aux = (BitSet) index.mIndex.get(neighbornCell);
				if (aux!=null) {
					auxTemporalMatches.or(index.mIndex.get(neighbornCell));
				}
			}
						
			for (int i = index.mIndex.get(cellAnalyzed).nextSetBit(0); i >= 0; i = index.mIndex.get(cellAnalyzed).nextSetBit(i+1)) {
				if (points.size() <= i) break; // NO match to test points
				points.get(i).temporalMatches.or(auxTemporalMatches);
			}

//			cell.remove();

		}
		
//		index.mTemporalIndex.clear();

	}
	
	public void computeSpatialMatches(SpatialMATIndex index, List<IndexPoint> points) {

		Iterator<String> cell = index.mIndex.keySet().iterator();

		while (cell.hasNext()) {

			String cellAnalyzed = cell.next();

			// The list of candidates rIds to be tested (the one that aren't in the same cell)
			BitSet rIdsToTest = getCellsToTestSpace(index, cellAnalyzed.split(MATIndex.SEPARATOR));
	
			// For each point in the cell analyzed...
			for (int pointId = index.mIndex.get(cellAnalyzed).nextSetBit(0); 
					 pointId >= 0; 
					 pointId = index.mIndex.get(cellAnalyzed).nextSetBit(pointId + 1)) {

				if (points.size() <= pointId) break; // NO match to test points
				IndexPoint p1 = points.get(pointId);

				// Here are the cells that automatically match.
				// CAUTION: This BitSet was created with the object (it is completely unseted).
				p1.spatialMatches.or(index.mIndex.get(cellAnalyzed));
				
				//
				for (int pointCandidate = rIdsToTest.nextSetBit(0); pointCandidate >= 0; pointCandidate = rIdsToTest
						.nextSetBit(pointCandidate + 1)) {
					if (points.size() <= pointCandidate) break; // NO match to test points			
					euclideanDistanceUpdate(index.aId, p1, points.get(pointCandidate), spatialThreshold);
				}
			}

			cell.remove();

		}

//		index.mSpatialIndex.clear();
		
	}
	
	public void euclideanDistanceUpdate(int aId, IndexPoint p1, IndexPoint p2, double spatialThreshold) {
		Space2DAspect asp1 = (Space2DAspect) p1.point.getAspects().get(aId);
		Space2DAspect asp2 = (Space2DAspect) p2.point.getAspects().get(aId);
		if ( Math.sqrt( Math.pow(asp1.getX() - asp2.getX(), 2) + Math.pow(asp1.getY() - asp2.getY(), 2) ) <= spatialThreshold ) {
			p1.spatialMatches.set(p2.rId);
			p2.spatialMatches.set(p1.rId);
		}
	}
	
	public BitSet getCellsToTestSpace(SpatialMATIndex index, String[] coordinates) {

		int[] coord = {Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])};
		
		/*
		 * TO DO: Optimized this method. An option is to use the
		 */

		BitSet rIdsToTest = new BitSet();
		BitSet aux;

		int[] auxKey = new int[2];

		for (int[] key : SpatialMATIndex.SPATIAL_ADJACENTS) {

			auxKey[0] = coord[0] + key[0];
			auxKey[1] = coord[1] + key[1];

			aux = index.mIndex.get(auxKey[0] + MATIndex.SEPARATOR + auxKey[1]);
			if (aux != null) {
				rIdsToTest.or(aux);
			}
		}

		return rIdsToTest;

	}

	public void assessQuality(HashMap<MO, BitSet> mClasses, List<IndexPoint> points,
			Map<Integer, int[]> mMATIndex) {
//		progressBar.setPrefix("[2.5] >> Output Train");
//		progressBar.reset(train.size());
//		int rId = 0;
		for (Entry<Integer, int[]> e : mMATIndex.entrySet()) {
//		for (int rId = 0; rId < points.size(); ++rId) {
//			if (e.getKey() >= train.size()) continue;
			
			MAT<MO> trajectory = (MAT<MO>) points.get(e.getKey()).point.getTrajectory();
//		for (MAT<MO> trajectory : train) {
			BitSet clsIds = mClasses.get(trajectory.getMovingObject());
//			for (int k=0 ; k < trajectory.getPoints().size() ; ++k) {
//				Point p = trajectory.getPoints().get(k);
				
				// F-Score, count positives and negatives
				int[] matches = e.getValue();
				// True Positive, True Negative, False Positive, False Negative
				double TP = 0.0, TN = 0.0, FP = 0.0, FN = 0.0;
				for (int id = 0; id < train.size(); ++id) {
					if (clsIds.get(id)) {
//						if (matches[id] >= numberOfFeatures)
//							TP++; 
//						else
//							FN++; 
						TP += matches[id]; // True Positive: same class & similar
						FN += numberOfFeatures-matches[id]; // False Negative: NOT class & similar 
					} else {
//						if (matches[id] >= numberOfFeatures)
//							FP++; 
//						else
//							TN++; 
						TN += numberOfFeatures-matches[id]; // True Negative: NOT class & NOT similar
						FP += matches[id]; // False Positive: same class & NOT similar
					}
				}
				
				// F-Score, count in and out of class				
				IndexPoint p = points.get(e.getKey());
				p.TP = TP;
				p.FP = FP;
				p.FN = FN;
				p.TN = TN;
				
				if ("acc".equalsIgnoreCase(getDescriptor().getParamAsText("filter_strategy")))
					p.quality = p.accuracy();
				else
					p.quality = p.fscore();
				
//				p.matches = matches;
//				rId++;
//			}
			
			// Print points with high F-Score
//			progressBar.plus();
		}
	}
	
	public void assessQualityMSM1(HashMap<MO, BitSet> mClasses, List<IndexPoint> points,
			Map<Integer, int[]> mMATIndex) {

		for (Entry<Integer, int[]> e : mMATIndex.entrySet()) {
//		for (int i = 0; i < points.size(); ++i) {
			if (e.getKey() >= train.size()) continue;
			
			IndexPoint p = points.get(e.getKey());
			BitSet clsIds = mClasses.get(p.point.getTrajectory().getMovingObject());
			double parityT = 0.0, parityNT = 0.0;
			double lenT = 0.0, lenNT = 0.0;

			int[] matches = e.getValue();
			for (int id = 0; id < train.size(); ++id) {
				// MSM for 1 point.
				if (clsIds.get(id)) {
					parityT += matches[id];
					lenT++;
				} else {
					parityNT += matches[id];
					lenNT++;
				}
			}
			
			// Quality assess:			
			double sim = (parityT + parityNT) / (lenT + lenNT) / numberOfFeatures;
			p.quality = sim;			
//			p.matches = matches;
			
		}
	}
	
	public Map<Integer, BitSet> matchBitSet(List<IndexPoint> points, Map<Integer, int[]> mMATIndex) {

		Map<Integer, BitSet> matchSet = new HashMap<Integer, BitSet>();
		
		for (Entry<Integer, int[]> e : mMATIndex.entrySet()) {
			for (int id = 0; id < train.size(); ++id) {
				
				if (e.getValue()[id] == numberOfFeatures) {
					BitSet rIds = matchSet.get(id);
					if (rIds == null) {
						rIds = new BitSet();
						rIds.set(e.getKey());
						matchSet.put(id, rIds);
					} else {
						rIds.set(e.getKey());
						matchSet.replace(id, rIds);
					}
				}
				
			}			
		}
		
		return matchSet;
	}
	
	public double score(Map<Integer, int[]> mMATIndex, int[] A, int[] B) {

//		int[] A = {cId, start, end};
		
		int lenA = A[2]-A[1]+1;
		int lenB = B[2]-B[1]+1;
		
		double parityAB = 0;
		for (int id = A[1]; id <= A[2]; ++id) {
			parityAB += mMATIndex.get(id)[B[0]];
		}
		
		double parityBA = 0;
		for (int id = B[1]; id <= B[2]; ++id) {
			parityBA += mMATIndex.get(id)[A[0]];
		}
		
		return (parityAB + parityBA) / (lenA + lenB) / numberOfFeatures;
	}

	public void output(String filename, List<MAT<MO>> trajectories, List<IndexPoint> bestPoints, int startId, Map<Integer, int[]> mMATIndex) {
		if (outputers != null)
			for (OutputterAdapter output : outputers) {
				output.write(filename, trajectories, bestPoints, false, startId, mMATIndex);			
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
