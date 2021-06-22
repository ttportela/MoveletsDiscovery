/**
 * Wizard - Multiple Aspect Trajectory (MASTER) Classification. 
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
package br.ufsc.mov3lets.utils;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;
import br.ufsc.mov3lets.model.aspect.Aspect;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;

/**
 * The Class ParserUtils.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 */
public class ParserUtils {
	
//	public static <MO> Instances convertSet(Descriptor descriptor, List<MAT<MO>> train) {
//		return convertSet(descriptor, train, false);
//	}
	
	public static <MO> Instances convertSet(Descriptor descriptor, List<MAT<MO>> train) throws Exception { //, boolean encode) {
		
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
				
		int k = 0;
		for (AttributeDescriptor attribute : descriptor.getAttributes()) {
			if (attribute.isNumeric())
				atts.add(new Attribute(attribute.getText(), k++));
			else
				atts.add(new Attribute(attribute.getText(), (ArrayList<String>) null, k++));
		}
		atts.add(new Attribute("class", (ArrayList<String>) null, k));
		
	    Instances insts = new Instances("train", atts, train.size());
	    insts.setClassIndex(k);
	    
	    for (MAT<MO> mat : train) {
			 for (Point p : mat.getPoints()) {
				 Instance newInstance  = new DenseInstance(atts.size());
				 newInstance.setDataset(insts);
				 for(int i = 0 ; i < descriptor.getAttributes().size() ; i++)
				 {
					 Aspect<?> a = (Aspect<?>) p.getAspects().get(i);
					 if (a.getValue() instanceof Double)
						 newInstance.setValue(i , ((Aspect<Double>) a).getValue());
					 else if (a.getValue() instanceof Integer)
						 newInstance.setValue(i , ((Aspect<Integer>) a).getValue());
					 else {
						 newInstance.setValue(i , String.valueOf( ((Aspect<?>) a).getValue() ));
					 }
				 }
				 //add the new instance to the main dataset at the last position
				 newInstance.setClassValue(String.valueOf(mat.getMovingObject()));
				 insts.add(newInstance);
			 }	
		}
	    
//	    System.out.println("Before");
//	    for(int i=0; i < k; i=i+1) {
//	      System.out.println("Nominal? "+insts.attribute(i).isNominal());
//	    }
	    
	    StringToNominal filter = new StringToNominal();
		String[] options = new String[2];
        options[0] = "-R";
	    options[1] = "first-last"; //options[1].substring(0, options[1].length()-1);
	    filter.setOptions(options);
	    filter.setInputFormat(insts);
	    insts = Filter.useFilter(insts, filter);

//	    System.out.println("After");
//	    for(int i=0; i < k; i=i+1) {
//	      System.out.println("Nominal? "+insts.attribute(i).isNominal());
//	    }   
	    
	    return insts;
	}
	
}
