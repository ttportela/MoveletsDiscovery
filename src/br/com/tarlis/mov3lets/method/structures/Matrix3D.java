/**
 * 
 */
package br.com.tarlis.mov3lets.method.structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.util.Combinations;

import br.com.tarlis.mov3lets.model.Point;

/**
 * @author tarlis
 *
 */
public class Matrix3D extends ConcurrentHashMap<Integer, double[]> {

//	private double DEFAULT = Double.POSITIVE_INFINITY;
	private List<List<Integer>> combinations = new ArrayList<List<Integer>>();
		
	public Matrix3D(List<List<Integer>> combinations) {
		this.combinations = combinations;
	}
		
	public Matrix3D(boolean exploreDimensions, int numberOfFeatures, int maxNumberOfFeatures) {
		
		switch (maxNumberOfFeatures) {
			case -1: maxNumberOfFeatures = numberOfFeatures; break;
			case -2: maxNumberOfFeatures = (int) Math.ceil(Math.log(numberOfFeatures))+1; break;
			default: break;
		}
		
		makeCombinations(exploreDimensions, numberOfFeatures, maxNumberOfFeatures);
	}
	
	/**
	 * 
	 */
	public void makeCombinations(boolean exploreDimensions, int numberOfFeatures, int maxNumberOfFeatures) {
		
		int currentFeatures;
		if (exploreDimensions){
			currentFeatures = 1;
		} else {
			currentFeatures = numberOfFeatures;
		}
		
		// For each possible NumberOfFeatures and each combination of those: 
		for (;currentFeatures <= maxNumberOfFeatures; currentFeatures++) {
			for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) {					
				
				combinations.add(new ArrayList<Integer>() {{ for (int i : comb) add(i); }});
				
			} // for (int[] comb : new Combinations(numberOfFeatures,currentFeatures)) 					
		} // for (int i = 0; i < train.size(); i++

	}
	
    public int hashCode(Point a, Point b) {
        int result = a == null ? 0 : a.hashCode();

        final int h = b == null ? 0 : b.hashCode();
        
        if (result > h)
        	result = 37 * result + h ^ (h >>> 16);
        else
        	result = 37 * h + result ^ (result >>> 16);

        return result;
    }
	
	/**
	 * @return the combinations
	 */
	public List<List<Integer>> getCombinations() {
		return combinations;
	}
	
	/**
	 * @param combinations the combinations to set
	 */
	public void setCombinations(List<List<Integer>> combinations) {
		this.combinations = combinations;
	}

	/**
	 * @param a
	 * @param b
	 * @param index
	 * @param value
	 */
//	public void add(Point a, Point b, int index, List<Double> value) {
//		super.put(hashCode(a, b), value);
//	}
	
	public void add(Point a, Point b, double[] value) {
		super.put(hashCode(a, b), value);
	}

	/**
	 * @param a
	 * @param b
	 * @param index
	 * @param value
	 */
//	public void add(Point a, Point b, int index, Double value) {
//		super.put(new Tuple<Point, Point, Integer>(a, b, index), Arrays.asList(value));
//	}	

//	public void add(Point a, Point b, Double value) {
//		super.put(hashCode(a, b), Arrays.asList(value));
//	}

	/**
	 * @param a
	 * @param b
	 * @param distances
	 */
//	public void addCombinations(Point a, Point b, double[] distances) {
//		// For each possible *Number Of Features* and each combination of those:
//		for (int i = 0; i < getCombinations().size(); i++) {
//			List<Integer> comb = getCombinations().get(i);
//			List<Double> distComb = new ArrayList<Double>();
//			for (Integer c : comb) {
//				distComb.add(distances[c]);
//			}
//			add(a, b, i, distComb);
//		}
//	}
	
	public void addDistances(Point a, Point b, double[] distances) {
		// For each possible *Number Of Features* and each combination of those:
//		if (super.containsKey(new Tuple<Point, Point, Integer>(a, b, 0)))
//			System.out.println("TEM");
		super.put(hashCode(a, b), distances);
//				DoubleStream.of(distances).boxed().collect(Collectors.toList()));
	}

	/**
	 * @param k
	 * @return
	 */
	public int[] getCombination(int k) {
		return getCombinations().get(k).stream()
				.mapToInt(Integer::intValue)
				.toArray();
	}

	/**
	 * @param point
	 * @param point2
	 * @param k
	 * @return 
	 */
//	public List<Double> get(Point a, Point b, int index) {
//		return super.get(hashCode(a, b));
//	}
	
	public double[] get(Point a, Point b) {
		return super.get(hashCode(a, b));
	}

	/**
	 * @param point
	 * @param point2
	 * @param comb
	 * @return
	 */
	public double[] getBaseDistances(Point a, Point b) {
		return super.get(hashCode(a, b));
//				.stream().mapToDouble(Double::doubleValue).toArray();
	}
	
	@Override
	public Matrix3D clone() {
		Matrix3D clone = new Matrix3D(this.combinations);
		clone.putAll(this);
		return clone;
	}
	
}
