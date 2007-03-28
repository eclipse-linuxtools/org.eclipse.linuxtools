package org.eclipse.cdt.rpm.editor;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class AuthorEmailRule implements IPredicateRule {

	/** Buffer used for pattern detection */
	private StringBuffer fBuffer = new StringBuffer();

	/** The success token */
	IToken token;

	char START_CHAR = '<';

	char END_CHAR = '>';

	char[] INTER_CHARS = { '@', '.' };

	int STATE_START = 0;

	int STATE_OPENED = 1;

	int STATE_AT = 2;

	int STATE_PERIOD = 3;

	int STATE_DONE = 4;

	/** A list of possible ending section headers */
	String[] endingHeaders;

	public AuthorEmailRule(IToken token) {
		this.token = token;
	}

	public IToken getSuccessToken() {
		return token;
	}

	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		/*
		 * whether we think we're reading the ending sequence, i.e. the next
		 * section heading
		 */
		int state = STATE_START;
		fBuffer.setLength(0);
		int c;

		do {
			c = scanner.read();
			fBuffer.append((char) c);

			// we have reached the end of file or line prematurely, this is not
			// considered success
			if (c == ICharacterScanner.EOF || (char) c == '\n') {
				unreadBuffer(scanner, fBuffer);
				return Token.UNDEFINED;
			}

			// we encountered the opening character at the beginning
			if (state == STATE_START && (char) c == START_CHAR) {
				state++;
			} else if (state == STATE_OPENED) {
				// we encountered the first neccessary intermediary char
				if ((char) c == INTER_CHARS[0]) {
					state++;
				}

				// check if we have a valid char
				if (! (Character.isLetterOrDigit((char) c) || c == '.' || c == '_' || c == '-' || c == '@')){
					unreadBuffer(scanner, fBuffer);
					return Token.UNDEFINED;
				}
				
				// we just keep reading

			} else if (state == STATE_AT) {
				// we encountered the second neccessary intermediary char
				if ((char) c == INTER_CHARS[1]) {
					state++;
				}
				
				// check if we have a valid char
				if (! (Character.isLetterOrDigit((char) c) || c == '.' || c == '_' || c == '-')){
					unreadBuffer(scanner, fBuffer);
					return Token.UNDEFINED;
				}
				// we just keep reading
			} else if (state == STATE_PERIOD) {
				// the last char before the ending char cannot be a '.'
				if ((char) c == END_CHAR && fBuffer.charAt(fBuffer.length() - 1) != '.')
					state++;
				else if ((char) c == END_CHAR){
					unreadBuffer(scanner, fBuffer);
					return Token.UNDEFINED;
				}
			} else {
				unreadBuffer(scanner, fBuffer);
				return Token.UNDEFINED;
			}

		} while (state != STATE_DONE);
		
		// we've gone through all states until we've reached STATE_DONE, success
		return token;
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
	protected void unreadBuffer(ICharacterScanner scanner, StringBuffer buffer) {
		for (int i = buffer.length() - 1; i >= 0; i--)
			scanner.unread();
	}

}
