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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class SuppressionKindRule implements IRule {
    private Map<String, List<String>> kinds;
    private IToken token;
    private WordRule subrule;

    private static final IToken DUMMY_TOKEN = new Token(null);
    private static final char[] COLON = new char[] { ':' };

    public SuppressionKindRule(Map<String, List<String>> suppKinds, IToken successToken) {
        kinds = suppKinds;
        token = successToken;

        final List<String> suppKindsList = new ArrayList<>();
        for (List<String> entry : suppKinds.values()) {
            suppKindsList.addAll(entry);
        }

        subrule = new WordRule(new IWordDetector() {

            @Override
            public boolean isWordStart(char c) {
                for (String kind : suppKindsList) {
                    if (c == kind.charAt(0)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean isWordPart(char c) {
                return Character.isJavaIdentifierPart(c);
            }

        });

        for (String kind : suppKindsList) {
            subrule.addWord(kind, DUMMY_TOKEN);
        }
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        IToken result = subrule.evaluate(scanner);
        StringBuffer tool = new StringBuffer();
        StringBuffer kind = new StringBuffer();

        if (!result.isUndefined()) {
            boolean match = true;

            // move to beginning of token
            int tokenLength = ((SuppressionsElementScanner) scanner).getTokenLength();
            for (int i = 0; i < tokenLength; i++) {
                scanner.unread();
            }

            // check first char before token is a ':'
            match = checkColon(scanner);
            if (match) {
                // move to beginning of line
                int preTokenLength = 0;
                while (scanner.getColumn() > 0) {
                    scanner.unread();
                    preTokenLength++;
                }

                // ensure any leading characters are whitespace
                boolean foundChar = false;
                while (preTokenLength-- > 1) { // skip colon
                    int ch = scanner.read();
                    if (match && !Character.isWhitespace(ch)) {
                        foundChar = true;
                        tool.append(Character.toChars(ch));
                    }
                    else if (foundChar) {
                        // encountered whitespace after tool name started
                        match = false;
                    }
                }

                scanner.read(); // consume colon

                // reset to end of token
                for (int i = 0; i < tokenLength; i++) {
                    int ch = scanner.read();
                    if (match) {
                        kind.append(Character.toChars(ch));
                    }
                }

                // assert kind is valid for tool
                if (match) {
                    List<String> kindList = kinds.get(tool.toString());
                    if (kindList == null || !kindList.contains(kind.toString())) {
                        match = false;
                    }
                }
                else {
                    // reset to beginning of token
                    unreadBuffer(scanner, tokenLength);
                }
            }

            if (!match) {
                result = Token.UNDEFINED;
            }
            else {
                result = token;
            }
        }

        return result;
    }

    private void unreadBuffer(ICharacterScanner scanner, int length) {
        for (int i = 0; i < length; i++) {
            scanner.unread();
        }
    }

    private boolean checkColon(ICharacterScanner scanner) {
        if (scanner.getColumn() == 0) {
            // nothing to read
            return false;
        }
        scanner.unread();
        int ch = scanner.read();
        return Arrays.equals(Character.toChars(ch), COLON);
    }

}
