package br.ufsc.mov3lets.method.qualitymeasure;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class LeftSidePureQuality.
 */
public class LeftSidePureQuality extends Quality {

	/** The data. */
	private Map<String, Double> data;
	
	/**
	 * Instantiates a new left side pure quality.
	 */
	public LeftSidePureQuality() {
		data = new HashMap<>();
	}
	
	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.qualitymeasure.Quality#getData().
	 * 
	 * @return
	 */
	public Map<String, Double> getData() {
		return data;
	}
	
	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.qualitymeasure.Quality#setData(java.util.Map).
	 * 
	 * @param data
	 */
	@Override
	public void setData(Map<String, Double> data) {
		// TODO Auto-generated method stub
		this.data = data;
	}
	
	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.qualitymeasure.Quality#compareTo(br.com.tarlis.mov3lets.method.qualitymeasure.Quality).
	 * 
	 * @param other
	 * @return
	 */
	@Override
	public int compareTo(Quality other) {
				
		// First it compares the quality score		
		int quality = Double.compare(this.getData().get("quality"), other.getData().get("quality"));
		if (quality != 0)
			// in descending order (high value first)
			return (-1) * quality;
		
		// It continues if both split distance are the same
		// Second it compares the movelet size
		int diffSize = Double.compare(this.getData().get("size"), other.getData().get("size"));
		if (diffSize != 0)
			// in descending order
			return  (-1) * diffSize;

		// It continues if both sizes are the same
		// Third it compares the movelet number of attributes
		int diffDimensions = Double.compare(this.getData().get("dimensions"), other.getData().get("dimensions"));
		if (diffDimensions != 0)
			// in descending order
			return  (-1) * diffDimensions;

		// Fourth it compares the movelet start point
		int diffStart = Double.compare(this.getData().get("start"), other.getData().get("start"));
		if (diffStart != 0)	
			// in ascending order (lesser first)
			return diffStart;
		
		int diffTid = Double.compare(this.getData().get("tid"), other.getData().get("tid"));
		if (diffTid != 0)	
			// in ascending order
			return diffTid;
				
				
		// else, if they are equal				
		return 0;
	}

	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.qualitymeasure.Quality#hasZeroQuality().
	 * 
	 * @return
	 */
	@Override
	public boolean hasZeroQuality() {		
		
		return (data.get("quality") <= 0);
		
	}

}
