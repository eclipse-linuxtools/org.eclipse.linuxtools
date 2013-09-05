/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Alphonse Van Assche
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class SpecfileHover implements ITextHover, ITextHoverExtension {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private SpecfileEditor editor;

	public SpecfileHover(SpecfileEditor editor) {
		this.editor = editor;
	}

	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion == null || hoverRegion.getLength() == 0) {
			return null;
		}

		Specfile spec = editor.getSpecfile();
		if (spec == null) {
			return null;
		}

		String currentSelection;
		try {
			currentSelection = textViewer.getDocument().get(hoverRegion.getOffset() + 1, hoverRegion.getLength() - 1);
		} catch (BadLocationException e) {
			return null;
		}

        // First we try to get a define based on the given name
		SpecfileDefine define = spec.getDefine(currentSelection);

        String value = currentSelection + ": "; //$NON-NLS-1$

		if (define != null) {
			return value + define.getStringValue();
		}

		String macroLower = currentSelection.toLowerCase();

		// If there's no such define we try to see if it corresponds to
		// a Source or Patch declaration
		String retrivedValue = getSourceOrPatchValue(spec, macroLower);
		if (retrivedValue != null) {
			return value + retrivedValue;
		} else {
			// If it does not correspond to a Patch or Source macro, try to find it
			// in the macro proposals list.
			retrivedValue = getMacroValueFromMacroList(currentSelection);
			if (retrivedValue != null) {
				return value + retrivedValue;
			} else {
				// If it does not correspond to a macro in the list, try to find it
				// in the RPM list.
				retrivedValue = Activator.getDefault().getRpmPackageList().getValue(currentSelection.replaceFirst(":",EMPTY_STRING)); //$NON-NLS-1$
				if (retrivedValue != null) {
					return retrivedValue;
				}
			}
		}
       // We return null in other cases, so we don't show hover information
       // for unrecognized macros and RPM packages.
       return null;
	}

	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {

		if (textViewer != null) {
			/*
			 * If the hover offset falls within the selection range return the
			 * region for the whole selection.
			 */
			Point selectedRange = textViewer.getSelectedRange();
			if (selectedRange.x >= 0 && selectedRange.y	 > 0
					&& offset >= selectedRange.x
					&& offset <= selectedRange.x + selectedRange.y) {
				return new Region(selectedRange.x, selectedRange.y);
			} else {
				IRegion region = findWord(textViewer.getDocument(), offset);
				if (region.equals(new Region(offset, 0))) {
					region = findPackages(textViewer.getDocument(), offset);
				}
				return region;
			}
		}
		return null;
	}

	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, false);
			}
		};
	}


	public static IRegion findWord(IDocument document, int offset) {
		int start = -1;
		int end = -1;
		boolean beginsWithBrace = false;

		try {
			int pos = offset;
			char c;

			while (pos >= 0) {
				c = document.getChar(pos);
				if (c == '%') {
					if (document.getChar(pos + 1) == '{') {
						beginsWithBrace = true;
					}
					break;
				}
				else if (c == '\n' || c == '}'){
					// if we hit the beginning of the line, it's not a macro
					return new Region(offset, 0);
				}
				--pos;
			}

			if (!beginsWithBrace) {
				--pos;
			}

			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (beginsWithBrace && (c == '}')) {
					break;
				} else if (c == '\n' || c == '%' || c == '('){ // '(' is needed for the %deffatt( case
					break;
//					Do not return empty region here. We have a work.
//					return new Region(offset, 0);
				} else if (!beginsWithBrace && c == ' ') {
					break;
				}
				++pos;
			}

			end = pos;

		} catch (BadLocationException x) {
		}

		if (start >= -1 && end > -1) {
			if (start == offset) {
				return new Region(start, end - start);
			} else {
				return new Region(start + 1, end - start - 1);
			}
		}

		return null;
	}

	public static IRegion findPackages(IDocument document, int offset) {
		int start = -1;
		int end = -1;
		boolean beginsWithSpace = false;
		try {
			int pos = offset;
			char c;
			while (pos >= 0) {
				c = document.getChar(pos);
				if (c == ' ' || c =='\t' || c==':')  {
					if (Character.isLetter(document.getChar(pos + 1))) {
						beginsWithSpace = true;
						break;
					} else if (c == '\n'){
						return new Region(offset, 0);
					}
				}
				--pos;
			}
			--pos;
			start = pos;
			pos = offset;
			int length = document.getLength();
			while (pos < length) {
				c = document.getChar(pos);
				if (beginsWithSpace && (!Character.isLetter(c) && !Character.isDigit(c) && c != '-')) {
					break;
				} else if (c == '\n') {
					return new Region(offset, 0);
				}
				++pos;
			}
			end = pos;

		} catch (BadLocationException x) {
		}

		if (start > -1 && end > -1) {
			return new Region(start, end - start);
		}
		return null;
	}

	public static String getSourceOrPatchValue(Specfile spec, String patchOrSourceName) {
		String value = null;
		Pattern p = Pattern.compile("(source|patch)(\\d*)"); //$NON-NLS-1$
		Matcher m = p.matcher(patchOrSourceName);

		if (m.matches()) {
			String digits = m.group(2);

			SpecfileSource source = null;
			int number = -1;

			if (digits != null && digits.equals(EMPTY_STRING)) {
				number = 0;
			} else if (digits != null && !digits.equals(EMPTY_STRING)) {
				number = Integer.parseInt(digits);
			}

			if (number != -1) {
				if (m.group(1).equals("source")) {//$NON-NLS-1$
					source = spec.getSource(number);
				} else if (m.group(1).equals("patch")) {//$NON-NLS-1$
					source = spec.getPatch(number);
				}

				if (source != null) {
					value = source.getFileName();
				}
			}
		}
		return value;
	}

	public static String getMacroValueFromMacroList(String macroName) {
		String value = null;
		if (Activator.getDefault().getRpmMacroList().findKey("%" + macroName)) { //$NON-NLS-1$
			String currentConfig = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_MACRO_HOVER_CONTENT);
			// Show content of the macro according with the configuration set
			// in the macro preference page.
			if (currentConfig.equals(PreferenceConstants.P_MACRO_HOVER_CONTENT_VIEWDESCRIPTION)) {
				value = Activator.getDefault().getRpmMacroList().getValue(macroName);
			} else {
				value = RpmMacroProposalsList.getMacroEval("%" + macroName); //$NON-NLS-1$
			}
		}
		return value;
	}
}
