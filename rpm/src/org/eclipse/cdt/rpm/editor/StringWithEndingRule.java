package org.eclipse.cdt.rpm.editor;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class StringWithEndingRule implements IRule {
	/**
	 * The default token to be returned on success and if nothing else has been
	 * specified.
	 */
	protected IToken fDefaultToken;

	protected IToken token;

	protected IStrictWordDetector fDetector;

	/** The column constraint */
	protected int fColumn = UNDEFINED;

	/** Internal setting for the un-initialized column constraint */
	protected static final int UNDEFINED = -1;

	/** Buffer used for pattern detection */
	private StringBuffer fBuffer = new StringBuffer();

	private String fStartingSequence;

	private boolean fMandatoryEndSequence;

	public StringWithEndingRule(String startingSequence,
			IStrictWordDetector trailingCharDetector, IToken inToken,
			boolean endSequenceRequired) {
		token = inToken;
		fDetector = trailingCharDetector;
		fStartingSequence = startingSequence;
		fMandatoryEndSequence = endSequenceRequired;
		fDefaultToken = Token.UNDEFINED;

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

		if (!fMandatoryEndSequence && fDetector.isEndingCharacter((char) c))
			return token;
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
		for (int i = fBuffer.length() - 1; i >= 0; i--)
			scanner.unread();
	}

}
