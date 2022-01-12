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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import br.ufsc.mov3lets.method.loader.CZIPInternLoader;
import br.ufsc.mov3lets.method.loader.LoaderAdapter;
import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Comparator;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;

/**
 * The Class Test.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 */
public class ConvertDataset<T extends MAT<?>> {

	/**
	 * The main method.
	 *
	 * @param arg the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] arg) throws Exception {
		
//		String file = "/Users/tarlisportela/OneDrive/3 - Projetos/workdir/data/multivariate_ts/InsectWingbeat/InsectWingbeat_TEST";
		String base = "/Users/tarlisportela/OneDrive/3 - Projetos/datasets/data/multiple_trajectories/";
		String dataset = "Hurricanes";
		String descriptor = "descriptors/Hurricanes_specific_hp.json";
		String[] files = new String[] {"train", "test"};
		
		for (String file : files) {
			convert(base, dataset, descriptor, file);
			for (int i = 1; i <= 5; i++) {
				convert(base, dataset+"/run"+i, descriptor, file);
			}
		}

	}

	public static void convert(String base, String dataset, String descriptor, String file)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		
		System.out.println("Reading dataset " + dataset);
		Descriptor desc = Descriptor.load(base + descriptor, Mov3letsRun.defaultParams());
		desc.setParam("curpath", base+dataset);
		
//		String dataOrigin = base+file+"/"+file;
		String dataOrigin = base+dataset+"/"+file;
		String dataDestin = base+dataset+"/"+file+".mat";
		
		desc.setParam("curpath", base+dataset+"/");
		
		String SEP1 = ",", SEP2 = ":";
		
		LoaderAdapter<MAT<String>> loader = new CZIPInternLoader<MAT<String>>();
//		LoaderAdapter<MAT<String>> loader = new CSVInternLoader<MAT<String>>();
		List<MAT<String>> trajs = loader.loadTrajectories(dataOrigin, desc);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(dataDestin));
		
		// Write Header:
		System.out.println("Writing HEADER.");
	    writer.write("@name " + dataset); writer.newLine();
//	    writer.write("@missing " + false); writer.newLine();
	    writer.write("@dimensions " + desc.getAttributes().size()); writer.newLine();
	    writer.write("@dimensionTypes " + desc.getAttributes().stream().map(AttributeDescriptor::getType)
	    		.collect(Collectors.joining(" "))); writer.newLine();
	    writer.write("@dimensionDistances " + desc.getAttributes().stream()
	    		.map(AttributeDescriptor::getComparator).collect(Collectors.toList()).stream()
	    		.map(Comparator::getDistance).collect(Collectors.joining(" "))); writer.newLine();
	    		
	    writer.write("@data"); writer.newLine();
	    
		System.out.println("Writing Trajectories to " + file);
	    for (MAT<String> mat : trajs) {
    		String line = "";
	    	for (int i = 0; i < desc.getAttributes().size(); i++) {
	    		AttributeDescriptor attr = desc.getAttributes().get(i);
				for (Point p : mat.getPoints()) {
					line += (attr.getType().equals("numeric") ?
							p.getAspects().get(i).getValue() : 
							"\""+p.getAspects().get(i).getValue()+"\"") + SEP1;
				}
				line = line.substring(0, line.length()-1) + SEP2;
			}
	    	line += mat.getMovingObject().toString();
	    	writer.write(line); writer.newLine();
		}
	    
	    writer.newLine();
	    writer.close();
	    System.out.println("Done.");
	}

}