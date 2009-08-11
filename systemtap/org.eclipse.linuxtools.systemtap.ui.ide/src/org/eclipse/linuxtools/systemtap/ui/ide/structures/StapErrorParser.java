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

package org.eclipse.linuxtools.systemtap.ui.ide.structures;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.IErrorParser;


/**
 * Parses the output from the stap command into a table of strings that are displayed
 * in the error log.
 * @author Ryan Morse
 */
public final class StapErrorParser implements IErrorParser {
	/**
	 * Parses the error log passed in into a table of strings.
	 * @param output The output from the stderr StreamGobbler.
	 * @return A table of strings in the proper format to be displayed in the error log.  
	 */
	public String[][] parseOutput(String output) {
		String[][] sErrors = null;
		ArrayList<String[]> errors = new ArrayList<String[]>();
		int errorType = TYPE;
		
		if(null != output) {
			String[] tokens = output.split("\\s");
			String[] row = null;
			
			for(int i=0; i<tokens.length; i++) {
				if(tokens[i].equals("error:")) {
					row = new String[] {"", "", "", ""};
					errors.add(row);
					row[TYPE] = tokens[i-1] + " " + tokens[i];
					errorType = ERROR;
					i++;
				} else if(tokens[i].equals("saw:")) {
					errorType = SAW;
					i++;
				} else if(tokens[i].equals("at")) {
					errorType = LOCATION;
					i++;
				} else if(tokens[i].equals("Pass")) {
					errorType = PASS;
					
				}
				
				if(null != row && errorType != PASS)
					row[errorType] += tokens[i] + " ";
			}

			sErrors = new String[errors.size()][4];
			System.arraycopy(errors.toArray(), 0, sErrors, 0, errors.size());
			
			for(int i=0; i<sErrors.length; i++)
				sErrors[i][LOCATION] = fixLocation(sErrors[i][LOCATION]);
		}
		
		return sErrors;
	}

	private static String fixLocation(String loc) {
		if(loc.contains(":")) {
			loc = loc.substring(loc.indexOf(':')+1, loc.lastIndexOf(':'));
			return loc;
		} else
			return "";
	}
	
	private static final int TYPE		= 0;
	private static final int ERROR		= 1;
	private static final int SAW		= 2;
	private static final int LOCATION	= 3;
	private static final int PASS		= 4;
}
