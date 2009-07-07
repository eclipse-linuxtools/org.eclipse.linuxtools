/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.structures;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegExParser {
	
	/**
	 * Accepts a string of output from the script and compares it against the supplied regular
	 * expression. The output is split and returned as an array of objects.
	 * 
	 * @param s The line of output from the script.
	 * @param regEx The regular expression used in comparison.
	 * 
	 * @return The array of objects representing the parsed line.
	 */
	public static Object[] parseLine(StringBuilder s, String[] regEx) {
		Object[] d = null;
		
		StringBuilder wholeRegExpr = new StringBuilder();
		for(int i=0; i<regEx.length; i++) {
			regEx[i] = '(' + regEx[i] + ')';
			wholeRegExpr.append(regEx[i]);
		}
		Pattern wholePattern = Pattern.compile(wholeRegExpr.toString());
		Matcher wholeMatcher = wholePattern.matcher(s);

		if(wholeMatcher.find()) {
			d = new Object[regEx.length>>1];

			int group=0, j;
			
			for(int i=0; i<regEx.length; i++) {
				for(j=0; j<regEx[i].length(); j++)
					if(regEx[i].charAt(j) == ')')
						group++;
				
				if((i&1)==0)
					d[i>>1] = wholeMatcher.group(group);
			}
			s.delete(wholeMatcher.start(), wholeMatcher.end());
		}
		
		return d;
	}
}
