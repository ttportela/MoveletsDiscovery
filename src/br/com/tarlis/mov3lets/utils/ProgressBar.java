package br.com.tarlis.mov3lets.utils;

import java.nio.CharBuffer;

import org.apache.commons.lang3.StringUtils;

/**
 * Ascii progress meter. On completion this will reset itself,
 * so it can be reused
 * <br /><br />
 * 100% ################################################## |
 */
public class ProgressBar {
    protected StringBuilder progress;
    protected String prefix = "";
    protected long total = 0;
    protected long done = 0;
    protected char control = '\r';

    /**
     * initialize progress bar properties.
     */
    public ProgressBar() {
        init();
    }
    
    public ProgressBar(String prefix) {
    	this.prefix = prefix;
    	init();        
    } 
    
    public ProgressBar(String prefix, long total) {
    	this.prefix = prefix;
    	this.total = total;
    	init();
    	update(0, total, null);
    }    

    public void plus() {
    	this.done++;
    	update(done, this.total, null);
    }   

    public void plus(String message) {
    	this.done++;
    	update(done, this.total, message);
    }

	public void plus(long size) {
    	this.done += size;
//    	update(this.done, this.total, null);
	}

	public void plus(long size, String message) {
    	this.done += size;
    	update(this.done, this.total, message);
	}
	
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
     */
    public synchronized void update(long done, long total, String message) {
        char[] workchars = {'|', '/', '-', '\\'};
        String format = this.control + "%s: %c [%s%s] %3d%% %s";
        message = (message != null? ">> "+message + "." : "");
        message += CharBuffer.allocate( 250 - message.length() ).toString().replace( '\0', ' ' );

        int percent = (int) ((++done * 100) / total);
        int extrachars = (percent / 2) - this.progress.length();

        while (extrachars-- > 0) {
            progress.append('\u2588');
        }

        System.out.printf(format, prefix, workchars[(int) (done % workchars.length)], 
        		progress, StringUtils.repeat(' ', 50 - progress.length()), percent,
        		message);

    }

	public void trace(String message) {
    	update(this.done, this.total, message);
	}
    
    public void setInline(boolean inline) {
    	if (inline)
    		this.control = '\r';
    	else
    		this.control = ' ';
	}

    private void init() {
        this.progress = new StringBuilder(60);
    }
    
    public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
    
    public void setTotal(long total) {
		this.total = total;
	}
}