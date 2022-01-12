/*
 * This file is part of the UEA Time Series Machine Learning (TSML) toolbox.
 *
 * The UEA TSML toolbox is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * The UEA TSML toolbox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with the UEA TSML toolbox. If not, see <https://www.gnu.org/licenses/>.
 */
 
package br.ufsc.mov3lets.method.discovery.structures;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import br.ufsc.mov3lets.utils.Mov3letsUtils;

/**
 * Interface that allows the user to impose a train time contract of a classifier that
    implements this interface

known classifiers: ShapeletTransformClassifier, RISE, HIVE_COTE (partial),
* BOSS, TSF , ContractRotationForest
 *
 * ********************************NOTES********************************
 * 1) contract time of <=0 means no contract has been set, even if this is potentially contractable
 *
 */
public class TimeContract implements Runnable {

	protected long timeLimit = 0;
	
	public TimeContract(String param) {
		long time = Long.valueOf(param
				.substring(0, param.length()-1));
		switch (param.charAt(param.length()-1)) {

		case 'w':
			time *= 7;
		case 'd':
			time *= 24;
		case 'h':
			time *= 60;
		case 'm':
			time *= 60;
			break;
			
//		case 's': // Its the default unit
//		default:
//			break;
		}
		setTimeLimit(time * 1000);
	}
	
	public TimeContract(long amount, TimeUnit time) {
		setTimeLimit(time, amount);
	}
	
    /**
     * This is the single method that must be implemented to store the contract time
      * @param time in milliseconds
     */
    public void setTimeLimit(long time) {
    	this.timeLimit = time;
    }

    /**
     * Are we still within contract?
     * @param start start time
     * @return true if it is within the time contract, false otherwise.
     */
    public boolean withinTrainContract(long start) {
    	return this.timeLimit > start;
    }

    public void setOneDayLimit(){ setTimeLimit(TimeUnit.DAYS, 1); }
    public void setOneHourLimit(){ setTimeLimit(TimeUnit.HOURS, 1); }
    public void setOneMinuteLimit(){ setTimeLimit(TimeUnit.MINUTES, 1); }
    public void setDayLimit(int t){ setTimeLimit(TimeUnit.DAYS, t); }
    public void setHourLimit(int t){ setTimeLimit(TimeUnit.HOURS, t); }
    public void setMinuteLimit(int t){ setTimeLimit(TimeUnit.MINUTES, t); }

    //pass in an value from the TimeUnit enum and the amount of said values.
    public void setTimeLimit(TimeUnit time, long amount) {
        setTimeLimit(TimeUnit.MILLISECONDS.convert(amount, time));
    }

    public void setTimeLimit(long amount, TimeUnit time) {
        setTimeLimit(time, amount);
    }
    
    public long getContractTimeMilliseconds() {
    	return this.timeLimit;
    }
    
    private Thread thread;
	private ExecutorService executor;
    public void start() {
    	this.thread = new Thread(this);
    	this.thread.start();
	}
    
    public void start(ExecutorService executor) {
    	this.executor = executor;
    	start();
	}
    
    public void stop() {
    	try {
    		this.thread.interrupt();
    	} catch (Exception e) {}
	}

	@Override
	public void run() {
		try {
            Thread.sleep(getContractTimeMilliseconds());
    		Mov3letsUtils.trace("");
    		Mov3letsUtils.trace("[Warning] Time contract limit timeout.");
    		Mov3letsUtils.getInstance().stopTimer("[3] >> Processing time");
    		Mov3letsUtils.getInstance().ending();
    		if (executor != null)
    			executor.shutdownNow();
    		else
    			System.exit(0);
        } catch (InterruptedException e) {}
	}
	
	@Override
	public String toString() {
		return getContractTimeMilliseconds() + " ms";
	}
}
