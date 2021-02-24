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
package br.ufsc.mov3lets.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import br.ufsc.mov3lets.model.MAT;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 * The Class Test.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 */
public class Test {

	/**
	 * The main method.
	 *
	 * @param arg the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] arg) throws Exception {
		
		String path = "/Users/tarlis/git/HIPERMovelets/tarlis/new_data/worms/train.arff";
		BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
		ArffReader arff = new ArffReader(reader, 1000);         
		Instances data = arff.getStructure();
		data.setClassIndex(0);

		
		System.out.println(data.numAttributes());
		System.out.println(data.numClasses());
		
		Instance inst;
		while ((inst = arff.readInstance(data)) != null) {    
			
			System.out.println(inst.attribute(899));
			System.exit(0);
			
		    // the first attribute is ignored because it is the index
		    for(int index = 0 ; index < inst.numAttributes() ; index++) {
			    System.out.println("index "+index+" is : "+(inst.value(index)) + " - " + inst.attribute(index).type());
		    	
		        switch(inst.attribute(index).type()) {
		        case Attribute.NUMERIC :
		            System.out.println(inst.value(index)); break;
		        case Attribute.STRING :
		        case Attribute.NOMINAL:
		            System.out.println(inst.stringValue(index)); break;
		        case Attribute.RELATIONAL : 
		            // test if we have an imbrication of two relations or not
		            if (inst.attribute(index).relation().numAttributes() > 0 &&
		                    inst.attribute(index).relation().attribute(0).isRelationValued()) {
		                // case of an array of int arrays
		                double[][] seq = new double[inst.attribute(index).relation().numAttributes()][];
		                for (int i = 0 ; i < inst.attribute(index).relation().numAttributes() ; i++) {
		                    Instances instances = inst.relationalValue(index);
		                    seq[i] = new double[instances.attribute(0).relation().numAttributes()];

		                    Instance q = instances.instance(0).relationalValue(i).get(0);
		                    for(int j = 0 ; j < instances.attribute(0).relation().numAttributes() ; j++) {
		                        seq[i][j] = q.value(j);

		                    }
		                }
		                System.out.println(seq);
		            } else {
		                // case wit only an arry of int
		                double[] seq = new double[inst.attribute(index).relation().numAttributes()];
		                for (int i = 0 ; i < inst.attribute(index).relation().numAttributes() ; i++) {
		                        seq[i] = inst.value(i);
		                }
		                System.out.println(seq);
		            }
		        }
		    }
		}
		
		
	}

	/**
	 * Prop.
	 *
	 * @param max the max
	 * @param distances the distances
	 * @return the double
	 */
	private static double prop(double[] max, double[][] distances) {
		double proportion = 0.0;
		for (int i = 0; i < distances.length; i++) {
//			double pSum = 0.0;
			double total = 0.0;	
			double sum = 0.0;
			double split = max[i]*0.1;
			
			for (int j = 0; j < distances[i].length; j++) {
					if (distances[i][j] <= split)
						sum += max[i] - distances[i][j];

//					if (distances[i][j] == 0.0) {
//						pSum += 1.0;
//						freq[j] += 1;
//					}
					
			}

			total = max[i] * (double) distances[i].length;
//			System.out.println(sum);
//			System.out.println(total);
//			System.out.println(split);
			proportion += (sum / total);
//			pZero += pSum / (double) distances[i].length;
			
		}
		return proportion;
	}
	
	/**
	 * Median.
	 *
	 * @param a the a
	 * @param l the l
	 * @param r the r
	 * @return the int
	 */
	private static int median(double a[],  
			int l, int r) 
	{ 
		int n = r - l + 1; 
		n = (n + 1) / 2 - 1; 
		return n + l; 
	}
	
	/**
	 * Iqr.
	 *
	 * @param a the a
	 * @param n the n
	 * @return the double
	 */
	private static double IQR(double [] a, int n) 
	{ 
	    Arrays.sort(a); 
	  
	    // Index of median  
	    // of entire data 
	    int mid_index = median(a, 0, n); 
	  
	    // Median of first half 
	    double Q1 = a[median(a, 0,  
	                      mid_index)]; 
	  
	    // Median of second half 
	    double Q3 = a[median(a,  
	               mid_index + 1, n)]; 
	  
	    // IQR calculation 
	    return Q1; //(Q3 - Q1); 
	} 

	/** The index not found. */
	static int INDEX_NOT_FOUND = -1;
	
	/**
	 * Difference.
	 *
	 * @param str1 the str 1
	 * @param str2 the str 2
	 * @return the string
	 */
	public static String difference(String str1, String str2) {
	    if (str1 == null) {
	        return str2;
	    }
	    if (str2 == null) {
	        return str1;
	    }
	    int at = indexOfDifference(str1, str2);
	    if (at == INDEX_NOT_FOUND) {
	        return null;
	    }
	    return str2.substring(at);
	}

	/**
	 * Index of difference.
	 *
	 * @param cs1 the cs 1
	 * @param cs2 the cs 2
	 * @return the int
	 */
	public static int indexOfDifference(CharSequence cs1, CharSequence cs2) {
	    if (cs1 == cs2) {
	        return INDEX_NOT_FOUND;
	    }
	    if (cs1 == null || cs2 == null) {
	        return 0;
	    }
	    int i;
	    for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
	        if (cs1.charAt(i) != cs2.charAt(i)) {
	            break;
	        }
	    }
	    if (i < cs2.length() || i < cs1.length()) {
	        return i;
	    }
	    return INDEX_NOT_FOUND;
	}
	
}