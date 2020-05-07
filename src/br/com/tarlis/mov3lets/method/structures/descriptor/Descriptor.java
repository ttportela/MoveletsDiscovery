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
package br.com.tarlis.mov3lets.method.structures.descriptor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.tarlis.mov3lets.method.distancemeasure.DistanceMeasure;
import br.com.tarlis.mov3lets.method.distancemeasure.NominalEqualsDistance;
import br.com.tarlis.mov3lets.utils.Mov3letsUtils;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */	
public class Descriptor {
	
	private AttributeDescriptor idFeature = null;
	private AttributeDescriptor labelFeature = null;
	private List<AttributeDescriptor> attributes = null;
	private LoaderDescriptor input = null;
	
	private HashMap<String, Object> params;

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
	
	public LoaderDescriptor getInput() {
		return input;
	}
	
	public void setInput(LoaderDescriptor input) {
		this.input = input;
	}

	/**
	 * 
	 */
	public void configure() {
		attributes.removeAll(Collections.singletonList(null));
		
		for (AttributeDescriptor attr : attributes) {
			if (attr.getComparator() != null && attr.getComparator().getDistance() != null) {
				instantiateDistanceMeasure(attr);
			} else {
				Mov3letsUtils.traceW("Malformed "+ attr.toString());
				System.exit(1);
			}
		}
		
		if (getInput() != null && getInput().getFormat() != null) {
			// Config input format:
			switch (getInput().getFormat()) {
				case "CSV":
					setParam("data_format", "CSV");
					break;
				case "CZIP":
				default:
					setParam("data_format", "CZIP");
					break;
			}
			
			// Config Optimization:
			switch (getInput().getLoader()) {
				case "indexed":
					setFlag("indexed", true);
					break;
				case "interning":
				case "default":
				default:
					setFlag("interning", true);
					break;
			}
			
		}
	}
	
	/**
	 * @param attr
	 */
	private void instantiateDistanceMeasure(AttributeDescriptor attr) {
		String className = attr.getType();
		className = "br.com.tarlis.mov3lets.method.distancemeasure." 
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
			Mov3letsUtils.traceW("default comparator was set for {" 
					+ attr.getOrder() + " , "
					+ attr.getType() + " , "
					+ attr.getText() + "}.");
			Mov3letsUtils.traceE("default comparator was set:", e);
		}
	}
	
	public static Descriptor load(String fileName, HashMap<String, Object> params) throws UnsupportedEncodingException, FileNotFoundException {
		Reader reader = new InputStreamReader(
				new FileInputStream(fileName), "UTF-8");
        Gson gson = new GsonBuilder().create();
        Descriptor descriptor = gson.fromJson(reader, Descriptor.class);
        
        descriptor.setParams(params);
        descriptor.configure();

//		System.out.println(descriptor);
		return descriptor;
	}

	@Override
	public String toString() {
		String s = "";
		for (AttributeDescriptor attribute : attributes) {
			s += "\t - " + attribute + "\n";
		}
		return s;
	}
	
	/**
	 * 
	 */
	public void setParam(String key, Object value) {
		params.put(key, value);
	}
	
	/**
	 * 
	 */
	public void setFlag(String key, boolean value) {
		params.put(key, value);
	}
	
	/**
	 * 
	 */
	public Object getParam(String key) {
		if (params.containsKey(key))
			return params.get(key);
		else
			return null;
	}
	
	public boolean getFlag(String key) {
		if (params.containsKey(key))
			return (boolean) params.get(key);
		else
			return false;
	}

	/**
	 * @param string
	 * @return
	 */
	public int getParamAsInt(String key) {
		if (params.containsKey(key))
			return (int) params.get(key);
		else 
			return 1;
	}

	/**
	 * @param string
	 * @return
	 */
	public double getParamAsDouble(String key) {
		if (params.containsKey(key))
			return (double) params.get(key);
		else 
			return 1.0;
	}

	/**
	 * @param params2
	 */
	public void addParams(HashMap<String, Object> params) {
		this.params.putAll(params);
	}

	/**
	 * @param string
	 * @return
	 */
	public boolean hasParam(String key) {
		return params.containsKey(key);
	}

	/**
	 * @param string
	 * @return
	 */
	public String getParamAsText(String key) {
		if (params.containsKey(key))
			return params.get(key).toString();
		else 
			return null;
	}
	
	/**
	 * @return the params
	 */
	public HashMap<String, Object> getParams() {
		return params;
	}
	
	public void setParams(HashMap<String, Object> params) {
		this.params = params;
	}

	/**
	 * @return
	 */
	public int numberOfFeatures() {
		return getAttributes().size();
	}

}
