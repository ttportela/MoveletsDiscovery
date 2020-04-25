package br.com.tarlis.mov3lets.method.qualitymeasure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.tarlis.mov3lets.model.MAT;

public class ProportionQuality extends Quality {
	
	private List<MAT<?>> coveredInClass;
	private Map<String, Double> data;
	
	public ProportionQuality() {
		data = new HashMap<>();
	}

	@Override
	public Map<String, Double> getData() {
		return data;
	}
	
	@Override
	public void setData(Map<String, Double> data) {
		this.data = data;
	}
	
	@Override
	public int compareTo(Quality other) {
				
		String measure = this.getData().containsKey("proportion")? "proportion" : "quality";
		
		// First it compares the quality score		
		int quality = Double.compare(this.getData().get(measure), other.getData().get(measure));
		if (quality != 0)
			// in descending order
			return (-1) * quality;
		
		// It continues if both split distance are the same
		// Fourth it compares the movelet size
		int diffSize = Double.compare(this.getData().get("size"), other.getData().get("size"));
		if (diffSize != 0)
			// in ascending order
			return  diffSize;

		// It continues if both sizes are the same
		// Fourth it compares the movelet size
		int diffDimensions = Double.compare(this.getData().get("dimensions"), other.getData().get("dimensions"));
		if (diffDimensions != 0)
			// in ascending order
			return  diffDimensions;
		
		int diffCoverage = Integer.compare(this.getCoveredInClass().size(), ((ProportionQuality)other).getCoveredInClass().size());
		if (diffCoverage != 0)	
		// Ordem crescente
		return diffCoverage;
				
		int diffStart = Double.compare(this.getData().get("start"), other.getData().get("start"));
		if (diffStart != 0)	
		// Ordem crescente
		return diffStart;
		
		int diffTid = Double.compare(this.getData().get("tid"), other.getData().get("tid"));
		if (diffTid != 0)	
		// Ordem crescente
		return diffTid;
				
				
		// else, se for igual
		// Segundo criterio
		return 0;
	}

	public double getValue() {
		return data.get( this.getData().containsKey("proportion")? "proportion" : "quality" );
	}
	
	@Override
	public boolean hasZeroQuality() {		
		return (getValue() == 0);
	}
	
	public boolean hasHalfQuality() {		
		return (getValue() > 0);
	}
	
	@Override
	public String toString() {
		return data.toString();
	}
	
	public List<MAT<?>> getCoveredInClass() {
		return coveredInClass;
	}
	
	public void setCoveredInClass(List<MAT<?>> coveredInClass) {
		this.coveredInClass = coveredInClass;
	}

}
