package br.ufsc.mov3lets.method.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import br.ufsc.mov3lets.model.MAT;

public abstract class Sampler<MO> {
	
	protected List<MO> classes;
	protected List<List<MAT<MO>>> classBuckets;

	protected Random random;
	protected int count;
	protected int totalCount;
	
	public Sampler(List<MAT<MO>> data) {
		this.classes = data.stream().map(e -> (MO) e.getMovingObject()).distinct().collect(Collectors.toList());
		
		this.classBuckets = new ArrayList<List<MAT<MO>>>();
		for (MO mo : this.classes) {
			this.classBuckets.add(
					data.stream().filter(e-> mo.equals(e.getMovingObject())).collect(Collectors.toList())
			);
		}
	}
	
	public abstract MAT<MO> next();

	public boolean hasNext() {
        return count < totalCount;
    }
}
