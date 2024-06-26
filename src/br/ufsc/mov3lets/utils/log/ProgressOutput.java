package br.ufsc.mov3lets.utils.log;

import java.nio.CharBuffer;

import org.apache.commons.lang3.StringUtils;

public class ProgressOutput implements ProgressBar {
    
    /** The progress. */
    protected StringBuilder progress;
    
    /** The prefix. */
    protected String prefix = "";
    
    /** The total. */
    protected long total = 0;
    
    /** The done. */
    protected long done = 0;
    
    /** The control. */
    protected char control = '\r';
    
    private int size = 300;

    /**
     * initialize progress bar properties.
     */
    public ProgressOutput() {
        init();
    }
    
    /**
     * Instantiates a new progress bar.
     *
     * @param prefix the prefix
     */
    public ProgressOutput(String prefix) {
    	this.prefix = prefix;
    	init();        
    } 
    
    /**
     * Instantiates a new progress bar.
     *
     * @param prefix the prefix
     * @param total the total
     */
    public ProgressOutput(String prefix, long total) {
    	this.prefix = prefix;
    	this.total = total;
    	init();
    	update(0, total, null);
    }    

    /**
     * Plus.
     */
    public void plus() {
    	this.done++;
    	update(done, this.total, null);
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
//    	update(this.done, this.total, null);
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
//    	update(done, total, null);
    }

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     *
     * @param done an int representing the work done so far
     * @param total an int representing the total work
     * @param message the message
     */
    public synchronized void update(long done, long total, String message) { // TODO optimize
        char[] workchars = {'|', '/', '-', '\\'};
        String format = this.control + "%s: %c [%s%s] %3d%% %s";
        message = (message != null? ">> "+message : "");
        if (size - message.length() < 1) size += message.length() - size + 1;
        message = message.trim(); message = message.endsWith(".")? message : message + ".";
        message += CharBuffer.allocate( size - message.length() ).toString().replace( '\0', ' ' );

        int percent = (int) ((done * 100) / total);
        int extrachars = (percent / 2) - this.progress.length();

        while (extrachars-- > 0) {
            progress.append('\u2588');
        }

        System.out.printf(format, prefix, workchars[(int) (done % workchars.length)], 
        		progress, StringUtils.repeat(' ', 50 - progress.length()), percent,
        		message);

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
    public void setInline(boolean inline) {
    	if (inline)
    		this.control = '\r';
    	else
    		this.control = ' ';
	}

    /**
     * Inits the.
     */
    private void init() {
        this.progress = new StringBuilder(60);
    }
    
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
		init();
	}
}