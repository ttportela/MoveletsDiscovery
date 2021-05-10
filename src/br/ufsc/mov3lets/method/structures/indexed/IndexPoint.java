package br.ufsc.mov3lets.method.structures.indexed;

import java.util.BitSet;

import br.ufsc.mov3lets.model.Point;

public class IndexPoint {

	public double TP, FP, FN, TN;
	public double quality;
	public int rId;
	public int cId;
	public Point point;
	public String semanticCompositeKey;
	public BitSet spatialMatches;
	public BitSet temporalMatches;
	public int[][] matches; // matches[dimension][trajectory]

	public IndexPoint(int cId, int rId, Point point, int features) {
		this.cId = cId;
		this.rId = rId;
		this.point = point;
		this.matches = new int[features][];
	}

	public IndexPoint(int cId, int rId, Point point, String semanticCompositeKey) {
		this.cId = cId;
		this.rId = rId;
		this.point = point;
		this.semanticCompositeKey = semanticCompositeKey;
		this.spatialMatches = new BitSet();
		this.temporalMatches = new BitSet();
	}
	
	public double precision() {
		return TP / (TP + FP);
	}
	
	public double recall() {
		return TP / (TP + FN);
	}
	
	public double accuracy() {
		return (TP + TN) / (TP + TN + FP + FN);
	}
	
	public double fscore() {
		// F-Score, count in and out of class
		double P = precision(); // precision
		double R = recall(); // recall
		double fscore = 2.0 * ((P * R) / (P + R));
		if (Double.isNaN(fscore)) fscore = 0.0;
		return fscore;
	}
	
}
