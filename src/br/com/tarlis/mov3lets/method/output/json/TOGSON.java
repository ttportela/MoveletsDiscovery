package br.com.tarlis.mov3lets.method.output.json;

import java.util.List;
import java.util.Map;

public class TOGSON {
	
	private List<Map<String,Object>> classes;
	
	private List <SubtrajectoryGSON> movelets;
	
	public TOGSON() {
	}

	public TOGSON(List<Map<String, Object>> classes, List<SubtrajectoryGSON> movelets) {
		super();
		this.classes = classes;
		this.movelets = movelets;
	}

	public void setClasses(List<Map<String, Object>> classes) {
		this.classes = classes;
	}
	
	public void setShapelets(List<SubtrajectoryGSON> movelets) {
		this.movelets = movelets;
	}
	
	public List<SubtrajectoryGSON> getShapelets() {
		return movelets;
	}
	
	
	public List<Map<String, Object>> getClasses() {
		return classes;
	}
}
