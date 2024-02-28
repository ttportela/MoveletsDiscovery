package br.ufsc.mov3lets.utils.log;

import br.ufsc.mov3lets.utils.Mov3letsUtils;

public class MemoryOutput implements ProgressBar {

    /** The prefix. */
    protected String prefix = "";
	protected long startTime = System.currentTimeMillis();
	
	public MemoryOutput() {
		this.prefix = "[Memory Usage (MiB)]";
	}
	
	public long elapsedTime() {
		return System.currentTimeMillis() - startTime;
	}
	
	public double totalMemory() {
		return Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0);
	}
	
	public double freeMemory() {
		return Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0);
	}

	@Override
	public void plus(String message) {
    	update();
	}

	@Override
	public void plus(long size, String message) {
		update();
	}

	@Override
	public void trace(String message) {}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void reset(long total) {
		System.gc();
		this.startTime = System.currentTimeMillis();
	}

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     *
     * @param done an int representing the work done so far
     * @param total an int representing the total work
     * @param message the message
     */
    public synchronized void update() {
		double total = totalMemory();
		double free = freeMemory();
        double used = (total - free);
        Mov3letsUtils.trace(this.prefix + " at: "+elapsedTime()+" ms; " +
        		"Memory Total: "+total+"; Memory Free: "+free+"; Memory Used: "+used+";");
    }

}
