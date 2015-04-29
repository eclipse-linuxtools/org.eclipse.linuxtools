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

import org.eclipse.linuxtools.internal.valgrind.core.Messages;
import org.eclipse.osgi.util.NLS;

/**
 * Class containing convenient methods common to Valgrind
 * parsers.
 */
public final class ValgrindParserUtils {
    private static final String DOT = "."; //$NON-NLS-1$
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * Retrieves ARGUMENT portion of [OPTION][DELIMITER][ARGUMENT]
     * where ARGUMENT is a Long
     * @param line - the line to parse
     * @param delim - the DELIMITER to separate on
     * @return Long value of ARGUMENT
     * @throws IOException If parsing failed.
     */
    public static Long parseLongValue(String line, String delim)
    throws IOException {
        Long result = null;
        String[] parts = line.split(delim, 2);
        if (parts.length > 1 && isNumber(parts[1])) {
            result = Long.parseLong(parts[1]);
        } else {
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
     * @throws IOException If parsing failed.
     */
    public static String parseStrValue(String line, String delim)
    throws IOException {
        String result = null;
        String[] parts = line.split(delim, 2);
        if (parts.length > 1) {
            result = parts[1];
        } else {
            fail(line);
        }
        return result;
    }

    /**
     * Retrieves PID from filename with format [PREFIX][PID].[EXTENSION]
     * @param filename - the file name to parse
     * @param prefix - the prefix of the filename up to the PID
     * @return - the PID portion of the filename as an Integer
     * @throws IOException If PID can not be parsed.
     */
    public static Integer parsePID(String filename, String prefix) throws IOException {
        String pidstr = filename.substring(prefix.length(), filename.lastIndexOf(DOT));
        if (isNumber(pidstr)) {
            return Integer.valueOf(pidstr);
        } else {
            throw new IOException("Cannot parse PID from output file"); //$NON-NLS-1$
        }
    }

    /**
     * Throws an IOException indicating parsing failed on a given line
     * @param line - line that parsing failed
     * @throws IOException If parsing failed.
     */
    public static void fail(String line) throws IOException {
        throw new IOException(NLS.bind(Messages.getString("AbstractValgrindTextParser.Parsing_output_failed"), line)); //$NON-NLS-1$
    }

    /**
     * Determines if argument is a number
     * @param string - argument to test
     * @return - true if argument is a number
     */
    public static boolean isNumber(String string) {
        boolean result = true;
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isDigit(chars[i])) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Parses string ending with format ([FILE]:[LINE MODULE])
     * Assumes syntax is: "\(.*:[0-9]+(\s.+)?\)$"
     * @param line - String with the above criteria
     * @return a tuple of [String filename, Integer line]
     */
    public static Object[] parseFilename(String line) {
        String filename = null;
        int lineNo = 0;

        int ix = line.lastIndexOf('(');
        if (ix >= 0) {
            String part = line.substring(ix, line.length());
            part = part.substring(1, part.length() - 1); // remove leading and trailing parentheses
            if ((ix = part.lastIndexOf(':')) >= 0) {
                String strLineNo = part.substring(ix + 1);
                if (isNumber(strLineNo)) {
                    lineNo = Integer.parseInt(strLineNo);
                    filename = part.substring(0, ix);
                } else {
                    // handle format: (FILE:LINE MODULE)
                    int ix1 = strLineNo.indexOf(' ');
                    if (ix1 > 0) {
                        strLineNo = strLineNo.substring(0, ix1);
                        if (isNumber(strLineNo)) {
                            lineNo = Integer.parseInt(strLineNo);
                            filename = part.substring(0, ix);
                        }
                    }
                }
            } else {
                // check for "in " token (lib, with symbol)
                part = part.replaceFirst("^in ", EMPTY_STRING); //$NON-NLS-1$
                // check for "within " token (lib, without symbol)
                part = part.replaceFirst("^within ", EMPTY_STRING); //$NON-NLS-1$
                filename = part; // library, no line number
            }
        }

        return new Object[] { filename, lineNo };
    }

}