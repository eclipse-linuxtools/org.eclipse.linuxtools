/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
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
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfileChangelogScanner;

public class VersionReleaseRule implements IPredicateRule {

	/** Buffer used for pattern detection */
	private StringBuilder fBuffer = new StringBuilder();

	/** Buffer to keep track of trailing whitespace */
	private StringBuilder fWhiteSpaceBuffer = new StringBuilder();

	/** The success token */
	private IToken fToken;

	/** The token that has to preceed this token */
	private IToken fPreceedingToken;

	/** Where we can find out what the preceeding token was */
	private SpecfileChangelogScanner fChangelogScanner;

	protected static final char CHARS_SEPERATOR = '-';

	protected static final int STATE_START = 0;

	protected static final int STATE_VERSION = 1;

	protected static final int STATE_RELEASE = 2;

	protected static final int STATE_TRAIL = 3;

	protected static final int STATE_DONE = 4;

	public VersionReleaseRule(IToken token) {
		this.fToken = token;
	}

	public VersionReleaseRule(IToken successToken, IToken preceedingToken,
			SpecfileChangelogScanner scanner) {
		fToken = successToken;
		fPreceedingToken = preceedingToken;
		fChangelogScanner = scanner;
	}

	public IToken getSuccessToken() {
		return fToken;
	}

	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		// if the last token successfully read was not the fPreceedingToke fail
		IToken lastToken = getLastToken();

		if (lastToken != fPreceedingToken) {
			return Token.UNDEFINED;
		}

		fBuffer.setLength(0);
		int state = STATE_START;
		int c;
		int numPreceedingBlanks = 0;

		do {
			c = scanner.read();
			fBuffer.append((char) c);

			// preceeding white space
			if (state == STATE_START) {
				if (Character.isWhitespace((char) c) || c == '-') {
					numPreceedingBlanks++;
				} else {
					state++;
				}
			}
			// version state (first part of version-release)
			if (state == STATE_VERSION) {
				// if we've read some semblance of a version and we've reached
				// the separator character
				if (fBuffer.length() > numPreceedingBlanks
						&& c == CHARS_SEPERATOR) {
					state++;
				}
				// otherwise we allow only digits, letters, underscores, ':' or
				// '.' in the version
				else if (!(Character.isLetterOrDigit((char) c) || c == '.'
						|| c == '_' || c == ':')) {
					unreadBuffer(scanner, fBuffer);
					return Token.UNDEFINED;
				}
			}
			// release state (second part of version-release)
			else if (state == STATE_RELEASE) {
				// an EOF or EOL indicates success
				if (c == ICharacterScanner.EOF || c == '\n') {
					state = STATE_DONE;
				}

				// if we encounter a space, we enter the optional trailing
				// space section which we consider valid (but not part of the
				// token) if and only if it is ended by and EOF or EOL
				else if (Character.isWhitespace((char) c)) {
					state++;
					fWhiteSpaceBuffer.setLength(0);
					fWhiteSpaceBuffer.append(c);
				} else if (!(Character.isLetterOrDigit((char) c) || c == '.' || c == '_')) {
					// allow digits, characters or '.' in the release
					unreadBuffer(scanner, fBuffer);
					return Token.UNDEFINED;
				}

			}
			// whitespace state, we finished redeaing the ver-rel and are
			// now looking for an EOF or EOL for success
			else if (state == STATE_TRAIL) {
				// success, unwind the whitespace
				if (c == ICharacterScanner.EOF || c == '\n') {
					unreadBuffer(scanner, fWhiteSpaceBuffer);
					state++;
				} // some other illegal token after ver-rel unwind the whole
					// deal
				else if (!Character.isWhitespace((char) c)) {
					unreadBuffer(scanner, fBuffer);
					return Token.UNDEFINED;
				} else { // white space, keep reading
					fWhiteSpaceBuffer.append((char) c);
				}

			}
		} while (state != STATE_DONE);

		// we've gone through all states until we've reached STATE_DONE, success
		return fToken;
	}

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

	protected IToken getLastToken() {
		return fChangelogScanner.getLastToken();
	}
}
