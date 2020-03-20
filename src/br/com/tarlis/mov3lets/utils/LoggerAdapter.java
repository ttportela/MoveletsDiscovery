/**
 * 
 */
package br.com.tarlis.mov3lets.utils;

/**
 * @author tarlis
 *
 */
public abstract class LoggerAdapter {
	
	public abstract void trace(String s);
	
	public synchronized void printTimer(String timer, long time) {
		trace(timer + ": " + time/1000000.0 + "ms");
	}
	
	public synchronized void traceW(String s) {
		trace("Warning: " + s);
	}
	
	public synchronized void traceE(String s, Exception e) {
		System.err.println("Error: " + s);
		e.printStackTrace();
	}

	public void printMemory() {
		System.gc();
		Runtime rt = Runtime.getRuntime();
		double total = rt.totalMemory() / 1024.0;
		double free = rt.freeMemory() / 1024.0;
        double used = (total - free);
        
		trace(String.format("Memory usage (KiB), Memory Total: %.3f, Memory Free: %.3f, Memory Used: %.3f",
                    total,
                    free,
                    used));
	}

}
