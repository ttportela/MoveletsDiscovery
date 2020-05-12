/**
 * 
 */
package br.com.tarlis.mov3lets.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * @author tarlis
 *
 */
public abstract class LoggerAdapter {
	
	public abstract void trace(String s);
	
	public synchronized void printTimer(String timer, long time) {
		trace(timer + ": " + time + " milliseconds");
	}
	
	public synchronized void traceW(String s) {
		trace("Warning: " + s);
	}
	
	public synchronized void traceE(String s, Exception e) {
		trace("\n[Error] " + s);
		String stacktrace = ExceptionUtils.getStackTrace(e);
        trace(stacktrace);
		e.printStackTrace();
	}

	public void printMemory() {
		System.gc();
		Runtime rt = Runtime.getRuntime();
		double total = rt.totalMemory() / (1024.0 * 1024.0);
		double free = rt.freeMemory() / (1024.0 * 1024.0);
        double used = (total - free);
        
		trace("Memory Usage (MiB), Memory Total: "+total+". Memory Free: "+free+". Memory Used: "+used+".");
	}

}
