/*******************************************************************************
 * Copyright (c) 2007, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SectionRule implements IPredicateRule {

	/** Buffer used for pattern detection */
	private StringBuilder fBuffer = new StringBuilder();

	/** Buffer used for pattern detection of next header */
	private StringBuilder nextHeaderBuffer = new StringBuilder();

	/** The success token */
	private IToken token;

	/** The beginning token, represents a section of the spec file */
	private String startingHeader;

	/** A list of possible ending section headers */
	private String[] endingHeaders;

	public SectionRule(String startingHeader, String[] endingHeaders,
			IToken token) {
		this.startingHeader = startingHeader;
		this.endingHeaders = endingHeaders;
		this.token = token;
	}

	@Override
	public IToken getSuccessToken() {
		return token;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		/* whether we think we're reading the ending sequence, i.e. the next
		 * section heading
		 */
		boolean readingEndSequence = false;
		fBuffer.setLength(0);
		nextHeaderBuffer.setLength(0);
		int c = scanner.read();

		/* if we're starting at the beginning header we check that the partition
		 * begins with the required header */
		if (!resume) {
			for (int i = 0; i < startingHeader.length(); i++) {
				fBuffer.append((char) c);
				if (startingHeader.charAt(i) != (char) c) {
					unreadBuffer(scanner, fBuffer);
					return Token.UNDEFINED;
				}
				c = scanner.read();
			}
		}

		fBuffer.append((char) c);

		do {
			// Sections can only begin with a % on a new line
			if (c == '\n') {
				// if we were readin a %blah, reset because it turned out to
				// not be a terminating section header
				readingEndSequence = false;

				// if we're reading a line beginning with % then it might be
				// at terminating sectino header
				c = scanner.read();
				if (c == '%') {
					// Start appending to the we reset the buffer for section
					// headers, and indicate that this line can be a terminating
					// section header
					nextHeaderBuffer.setLength(0);
					readingEndSequence = true;
				} else if (c == ICharacterScanner.EOF) {
					// we allow EOF as a valid ending to a section
					break;
				} else {
					fBuffer.append((char) c);
					continue;
				}
			}
			// we're in a line that's a possible terminating section header,
			// so we compare it with all terminating headers
			if (readingEndSequence) {
				nextHeaderBuffer.append((char) c);
				for (int i = 0; i < endingHeaders.length; i++) {
					String tempSectionheader = endingHeaders[i];

					// we've found our terminating header
					if (nextHeaderBuffer.toString().equals(tempSectionheader)) {
						// exclude the terminating header from the partition
						unreadBuffer(scanner, nextHeaderBuffer);
						return token;
					}
				}
			}

			// read the next char
			c = scanner.read();
			fBuffer.append((char) c);
		} while (c != ICharacterScanner.EOF);

		// we've reached EOF and since our section started with the correct
		// header, then this is just the current end of the partition, and
		// we return the success token
		return token;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		return evaluate(scanner, false);
	}

	/**
	 * Returns the characters in the buffer to the scanner.
	 *
	 * @param scanner
	 *            the scanner to be used
	 */
	protected void unreadBuffer(ICharacterScanner scanner, StringBuilder buffer) {
		for (int i = buffer.length() - 1; i >= 0; i--) {
			scanner.unread();
		}
	}

}
