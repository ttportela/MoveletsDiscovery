package br.com.tarlis.mov3lets.utils;

public class StatusBar {
	private int progress = -1;
	private long total = 0;
	private long done = 0;
	private String mark = null;

    /**
     * initialize progress bar properties.
     */
    public StatusBar() { }
    
    public StatusBar(String name, long total) { 
    	this.mark = name;
    	this.total = total;
    }    

    public synchronized void plus() {
    	update(done+1, this.total);
    }

	public synchronized void plus(long size) {
    	update((done+size), this.total);
	}

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     *
     * @param done an int representing the work done so far
     * @param total an int representing the total work
     */
    public synchronized void update(long done) {
    	update(done, this.total);
    }

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     *
     * @param done an int representing the work done so far
     * @param total an int representing the total work
     */
    public synchronized void update(long done, long total) {
    	this.done = done;
    	if (this.progress == -1) {
    		start();
    	}

    	long percent = (++this.done * 100) / total;
    	int extrachars = (int) ((percent / 2) - this.progress);
        
        this.progress += extrachars;

        if (extrachars > 0 && !printStep())
	        while (extrachars-- > 0) {
	        	System.out.print(mark);
	        }

        if (this.done == total) {
            stop();
        }
    }

    private synchronized boolean printStep() {
		switch (this.progress*2) {
		case 10:
			System.out.print("10%"); return true;
		case 20:
			System.out.print("20%"); return true;
		case 30:
			System.out.print("30%"); return true;
		case 40:
			System.out.print("40%"); return true;
		case 50:
			System.out.print("50%"); return true;
		case 60:
			System.out.print("60%"); return true;
		case 70:
			System.out.print("70%"); return true;
		case 80:
			System.out.print("80%"); return true;
		case 90:
			System.out.print("90%"); return true;
		}
		return false;
	}

	private synchronized void start() {
    	this.progress = 0;
    	if (mark != null) {
    		System.out.print(mark + ": [");
    		mark = "-";
    	} else
    		System.out.print("[");
    		
    }

    private synchronized void stop() {
    	System.out.print("] 100%");
        System.out.flush();
        System.out.println();
    }
}