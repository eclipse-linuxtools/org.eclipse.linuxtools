/*******************************************************************************
 * Copyright (c) 2006, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

    private IWhitespaceDetector fWsDetector = character -> Character.isWhitespace(character);

    /** Internal setting for the un-initialized column constraint */
    protected static final int UNDEFINED = -1;

    /** Buffer used for pattern detection */
    private StringBuilder fBuffer = new StringBuilder();

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
        if (!started) {
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
            if (scanner instanceof GNUHyperlinkScanner gnuScanner) {
            	return gnuScanner.getDefaultToken();
            }
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
    private void unreadBuffer(ICharacterScanner scanner) {
        for (int i = fBuffer.length() - 1; i >= 0; i--) {
            scanner.unread();
        }
        started = false;
    }

    @Override
    public IToken getSuccessToken() {
        return fileToken;
    }

}
