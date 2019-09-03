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
package br.com.tarlis.mov3lets.view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.tarlis.mov3lets.model.distancemeasure.DistanceMeasure;
import br.com.tarlis.mov3lets.model.distancemeasure.NominalEqualsDistance;
import br.com.tarlis.mov3lets.run.Mov3lets;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */	
public class Descriptor {
	
	private AttributeDescriptor idFeature = null;
	private AttributeDescriptor labelFeature = null;
	private List<AttributeDescriptor> attributes = null;
	private List<String> inputFiles = null;

	/**
	 * @return the idFeature
	 */
	public AttributeDescriptor getIdFeature() {
		return idFeature;
	}
	
	/**
	 * @param idFeature the idFeature to set
	 */
	public void setIdFeature(AttributeDescriptor idFeature) {
		this.idFeature = idFeature;
	}
	
	/**
	 * @return the labelFeature
	 */
	public AttributeDescriptor getLabelFeature() {
		return labelFeature;
	}
	
	/**
	 * @param labelFeature the labelFeature to set
	 */
	public void setLabelFeature(AttributeDescriptor labelFeature) {
		this.labelFeature = labelFeature;
	}
	
	/**
	 * @return the attributes
	 */
	public List<AttributeDescriptor> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(List<AttributeDescriptor> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the inputFiles
	 */
	public List<String> getInputFiles() {
		return inputFiles;
	}

	/**
	 * @param inputFiles the inputFiles to set
	 */
	public void setInputFiles(List<String> inputFiles) {
		this.inputFiles = inputFiles;
	}

	/**
	 * 
	 */
	public void configure() {
		for (AttributeDescriptor attr : attributes) {
			if (attr.getComparator() != null && attr.getComparator().getDistance() != null) {
				instantiateDistanceMeasure(attr);
			}
		}
	}
	
	/**
	 * @param attr
	 */
	private void instantiateDistanceMeasure(AttributeDescriptor attr) {
		String className = attr.getType();
		className = "br.com.tarlis.mov3lets.model.distancemeasure." 
				+ className.substring(0, 1).toUpperCase() + className.substring(1).toLowerCase();
		className += attr.getComparator().getDistance().substring(0, 1).toUpperCase() 
				+ attr.getComparator().getDistance().substring(1).toLowerCase();
		className += "Distance";
		try {
			attr.setDistanceComparator((DistanceMeasure<?>) Class.forName(className).getConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			// Sets DEFAULT in case of error:
			attr.setDistanceComparator(new NominalEqualsDistance());
			Mov3lets.traceW("default comparator was set for {" 
					+ attr.getOrder() + " , "
					+ attr.getType() + " , "
					+ attr.getText() + "}.");
			Mov3lets.traceE("default comparator was set:", e);
		}
	}

	@Override
	public String toString() {
		String s = "";
		for (AttributeDescriptor attribute : attributes) {
			s += attribute + "\n";
		}
		return s;
	}
	
	public static Descriptor load(String fileName) throws UnsupportedEncodingException, FileNotFoundException {
		Reader reader = new InputStreamReader(
				new FileInputStream(fileName), "UTF-8");
        Gson gson = new GsonBuilder().create();
        Descriptor descriptor = gson.fromJson(reader, Descriptor.class);
        descriptor.configure();

		System.out.println(descriptor);
		return descriptor;
	}

}
