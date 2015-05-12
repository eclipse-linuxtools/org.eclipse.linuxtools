/*******************************************************************************
 * Copyright (c) 2006-2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import org.eclipse.jface.text.formatter.IFormattingStrategy;

public class ChangeLogFormattingStrategy implements IFormattingStrategy {

    private static final String NEW_LINE_CHAR = "\n";
    private static final String WHITE_SPACE_CHAR = " ";
    private static final String TAB_SPACE_CHAR = "\t";
    private static final String DELIMITER_CHARS = NEW_LINE_CHAR + WHITE_SPACE_CHAR + TAB_SPACE_CHAR;

    private static final int MAX_WIDTH = 80;
    private static final SimpleDateFormat isoDate = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public String format(String content, boolean isLineStart,
            String indentation, int[] positions) {

        String firstLine = "";

        // if first line is not from the start, ignore it
        if (!isLineStart) {
            int eol;
            if ((eol = content.indexOf(NEW_LINE_CHAR)) == content.length() - 1) {
                return content;
            } else {
                firstLine = content.substring(0, eol + 1);
                content = content.substring(eol + 1);
            }
        }

        content.replaceFirst("(\\s+)?\\n(\\s+)?", NEW_LINE_CHAR);
        StringTokenizer candidateWords = new StringTokenizer(content, DELIMITER_CHARS, true);

        String formattedContent = formatContent(candidateWords, indentation.length());
        return firstLine.concat(formattedContent);
    }

    private String formatContent(StringTokenizer candidateWords, int indentationLength) {
        StringBuilder formattedWords = new StringBuilder();
        int currentLineLength = indentationLength;

        while (candidateWords.hasMoreTokens()) {
            String cword = candidateWords.nextToken();

            if (isDelimeter(cword)) {
                continue;
            }

            // if the word is date, start new line and include
            // names, email, then an empty line.
            else if (isDate(cword)) {

                // see if we are in middle of line and
                // if so, start new line, else continue.
                if (!isOnNewLine(formattedWords)) {
                    formattedWords.append(NEW_LINE_CHAR);
                }

                if (formattedWords.length() > 0) {
                    formattedWords.append(NEW_LINE_CHAR);
                }

                // insert date
                formattedWords.append(cword + WHITE_SPACE_CHAR);

                // insert name/email
                while (candidateWords.hasMoreTokens()) {
                    cword = candidateWords.nextToken();
                    if (cword.equals(NEW_LINE_CHAR)) {
                        formattedWords.append(NEW_LINE_CHAR);
                        break;
                    }
                    if (isEmail(cword)) {
                        formattedWords.append(WHITE_SPACE_CHAR)
                                      .append(WHITE_SPACE_CHAR)
                                      .append(cword)
                                      .append(NEW_LINE_CHAR)
                                      .append(NEW_LINE_CHAR);
                        currentLineLength = indentationLength;
                        break;
                    }
                    if (!isDelimeter(cword)) {
                        formattedWords.append(WHITE_SPACE_CHAR).append(cword);
                    }
                }
            }

            // means beginning of file name, so whole filename should be
            // in one line.
            else if (isStar(cword)) {
                // see if we are in middle of line and
                // if so, start new line, else continue.
                if (!isOnNewLine(formattedWords)) {
                    formattedWords.append(NEW_LINE_CHAR);
                    currentLineLength = indentationLength;
                }

                formattedWords.append(TAB_SPACE_CHAR).append(cword);
                currentLineLength += cword.length() + 1;

                // this should be path name
                if (candidateWords.countTokens() >= 2) {
                    candidateWords.nextToken();
                    cword = candidateWords.nextToken();

                    formattedWords.append(WHITE_SPACE_CHAR).append(cword);
                    currentLineLength += cword.length() + 1;
                }
            }

            else if (cword.startsWith("(")) {

                if (formattedWords.length() > 0) {
                    formattedWords.append(NEW_LINE_CHAR);
                }
                formattedWords.append(TAB_SPACE_CHAR);

                currentLineLength = 1;
                // add until closing bracket

                boolean skipMultiWhiteSpace = false;

                while (!cword.endsWith("):") && candidateWords.hasMoreTokens()) {
                    if (cword.equals(NEW_LINE_CHAR)) {
                        break;
                    }
                    if (cword.equals(WHITE_SPACE_CHAR) && !skipMultiWhiteSpace) {
                        formattedWords.append(cword);
                        currentLineLength += cword.length();
                        skipMultiWhiteSpace = true;
                    }
                    if (!isDelimeter(cword)) {
                        formattedWords.append(cword);
                        currentLineLength += cword.length();
                        skipMultiWhiteSpace = false;
                    }
                    cword = candidateWords.nextToken();
                }

                formattedWords.append(cword);
                currentLineLength += cword.length();
            }

            else if (currentLineLength + cword.length() > MAX_WIDTH) {
                formattedWords.append(NEW_LINE_CHAR)
                              .append(TAB_SPACE_CHAR)
                              .append(cword);
                currentLineLength = indentationLength + cword.length();
            } else {
                if (isOnNewLine(formattedWords)) {
                    formattedWords.append(TAB_SPACE_CHAR);
                } else {
                    formattedWords.append(WHITE_SPACE_CHAR);
                }
                formattedWords.append(cword);
                currentLineLength += cword.length() + 1;
            }
        }
        return formattedWords.toString();
    }

    private boolean isDate(String inputStr) {
        try {
            return isoDate.parse(inputStr) != null;
        } catch (ParseException e) {
            // Don't care
        }
        return false;
    }

    private boolean isEmail(String inputStr) {
        return inputStr.startsWith("<") && inputStr.endsWith(">");
    }

    private boolean isStar(String inputStr) {
        return inputStr.equals("*");
    }

    private boolean isDelimeter(String cword) {
        return DELIMITER_CHARS.contains(cword);
    }

    private boolean isOnNewLine(StringBuilder formattedWords) {
        int len = formattedWords.length();
        return len == 0 || formattedWords.charAt(len - 1) == NEW_LINE_CHAR.charAt(0);
    }

    @Override
    public void formatterStarts(String initialIndentation) {

    }

    @Override
    public void formatterStops() {

    }

}
