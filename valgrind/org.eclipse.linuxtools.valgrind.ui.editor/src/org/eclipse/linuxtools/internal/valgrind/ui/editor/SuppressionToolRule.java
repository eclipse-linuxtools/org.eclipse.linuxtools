/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui.editor;

import java.util.Arrays;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class SuppressionToolRule implements IRule {
	private String[] toolList;
	private IToken token;
	private WordRule subrule;
	
	private static final IToken DUMMY_TOKEN = new Token(null);
	private static final char[] COLON = new char[] { ':' };
	
	public SuppressionToolRule(String[] tools, IToken successToken) {
		toolList = tools;
		token = successToken;
		subrule = new WordRule(new IWordDetector() {
		
			@Override
			public boolean isWordStart(char c) {
				for (String tool : toolList) {
					if (c == tool.charAt(0)) {
						return true;
					}
				}
				return false;
			}
		
			@Override
			public boolean isWordPart(char c) {
				return c != ':';
			}
			
		});
		
		for (String tool : toolList) {
			subrule.addWord(tool, DUMMY_TOKEN);
		}
	}
	
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		IToken result = subrule.evaluate(scanner);
		if (!result.isUndefined()) {
			boolean match = true;
			
			int tokenLength = ((SuppressionsElementScanner) scanner).getTokenLength();
			match = checkColon(scanner);
			if (match) {
				// move to beginning of token
				for (int i = 0; i < tokenLength; i++) {
					scanner.unread();
				}

				// move to beginning of line
				int indentLength = 0;
				while (scanner.getColumn() > 0) {
					scanner.unread();
					indentLength++;
				}

				// ensure any leading characters are whitespace
				while (indentLength-- > 0) {
					int ch = scanner.read();
					if (!Character.isWhitespace(ch)) {
						match = false;
					}
				}

				// reset to end of token
				for (int i = 0; i < tokenLength; i++) {
					scanner.read();
				}
			}
			
			if (!match) {
				unreadBuffer(scanner, tokenLength);
				result = Token.UNDEFINED;
			}
			else {
				result = token;
			}
		}
		
		return result;
	}

	private boolean checkColon(ICharacterScanner scanner) {
		int ch = scanner.read();
		if (ch == ICharacterScanner.EOF) {
			return false;
		}
		scanner.unread();
		return Arrays.equals(Character.toChars(ch), COLON);
	}

	private void unreadBuffer(ICharacterScanner scanner, int length) {
		for (int i = 0; i < length; i++) {
			scanner.unread();
		}		
	}
}
