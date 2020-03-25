package br.com.tarlis.mov3lets.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Ascii progress meter. On completion this will reset itself,
 * so it can be reused
 * <br /><br />
 * 100% ################################################## |
 */
public class ProgressBar {
    private StringBuilder progress;
    private String prefix = "";
	private long total = 0;
	private long done = 0;
	private char control = '\r';

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
    	update(0, total);
    }    

    public synchronized void plus() {
    	this.done++;
    	update(done, this.total);
    }

	public synchronized void plus(long size) {
    	this.done += size;
    	update(this.done, this.total);
	}

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     *
     * @param done an int representing the work done so far
     * @param total an int representing the total work
     */
    public synchronized void update(long done, long total) {
        char[] workchars = {'|', '/', '-', '\\'};
        String format = "\r%s: %c [%s%s] %3d%%";

        int percent = (int) ((++done * 100) / total);
        int extrachars = (percent / 2) - this.progress.length();

        while (extrachars-- > 0) {
            progress.append('\u2588');
        }
//        System.out.println();
        System.out.printf(format, prefix, workchars[(int) (done % workchars.length)], 
        		progress, StringUtils.repeat(' ', 50 - progress.length()), percent);

        if (done == total) {
            System.out.flush();
            System.out.println();
            init();
        }
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
}