package br.com.tarlis.mov3lets.method.structures.descriptor;

import java.util.List;

public class LoaderDescriptor {

	private List<String> train = null;
	private List<String> test = null;
	private String format = null; // Cached ZIP: CZIP (default) | Comma separated: CSV
	private String loader = null; // default or null: interning | indexed
	
	public List<String> getTrain() {
		return train;
	}
	
	public void setTrain(List<String> train) {
		this.train = train;
	}
	
	public List<String> getTest() {
		return test;
	}
	
	public void setTest(List<String> test) {
		this.test = test;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getLoader() {
		return loader;
	}
	public void setLoader(String loader) {
		this.loader = loader;
	}
	
}