package br.ufsc.mov3lets.utils.log;

import java.util.ArrayList;
import java.util.List;

public class MultipleOutput implements ProgressBar {
	
	private List<ProgressBar> bars = new ArrayList<>();
	
	public void add(ProgressBar bar) {
		bars.add(bar);
	}

	@Override
	public void plus(String message) {
		for (ProgressBar b : bars) {
			b.plus(message);
		}
	}

	@Override
	public void plus(long size, String message) {
		for (ProgressBar b : bars) {
			b.plus(size, message);
		}
	}

	@Override
	public void trace(String message) {
		for (ProgressBar b : bars) {
			b.trace(message);
		}
	}

	@Override
	public void setPrefix(String prefix) {
		for (ProgressBar b : bars) {
			b.setPrefix(prefix);
		}
	}

	@Override
	public void reset(long total) {
		for (ProgressBar b : bars) {
			b.reset(total);
		}
	}

}
