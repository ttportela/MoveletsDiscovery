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
package br.com.tarlis.mov3lets.run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] arg) throws Exception {
		List<Integer> numbers = new ArrayList<Integer>(
			    Arrays.asList(5,3,1,2,9,5,0,7,8)
			);
		
//		ProgressBar bar = new ProgressBar();
//
//        System.out.println("Process Starts Now!");
//
//        bar.update(0, 1000);
//        for(int i=0;i<1000;i++) {
//                        // do something!
//            for(int j=0;j<10000000;j++)
//                for(int p=0;p<10000000;p++);
//            // update the progress bar
//            bar.update(i, 1000);
//        }
//        System.out.println("Process Completed!");
		int N = numbers.size();
		int L = N/4;
		for (int i = 0; i < N; i += L) {			
			System.out.println(numbers.subList(i, Math.min(N, i + L)));
		}
    }

}

class ProgressBar {
//    private StringBuilder progress;
	private int progress = -1;
	private String mark = "-";

    /**
     * initialize progress bar properties.
     */
    public ProgressBar() { }

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     *
     * @param done an int representing the work done so far
     * @param total an int representing the total work
     */
    public void update(int done, int total) {
    	if (this.progress == -1) {
    		start();
    	}
//        char[] workchars = {'|', '/', '-', '\\'};
//        String format = "\r%3d%% %s %c";

        int percent = (++done * 100) / total;
        int extrachars = (percent / 2) - this.progress;
        
        this.progress += extrachars;

        if (extrachars > 0 && !printStep())
	        while (extrachars-- > 0) {
	        	System.out.print(mark);
	        }
//        while (extrachars-- > 0) {
//            progress.append('#');
//        }
        
//        System.out.printf(format, percent, progress,
//         workchars[done % workchars.length]);

        if (done == total) {
            stop();
        }
    }

    private boolean printStep() {
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

	private void start() {
    	this.progress = 0;
    	System.out.print("[");
//        this.progress = new StringBuilder(60);
    }

    private void stop() {
    	System.out.print("] 100%");
        System.out.flush();
        System.out.println();
//        init();
    }
}
