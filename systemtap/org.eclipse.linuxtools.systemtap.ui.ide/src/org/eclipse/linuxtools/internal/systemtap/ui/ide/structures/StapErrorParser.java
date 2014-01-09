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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

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
	@Override
	public String[][] parseOutput(String output) {
		String[][] sErrors = null;
		ArrayList<String[]> errors = new ArrayList<>();
		int errorType = TYPE;

		if(null != output) {
			String[] tokens = output.split("\\s"); //$NON-NLS-1$
			String[] row = null;

			for(int i=0; i<tokens.length; i++) {
				if(tokens[i].equals("error:")) { //$NON-NLS-1$
					row = new String[] {"", "", "", ""};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
					errors.add(row);
					row[TYPE] = tokens[i-1] + " " + tokens[i]; //$NON-NLS-1$
					errorType = ERROR;
					i++;
				} else if(tokens[i].equals("saw:")) { //$NON-NLS-1$
					errorType = SAW;
					i++;
				} else if(tokens[i].equals("at")) { //$NON-NLS-1$
					errorType = LOCATION;
					i++;
				} else if(tokens[i].equals("Pass")) { //$NON-NLS-1$
					errorType = PASS;

				}

				if (null != row && errorType != PASS) {
					row[errorType] += tokens[i] + " "; //$NON-NLS-1$
				}
			}

			sErrors = new String[errors.size()][4];
			System.arraycopy(errors.toArray(), 0, sErrors, 0, errors.size());

			for(int i=0; i<sErrors.length; i++)
				sErrors[i][LOCATION] = fixLocation(sErrors[i][LOCATION]);
		}

		return sErrors;
	}

	private static String fixLocation(String loc) {
		if(loc.contains(":")) { //$NON-NLS-1$
			loc = loc.substring(loc.indexOf(':')+1, loc.lastIndexOf(':'));
			return loc;
		} else {
			return ""; //$NON-NLS-1$
		}
	}

	private static final int TYPE		= 0;
	private static final int ERROR		= 1;
	private static final int SAW		= 2;
	private static final int LOCATION	= 3;
	private static final int PASS		= 4;
}
