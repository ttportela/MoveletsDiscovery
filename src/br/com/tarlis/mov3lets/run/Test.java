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

/**
 * @author Tarlis Portela <tarlis@tarlis.com.br>
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] arg) throws Exception {
//		BufferedReader reader1 = new BufferedReader(new FileReader("data/MM_output.txt"));
//		
//		BufferedReader reader2 = new BufferedReader(new FileReader("data/M3_output.txt"));
//		
//		String line1 = reader1.readLine();
//		
//		String line2 = reader2.readLine();
//		
//		boolean areEqual = true;
//		
//		int lineNum = 1;
//		
//		while (line1 != null || line2 != null)
//		{
//			if(line1 == null || line2 == null)
//			{
//				areEqual = false;
//				
//				break;
//			}
//			else if(! line1.equalsIgnoreCase(line2))
//			{
//				areEqual = false;
//				
////				break;
//			}
//			
//			if(!areEqual) {
//				System.out.println("Diff Line-"+lineNum + " at " + indexOfDifference(line1, line2));
//				System.out.println("\t"+line1);
//				System.out.println("\t"+line2);
//			}
//			
//			line1 = reader1.readLine();
//			
//			line2 = reader2.readLine();
//
//			areEqual = true;
//			lineNum++;
//		}
//		
//		System.out.println("Last line: " + lineNum);
//		
//		reader1.close();
//		
//		reader2.close();
		
		double qDiff = (int) ((0.011516314779270634 - 0.00980392156862745) * 1000);
		System.out.println(qDiff);
		qDiff = ( qDiff ) / 1000.0;
		
		System.out.println(qDiff);
	}

	static int INDEX_NOT_FOUND = -1;
	public static String difference(String str1, String str2) {
	    if (str1 == null) {
	        return str2;
	    }
	    if (str2 == null) {
	        return str1;
	    }
	    int at = indexOfDifference(str1, str2);
	    if (at == INDEX_NOT_FOUND) {
	        return null;
	    }
	    return str2.substring(at);
	}

	public static int indexOfDifference(CharSequence cs1, CharSequence cs2) {
	    if (cs1 == cs2) {
	        return INDEX_NOT_FOUND;
	    }
	    if (cs1 == null || cs2 == null) {
	        return 0;
	    }
	    int i;
	    for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
	        if (cs1.charAt(i) != cs2.charAt(i)) {
	            break;
	        }
	    }
	    if (i < cs2.length() || i < cs1.length()) {
	        return i;
	    }
	    return INDEX_NOT_FOUND;
	}
	
}