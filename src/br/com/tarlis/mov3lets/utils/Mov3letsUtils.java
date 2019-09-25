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
package br.com.tarlis.mov3lets.utils;

import java.util.HashMap;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Mov3letsUtils {
	
	private static Mov3letsUtils instance = null;
	
	private HashMap<String, Long> timers = new HashMap<String, Long>();
	
	private Mov3letsUtils() {
		
	}
	
	/**
	 * @return the instance
	 */
	public synchronized static Mov3letsUtils getInstance() {
		if (instance == null)
			instance = new Mov3letsUtils();
		return instance;
	}
	
	/**
	 * 
	 */
	public synchronized void startTimer(String timer) {
		timers.put(timer, System.nanoTime());
	}
	
	/**
	 * 
	 */
	public synchronized long stopTimer(String timer) {
		if (timers.containsKey(timer)) {
			long time = (System.nanoTime() - timers.get(timer));
			printTimer(timer, time);
			return time;
		} else
			return 0L;
	}
	
	/**
	 * 
	 */
	public synchronized void printTimer(String timer) {
		if (timers.containsKey(timer))
			printTimer(timer, (System.nanoTime() - timers.get(timer)));
		else
			trace("No timer found: " + timer);
	}
	
	public synchronized void printTimer(String timer, long time) {
		trace(timer + ": " + time/1000000);
	}
	
	public synchronized static void trace(String s) {
		System.out.println(s);
	}
	
	public synchronized static void traceW(String s) {
		trace("Warning: " + s);
	}
	
	public synchronized static void traceE(String s, Exception e) {
		System.err.println("Error: " + s);
		e.printStackTrace();
	}

}
