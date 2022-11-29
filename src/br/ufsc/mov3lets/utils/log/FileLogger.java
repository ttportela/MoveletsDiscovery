/**
 * 
 */
package br.ufsc.mov3lets.utils.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author tarlisportela
 *
 */
public class FileLogger extends LoggerAdapter {
	
	PrintWriter writer;
	
	public FileLogger(File file) throws IOException {
		super();
		file.getParentFile().mkdirs();
		writer = new PrintWriter(new FileWriter(file), true);
	}

	@Override
	public void trace(String s) {
		writer.write(s + "\n");
	}

	public void end() {
		writer.close();
	}

}
