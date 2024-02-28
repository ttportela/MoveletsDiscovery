package br.ufsc.mov3lets.utils.log;

import br.ufsc.mov3lets.utils.Mov3letsUtils;

public class SimpleOutput implements ProgressBar {
	
    /** The prefix. */
    protected String prefix = "";
    
    /** The total. */
    protected long total = 0;
    
    /** The done. */
    protected long done = 0;
    
    /**
     * initialize progress bar properties.
     */
    public SimpleOutput() {}
    
    /**
     * Instantiates a new progress bar.
     *
     * @param prefix the prefix
     */
    public SimpleOutput(String prefix) {
    	this.prefix = prefix;     
    } 
    
    /**
     * Instantiates a new progress bar.
     *
     * @param prefix the prefix
     * @param total the total
     */
    public SimpleOutput(String prefix, long total) {
    	this.prefix = prefix;
    	this.total = total;
    }    

    /**
     * Plus.
     */
    public void plus() {
    	this.done++;
    	update(done, this.total, "");
    }   

    /**
     * Plus.
     *
     * @param message the message
     */
    public void plus(String message) {
    	this.done++;
    	update(done, this.total, message);
    }

	/**
	 * Plus.
	 *
	 * @param size the size
	 */
	public void plus(long size) {
    	this.done += size;
    }

	/**
	 * Plus.
	 *
	 * @param size the size
	 * @param message the message
	 */
	public void plus(long size, String message) {
    	this.done += size;
    	update(this.done, this.total, message);
	}
	
    /**
     * Update.
     *
     * @param done the done
     * @param total the total
     */
    public void update(long done, long total) {
    	this.done = done;
    	this.total = total;
    }

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     *
     * @param done an int representing the work done so far
     * @param total an int representing the total work
     * @param message the message
     */
    public synchronized void update(long done, long total, String message) {
    	int percent = (int) ((done * 100) / total);
        message = message.trim(); message = message.endsWith(".")? message : message + ".";
        Mov3letsUtils.trace(this.prefix + ": ["+percent+"%] "+message);
    }

	/**
	 * Trace.
	 *
	 * @param message the message
	 */
	public void trace(String message) {
    	update(this.done, this.total, message);
	}
    
    /**
     * Sets the inline.
     *
     * @param inline the new inline
     */
//    public void setInline(boolean inline) {}
    
    /**
     * Sets the prefix.
     *
     * @param prefix the new prefix
     */
    public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
    
    /**
     * Sets the total.
     *
     * @param total the new total
     */
    public void setTotal(long total) {
		this.total = total;
	}
    
    public void reset(long total) {
		this.total = total;
		this.done = 0;
	}
}