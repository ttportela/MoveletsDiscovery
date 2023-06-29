/**
 * Mov3lets - Multiple Aspect Trajectory (MASTER) Classification Version 3. 
 * Copyright (C) 2019  Tarlis Portela <tarlis@tarlis.com.br>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package br.ufsc.mov3lets.utils;

import java.util.HashMap;
import java.util.List;

import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.utils.log.LoggerAdapter;

/**
 * The Class Mov3letsUtils.
 *
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 */
public class Mov3letsUtils {
	
	/** The instance. */
	private static Mov3letsUtils instance = null;
	
	/** The timers. */
	private HashMap<String, Long> timers = new HashMap<String, Long>();
	
	// This configurations allows to create a logger in a file, 
	/** The log. */
	// just output to console, or anything:
	private LoggerAdapter log = null;
	
	/**
	 * Instantiates a new mov 3 lets utils.
	 */
	private Mov3letsUtils() {
		
	}

	/**
	 * Gets the logger.
	 *
	 */
	public LoggerAdapter getLogger() {
		return log;
	}
	
	/**
	 * Sets the logger.
	 *
	 * @param logger the new logger
	 */
	public void setLogger(LoggerAdapter logger) {
		this.log = logger;
	}
	
	/**
	 * Config logger.
	 */
	public void configLogger() {
		this.log = new LoggerAdapter() {
			@Override
			public void trace(String s) {
				System.out.println(s);
			}
		};
	}
	
	/**
	 * Gets the single instance of Mov3letsUtils.
	 *
	 * @return the instance
	 */
	public static Mov3letsUtils getInstance() {
		if (instance == null)
			instance = new Mov3letsUtils();
		return instance;
	}
	
	/**
	 * Start timer.
	 *
	 * @param timer the timer
	 */
	public void startTimer(String timer) {
		if (this.log != null)
	//		timers.put(timer, System.nanoTime());
			timers.put(timer, System.currentTimeMillis());
	//		this.log.trace("[Timer Set] " + timer);
	}
	
	/**
	 * Stop timer.
	 *
	 * @param timer the timer
	 * @return the long
	 */
	public long stopTimer(String timer) {
		if (this.log != null && timers.containsKey(timer)) {
//			long time = (System.nanoTime() - timers.get(timer));
			long time = (System.currentTimeMillis() - timers.get(timer));
			this.log.printTimer(timer, time);
			timers.remove(timer);
			return time;
		} else
			return 0L;
	}
	
	/**
	 * Trace.
	 *
	 * @param s the s
	 */
//	public void printTimer(String timer) {
//		if (this.log != null && timers.containsKey(timer))
//			this.log.printTimer(timer, (System.nanoTime() - timers.get(timer)));
//		else
//			this.log.trace("No timer found: " + timer);
//	}
	
	public static void trace(String s) {
		if (getInstance().log != null) getInstance().log.trace(s);
	}
	
	/**
	 * Trace W.
	 *
	 * @param s the s
	 */
	public static void traceW(String s) {
		if (getInstance().log != null) getInstance().log.traceW(s);
	}
	
	/**
	 * Trace E.
	 *
	 * @param s the s
	 * @param e the e
	 */
	public static void traceE(String s, Exception e) {
		if (getInstance().log != null) getInstance().log.traceE(s, e);
	}

	/**
	 * Prints the memory.
	 */
	public static void printMemory() {
		if (getInstance().log != null) getInstance().log.printMemory();
	}
	
	/**
	 * Total points.
	 *
	 * @param trajectories the trajectories
	 * @return the long
	 */
	public long totalPoints(List<MAT<?>> trajectories){
		long size = 0;
		
		for (MAT<?> T : trajectories) {
			size += T.getPoints().size();
		}
		
		return size;
	}

	/**
	 * Gets the used memory.
	 *
	 * @return the used memory
	 */
	public static double getUsedMemory() {
		Runtime rt = Runtime.getRuntime();
		double total = rt.totalMemory() / (1024.0 * 1024.0);
		double free = rt.freeMemory() / (1024.0 * 1024.0);
        return (total - free);
	}
	
	public void ending() {
		if (getInstance().log != null) getInstance().log.end();
	}

}
