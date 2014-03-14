/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Andrew Ferrazzutti
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * A helper class for removing comments from a SystemTap script.
 * Note: if an entire line is a comment, it is replaced with a blank line.
 */
public final class CommentRemover {

    private CommentRemover() {}

	/**
	 * Remove comments from a .stp file in the filesystem.
	 * @param filename The filename of the script to remove comments from.
	 * @return The copy of the script with comments removed.
	 */
	public static String execWithFile(String filename) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			StringBuffer buffer = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				buffer.append(line.concat("\n")); //$NON-NLS-1$
			}
			// No need to format line breaks, since this did it already with newlines.
			return exec(buffer.toString(), false);
		} catch (IOException e) {
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * Remove comments from a .stp script. Also, line breaks will be formatted to newlines.
	 * @param contents A complete .stp script.
	 * @return A copy of the script with comments removed.
	 */
	public static String exec(String contents) {
		return exec(contents, true);
	}

	private static String exec(String contents, boolean standardizeLineBreaks) {
		if (contents == null || contents.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		if (standardizeLineBreaks) {
			contents = doStandardizeLineBreaks(contents);
		}

		char curchar, nxtchar;
		boolean inQuotes = false;
		boolean inComment = false;

		int c = 0;
		StringBuffer buffer = new StringBuffer();

		do {
			curchar = contents.charAt(c++);
			nxtchar = c < contents.length() ? contents.charAt(c) : '\0';

			// Comment tags don't count if they are in a string.
			if (!inQuotes) {
				if (!inComment) {
					if (curchar == '#' || (curchar == '/' && nxtchar == '/')) {
						buffer.append('\n'); // Replace the rest of this line with a newline.
						c = contents.indexOf('\n', c); // Skip past the next newline, if one exists.
						if (c == -1) {
							break;
						}
						c++; // Skip the newline character on the next character scan.
						continue;
					}
					if (curchar == '/' && nxtchar == '*') {
						inComment = true;
						c++; // Skip the * on the next character scan.
						continue;
					}
				} else if (curchar == '*' && nxtchar == '/') {
					inComment = false;
					c++; // Skip the / on the next character scan.
					continue;
				}
			}

			// Quotes only count if they aren't commented out.
			if (!inComment) {
				if (curchar == '\"') {
					inQuotes = !inQuotes;
				}
				else if (curchar == '\n' && inQuotes) {
					inQuotes = false;
				}
				buffer.append(curchar);
			}
			else if (curchar == '\n') {
				// Print the line breaks of multiline comments.
				buffer.append(curchar);
			}

		} while (c < contents.length());

		return buffer.toString();
	}

	private static String doStandardizeLineBreaks(String contents) {
		return contents.replaceAll("(\\r\\n)|(\\n)", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}