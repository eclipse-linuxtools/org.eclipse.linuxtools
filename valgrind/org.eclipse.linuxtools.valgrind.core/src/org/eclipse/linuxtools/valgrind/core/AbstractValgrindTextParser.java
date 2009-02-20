/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core;

import java.io.IOException;

import org.eclipse.osgi.util.NLS;

public class AbstractValgrindTextParser {

	private static final String DOT = "."; //$NON-NLS-1$

	public AbstractValgrindTextParser() {
		super();
	}

	/**
	 * Retrieves ARGUMENT portion of [OPTION][DELIMITER][ARGUMENT]
	 * where ARGUMENT is a Long
	 * @param line - the line to parse
	 * @param delim - the DELIMITER to separate on
	 * @return Long value of ARGUMENT
	 * @throws IOException
	 */
	protected Long parseLongValue(String line, String delim)
	throws IOException {
		Long result = null;
		String[] parts = line.split(delim, 2);
		if (parts.length > 1 && isNumber(parts[1])) {
			result = Long.parseLong(parts[1]);
		}
		else {
			fail(line);
		}
		return result;
	}

	/**
	 * Retrieves ARGUMENT portion of [OPTION][DELIMITER][ARGUMENT]
	 * where ARGUMENT is a String
	 * @param line - the line to parse
	 * @param delim - the DELIMITER to separate fields
	 * @return String value of ARGUMENT
	 * @throws IOException
	 */
	protected String parseStrValue(String line, String delim)
	throws IOException {
		String result = null;
		String[] parts = line.split(delim, 2);
		if (parts.length > 1) {
			result = parts[1];
		}
		else {
			fail(line);
		}
		return result;
	}
	
	/**
	 * Retrieves PID from filename with format [PREFIX][PID].[EXTENSION]
	 * @param filename - the file name to parse
	 * @param prefix - the prefix of the filename up to the PID
	 * @return - the PID portion of the filename as an Integer
	 * @throws IOException
	 */
	protected Integer parsePID(String filename, String prefix) throws IOException {
		String pidstr = filename.substring(prefix.length(), filename.lastIndexOf(DOT));
		if (isNumber(pidstr)) {
			return new Integer(pidstr);
		}
		else {
			throw new IOException("Cannot parse PID from output file"); //$NON-NLS-1$
		}
	}

	/**
	 * Throws an IOException indicating parsing failed on a given line
	 * @param line - line that parsing failed
	 * @throws IOException
	 */
	protected void fail(String line) throws IOException {
		throw new IOException(NLS.bind(Messages.getString("AbstractValgrindTextParser.Parsing_output_failed"), line)); //$NON-NLS-1$
	}

	/**
	 * Determines if argument is a number
	 * @param string - argument to test
	 * @return - true if argument is a number
	 */
	protected boolean isNumber(String string) {
		boolean result = true;
		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isDigit(chars[i])) {
				result = false;
			}
		}
		return result;
	}	

}