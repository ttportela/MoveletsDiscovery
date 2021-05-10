package br.ufsc.mov3lets.model;

import java.util.ArrayList;

public class MemPoint extends Point {
	
	private long id;
	private int ci = -1;
	private int ri = -1;
	private ArrayList relations = null;
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public int getCi() {
		return ci;
	}
	
	public void setCi(int ci) {
		this.ci = ci;
	}
	
	public int getRi() {
		return ri;
	}
	
	public void setRi(int ri) {
		this.ri = ri;
	}

	public ArrayList getRelations() {
		return relations;
	}
	
	public void setRelations(ArrayList relations) {
		this.relations = relations;
	}
}
