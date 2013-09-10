/*******************************************************************************
 * Copyright (c) 2006 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

public class GNUFileEntryRule implements IPredicateRule {
	/**
	 * The default token to be returned on success and if nothing else has been
	 * specified.
	 */
	protected IToken fileToken;

	protected IWordDetector fDetector = new IWordDetector() {

		@Override
		public boolean isWordPart(char c) {
			return Character.isJavaIdentifierPart(c) || c == '/' || c == '.' || c == '-';
		}

		@Override
		public boolean isWordStart(char c) {
			return Character.isJavaIdentifierPart(c) || c == '/' || c == '.';
		}

	};

	private boolean started = false;

	private IWhitespaceDetector fWsDetector = new IWhitespaceDetector() {
		@Override
		public boolean isWhitespace(char character) {
			return Character.isWhitespace(character);
		}
	};

	/** The column constraint */
	protected int fColumn = UNDEFINED;

	/** Internal setting for the un-initialized column constraint */
	protected static final int UNDEFINED = -1;

	/** Buffer used for pattern detection */
	private StringBuffer fBuffer = new StringBuffer();

	private String fStartingSequence = "* ";

	public GNUFileEntryRule(IToken fileToken) {
		this.fileToken = fileToken;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		return evaluate(scanner, false);
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		int c = scanner.read();
		fBuffer.setLength(0);
		if (started == false) {
			for (int i = 0; i < fStartingSequence.length(); i++) {
				fBuffer.append((char) c);
				if (fStartingSequence.charAt(i) != c) {
					unreadBuffer(scanner);
					return Token.UNDEFINED;
				}
				c = scanner.read();
			}
		} else if (c == ',') { // we are continuing after a comma (perhaps we have multiple entries
			fBuffer.append((char) c);
			c = scanner.read();
			while (c != ICharacterScanner.EOF && fWsDetector.isWhitespace((char)c)) {
				fBuffer.append((char) c);
				c = scanner.read();
			}
			scanner.unread();
			return ((GNUElementScanner)scanner).getDefaultToken();
		}

		boolean haveFilePart = false;

		while (c != ICharacterScanner.EOF) {
			if (fDetector.isWordPart((char) c)) {
				fBuffer.append((char) c);
				haveFilePart = true;
			}
			else if (c == '\\') {
				fBuffer.append((char) c);
				c = scanner.read();
				if (c == ICharacterScanner.EOF) {
					unreadBuffer(scanner);
					return Token.UNDEFINED;
				}
				fBuffer.append((char) c);
			} else {
				break;
			}
			c = scanner.read();
		}

		if (!haveFilePart) {
			unreadBuffer(scanner);
			return Token.UNDEFINED;
		}

		if (c == ',')
			started = true;

		scanner.unread();
		return fileToken;
	}

	/**
	 * Returns the characters in the buffer to the scanner.
	 *
	 * @param scanner
	 *            the scanner to be used
	 */
	protected void unreadBuffer(ICharacterScanner scanner) {
		for (int i = fBuffer.length() - 1; i >= 0; i--)
			scanner.unread();
		started = false;
	}

	@Override
	public IToken getSuccessToken() {
		return fileToken;
	}

}
