/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
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
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.eclipse.jface.text.formatter.IFormattingStrategy;

public class ChangeLogFormattingStrategy implements IFormattingStrategy {

	private static final String NEW_LINE_CHAR = "\n";

	private static final String WHITE_SPACE_CHAR = " ";

	private static final String TAB_SPACE_CHAR = "\t";

	@Override
	public String format(String content, boolean isLineStart,
			String indentation, int[] positions) {

		ArrayList<String> formattedWords = new ArrayList<String>();
		int currentLineLength = indentation.length();
		boolean newLineBegin = true;

		String firstLine = "";

		// if first line is not from the start, ignore it
		if (!isLineStart) {
			int eol;
			if ((eol = content.indexOf('\n')) == content.length() - 1) {
				return content;
			} else {
				firstLine = content.substring(0, eol + 1);
				content = content.substring(eol + 1);
			}
		}

		StringTokenizer candidateWords = new StringTokenizer(content,
				NEW_LINE_CHAR + WHITE_SPACE_CHAR + TAB_SPACE_CHAR, true);

		boolean seenFirstWord = false;

		boolean addedFirstNL = false;
		while (candidateWords.hasMoreTokens()) {

			String cword = candidateWords.nextToken();

			if (!seenFirstWord) {

				if ((cword.indexOf(NEW_LINE_CHAR) < 0
						&& cword.indexOf(WHITE_SPACE_CHAR) < 0 && cword
						.indexOf(TAB_SPACE_CHAR) < 0)) {
					seenFirstWord = true;
				} else {
					if (!addedFirstNL && cword.indexOf(NEW_LINE_CHAR) >= 0) {
						firstLine += "\n";
						addedFirstNL = true;
					}
					continue;
				}

			} else {

				if (cword.indexOf(NEW_LINE_CHAR) >= 0
						|| cword.indexOf(WHITE_SPACE_CHAR) >= 0
						|| cword.indexOf(TAB_SPACE_CHAR) >= 0) {
					continue;
				}
			}

			// if the word is date, start new line and include
			// names, email, then an empty line.
			if (isDate(cword)) {

				// see if we are in middle of line and
				// if so, start new line, else continue.
				if (!newLineBegin)
					formattedWords.add(NEW_LINE_CHAR);

				if (formattedWords.size() > 0)
					formattedWords.add(NEW_LINE_CHAR);

				// insert date
				formattedWords.add(cword + WHITE_SPACE_CHAR);

				// insert name
				cword = candidateWords.nextToken();
				while (!isEmail(cword)) {

					if (!cword.equals(WHITE_SPACE_CHAR) && !cword.equals(TAB_SPACE_CHAR) && !cword.equals(NEW_LINE_CHAR)) {
						formattedWords.add(WHITE_SPACE_CHAR + cword);
					}


					cword = candidateWords.nextToken();
				}

				// insert email
				formattedWords.add(WHITE_SPACE_CHAR + WHITE_SPACE_CHAR + cword + NEW_LINE_CHAR);

				// inserted header, so insert a empty line
				formattedWords.add(NEW_LINE_CHAR);
				newLineBegin = true;
				currentLineLength = indentation.length();
				continue;
			}

			// means beginning of file name, so whole filename should be
			// in one line.
			if (isStar(cword)) {
				// see if we are in middle of line and
				// if so, start new line, else continue.
				if (!newLineBegin) {
					formattedWords.add(NEW_LINE_CHAR);
					currentLineLength = indentation.length();
				}

				formattedWords.add(TAB_SPACE_CHAR + cword);
				currentLineLength += cword.length() + 1;

				// this should be path name
				cword = candidateWords.nextToken();
				cword = candidateWords.nextToken();

				formattedWords.add(WHITE_SPACE_CHAR + cword);
				currentLineLength += cword.length() + 1;
				newLineBegin = false;
				continue;
			}

			if (cword.startsWith("(")) {

				if (formattedWords.size() > 0)
					formattedWords.add(NEW_LINE_CHAR + TAB_SPACE_CHAR);
				else
					formattedWords.add(TAB_SPACE_CHAR);

				currentLineLength = 1;
				// add until closing bracket

				boolean skipMultiWhiteSpace = false;

				while (!cword.endsWith("):")) {

					if (cword.equals(WHITE_SPACE_CHAR) && !skipMultiWhiteSpace) {
						formattedWords.add(cword);
						currentLineLength += cword.length();
						skipMultiWhiteSpace = true;
					}

					if (!cword.equals(WHITE_SPACE_CHAR)
							&& !cword.equals(NEW_LINE_CHAR)
							&& !cword.equals(TAB_SPACE_CHAR)) {
						formattedWords.add(cword);
						currentLineLength += cword.length();
						skipMultiWhiteSpace = false;
					}

					cword = candidateWords.nextToken();
				}
				formattedWords.add(cword);
				currentLineLength += cword.length();
				newLineBegin = false;

				continue;
			}

			if (currentLineLength + cword.length() > 80) {
				formattedWords.add(NEW_LINE_CHAR + TAB_SPACE_CHAR + cword);
				currentLineLength = indentation.length() + cword.length();
				newLineBegin = false;
			} else {
				if (newLineBegin) {
					formattedWords.add(TAB_SPACE_CHAR);
					newLineBegin = false;
				} else {
					formattedWords.add(WHITE_SPACE_CHAR);
				}
				formattedWords.add(cword);
				currentLineLength += cword.length() + 1;

			}
		}

		String finalContent = "";

		for (String formattedWord: formattedWords) {
			finalContent +=formattedWord;
		}

		return firstLine + finalContent;
	}

	private boolean isDate(String inputStr) {

		// Set up patterns for looking for the next date in the changelog
		SimpleDateFormat isoDate = new SimpleDateFormat("yyyy-MM-dd");

		// Try to find next Date bounded changelog entry by parsing date
		// patterns
		// First start with an ISO date
		try {
			Date ad = isoDate.parse(inputStr);
			if (ad != null)
				return true;
		} catch (ParseException e) {
			// We don't really care on exception; it just means it could not
			// parse a date on that line
		}
		return false;
	}

	private boolean isEmail(String inputStr) {
		return inputStr.startsWith("<") && inputStr.endsWith(">");
	}

	private boolean isStar(String inputStr) {
		return inputStr.equals("*");
	}

	@Override
	public void formatterStarts(String initialIndentation) {

	}

	@Override
	public void formatterStops() {

	}

}
