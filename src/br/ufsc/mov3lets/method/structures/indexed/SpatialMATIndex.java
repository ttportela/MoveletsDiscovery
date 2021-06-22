package br.ufsc.mov3lets.method.structures.indexed;

import java.text.DecimalFormat;
import java.util.BitSet;

import br.ufsc.mov3lets.model.aspect.Space2DAspect;

//------------------------------
//Spatial ----------------------
//------------------------------
public class SpatialMATIndex extends MATIndex<String, Space2DAspect> {

	public int aId;

	public double cellSizeSpace;
	
	public static int[][] SPATIAL_ADJACENTS = { 
			{ -2, -2 }, { -2, -1 }, { -2, 0 }, { -2, 1 }, { -2, 2 }, 
            { -1, -2 }, { -1, -1 }, { -1, 0 }, { -1, 1 }, { -2, 2 }, 
	        {  0, -2 }, {  0, -1 },            {  0, 1 }, { -2, 2 }, 
	        {  1, -2 }, {  1, -1 }, {  1, 0 }, {  1, 1 }, { -2, 2 }, 
	        {  2, -2 }, {  2, -1 }, {  2, 0 }, {  2, 1 }, { -2, 2 } 
	};
	
	public SpatialMATIndex(double spatialThreshold, int aId) {
		this.aId = aId;
		// Para encontrar o tamanho da celula quadrada basta assumir sua diagonal igual
		// ao threshold
		// Assim, teremos: Threshold = cellSize x sqrt(2)
		DecimalFormat df = new DecimalFormat("###.######");
		cellSizeSpace = Double.parseDouble(df.format(spatialThreshold * 0.7071).replace(",", "."));
	}

//	public Map<String, BitSet> mSpatialIndex = new HashMap<String, BitSet>();
	
	public void addToIndex(Space2DAspect coordinates, int rId) {

		// x,y
		String key = getCellPosition( coordinates.getX(), coordinates.getY() );

		BitSet rIds = mIndex.get(key);

		if (rIds == null) {
			rIds = new BitSet();
			rIds.set(rId);
			mIndex.put(key, rIds);
		} else {
			rIds.set(rId);
			mIndex.replace(key, rIds);
		}

	}

	public String getCellPosition(Double x, Double y) {
		return ( (int) Math.floor(x / cellSizeSpace) ) + "," + ((int) Math.floor(y / cellSizeSpace));
	}
	
}
