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
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.internal.rpm.ui.editor.detectors.IStrictWordDetector;

public class StringWithEndingRule implements IRule {

	private IToken token;

	private IStrictWordDetector fDetector;

	/** The column constraint */
	private int fColumn = UNDEFINED;

	/** Internal setting for the un-initialized column constraint */
	private static final int UNDEFINED = -1;

	/** Buffer used for pattern detection */
	private StringBuilder fBuffer = new StringBuilder();

	private String fStartingSequence;

	private boolean fMandatoryEndSequence;

	public StringWithEndingRule(String startingSequence,
			IStrictWordDetector trailingCharDetector, IToken inToken,
			boolean endSequenceRequired) {
		super();
		token = inToken;
		fDetector = trailingCharDetector;
		fStartingSequence = startingSequence;
		fMandatoryEndSequence = endSequenceRequired;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int c = scanner.read();
		fBuffer.setLength(0);

		for (int i = 0; i < fStartingSequence.length(); i++) {
			fBuffer.append((char) c);
			if (fStartingSequence.charAt(i) != c) {
				unreadBuffer(scanner);
				return Token.UNDEFINED;
			}
			c = scanner.read();
		}

		if (fDetector.isWordStart((char) c)) {
			if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {

				do {
					fBuffer.append((char) c);
					c = scanner.read();
				} while (c != ICharacterScanner.EOF
						&& fDetector.isWordPart((char) c));

				if (c != ICharacterScanner.EOF && !fDetector.isEndingCharacter((char) c)) {
					unreadBuffer(scanner);
					return Token.UNDEFINED;
				}

				return token;
			}

		}

		if (!fMandatoryEndSequence && fDetector.isEndingCharacter((char) c)) {
			return token;
		}
		scanner.unread();

		unreadBuffer(scanner);
		return Token.UNDEFINED;
	}

	/**
	 * Returns the characters in the buffer to the scanner.
	 *
	 * @param scanner
	 *            the scanner to be used
	 */
	protected void unreadBuffer(ICharacterScanner scanner) {
		for (int i = fBuffer.length() - 1; i >= 0; i--) {
			scanner.unread();
		}
	}

}
