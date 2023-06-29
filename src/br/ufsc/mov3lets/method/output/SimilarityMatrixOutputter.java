/**
 * 
 */
package br.ufsc.mov3lets.method.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.ufsc.mov3lets.model.MAT;

/**
 * The Class CSVOutputter.
 *
 * @author tarlis
 * @param <MO> the generic type
 */
public class SimilarityMatrixOutputter {

	protected DecimalFormat df;
	protected String filePath;
	protected String similarityName;
	
	/**
	 * Instantiates a new CSV outputter.
	 *
	 * @param filePath the file path
	 * @param descriptor the descriptor
	 * @param subfolderClasses the subfolder classes
	 */
	public SimilarityMatrixOutputter(String filePath, String similarityName) {
		this.filePath = filePath;
		this.similarityName = similarityName;
		
		this.df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		this.df.setMinimumFractionDigits(4);
		this.df.setMaximumFractionDigits(4);
		this.df.setMinimumIntegerDigits(1);
		this.df.setMaximumIntegerDigits(3);
	}
	
	/**
	 * Gets the file.
	 *
	 * @param className the class name
	 * @param filename the filename
	 * @return the file
	 */
	public File getFile(String filename) {
		return Paths.get(filePath, similarityName +"_"+ filename).toFile();
	}
	
	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.output.OutputterAdapter#write(java.lang.String, java.util.List, java.util.List, boolean).
	 * 
	 * @param filename
	 * @param trajectories
	 * @param movelets
	 * @param delayOutput
	 */
	public synchronized void write(String filename, List<MAT<?>> trajectories, double[][] scoreMatrix, Object... params) {
		
		BufferedWriter writer;

		try {
			File file = getFile(filename + ".csv");
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file, false));

			String header = "";
			List<String> tids = new ArrayList<>();
			trajectories.stream().sequential().forEach((t) -> tids.add(t.getTid() + ""));
			for (String s : tids)
				header += s + ",";
			header += "TIDs,label" + System.getProperty("line.separator");
			
			writer.write(header);
			
			for (int i = 0; i < trajectories.size(); i++) {
				String line = "";
				
				for (double n : scoreMatrix[i])
					line += this.df.format(n) + ",";
				
				line += trajectories.get(i).getTid() +","+"\"" + trajectories.get(i).getMovingObject() + "\""+ System.getProperty("line.separator");
				
				writer.write(line);
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
