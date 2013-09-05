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

package org.eclipse.linuxtools.internal.rpm.ui.editor.parser;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;

public class SpecfileSource extends SpecfileElement {
	private int number;
	private int lineNumber = -1;
	private String fileName;

	public enum SourceType {
		SOURCE, PATCH
	}

	private SourceType sourceType;
	private List<Integer> linesUsed;

	public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	public SpecfileSource(int number, String fileName) {
		super("source"); //$NON-NLS-1$
		this.number = number;
		this.fileName = fileName;
		this.linesUsed = new ArrayList<Integer>();
	}

	public String getFileName() {
		return resolve(fileName);
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void addLineUsed(int lineNumber) {
		linesUsed.add(Integer.valueOf(lineNumber));
	}

	public void removeLineUsed(int lineNumber) {
		linesUsed.remove(Integer.valueOf(lineNumber));
	}

	public List<Integer> getLinesUsed() {
		return linesUsed;
	}

	@Override
	public String toString() {
		if (sourceType == SourceType.SOURCE) {
			return MessageFormat.format(
					"Source #{0} (line #{1}, used on lines {2}) -> {3}", //$NON-NLS-1$
					number, lineNumber, getLinesUsed(), fileName);
		}
		return MessageFormat.format(
				"Patch #{0} (line #{1}, used on lines {2}) -> {3}", number, //$NON-NLS-1$
				lineNumber, getLinesUsed(), fileName);
	}

	// Note that changeReferences assumes that the number of the source/patch
	// has *already been set*. If this is not true, it will simply do nothing
	public void changeReferences(int oldPatchNumber) {
		Specfile specfile = this.getSpecfile();
		Pattern patchPattern;
		if (oldPatchNumber == 0) {
			patchPattern = Pattern
					.compile("%patch" + oldPatchNumber + "|%patch"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			patchPattern = Pattern.compile("%patch" + oldPatchNumber); //$NON-NLS-1$
		}
		for (int lineNumber : getLinesUsed()) {
			String line;
			try {
				line = specfile.getLine(lineNumber);
				Matcher patchMatcher = patchPattern.matcher(line);
				if (!patchMatcher.find()) {
					System.out
							.println(Messages.getString("SpecfileSource.0") + patchPattern.pattern()); //$NON-NLS-1$
					// throw new BadLocationException("can't match " +
					// patchPattern);
				}
				specfile.changeLine(lineNumber, line.replaceAll(
						patchPattern.pattern(),
						Messages.getString("SpecfileSource.1") + number)); //$NON-NLS-1$
			} catch (BadLocationException e) {
				SpecfileLog.logError(e);
			}
		}
	}

	public void changeDeclaration(int oldPatchNumber) {
		Specfile specfile = this.getSpecfile();
		Pattern patchPattern;
		if (oldPatchNumber == 0) {
			patchPattern = Pattern.compile("Patch" + oldPatchNumber + "|Patch"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			patchPattern = Pattern.compile("Patch" + oldPatchNumber); //$NON-NLS-1$
		}
		String line;
		try {
			line = specfile.getLine(lineNumber);
			Matcher patchMatcher = patchPattern.matcher(line);
			if (!patchMatcher.find()) {
				// TODO: Maybe we can throw a exception here.
				System.out
						.println(Messages.getString("SpecfileSource.2") + patchPattern.pattern()); //$NON-NLS-1$
			}
			specfile.changeLine(lineNumber,
					line.replaceAll(patchPattern.pattern(), "Patch" + number)); //$NON-NLS-1$
		} catch (BadLocationException e) {
			SpecfileLog.logError(e);
		}
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
}
