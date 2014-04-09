/*******************************************************************************
 * Copyright (c) 2000, 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Anton Leherbauer (Wind River Systems)
 *     Red Hat Inc. - modified for use in SystemTap
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import static org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPHeuristicScanner.NOT_FOUND;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;

/**
 * Uses the {@link org.eclipse.cdt.internal.ui.text.STPHeuristicScanner} to
 * get the indentation level for a certain position in a document.
 *
 * <p>
 * An instance holds some internal position in the document and is therefore
 * not thread-safe.
 * </p>
 */
public final class STPIndenter {
	/**
	 * The CDT Core preferences.
	 */
	private final class CorePrefs {
		private final boolean prefUseTabs;
		private final boolean prefArrayDimensionsDeepIndent;
		private final int prefArrayIndent;
		private final boolean prefArrayDeepIndent;
		private final boolean prefTernaryDeepAlign;
		private final int prefTernaryIndent;
		private final int prefCaseIndent;
		private final int prefCaseBlockIndent;
		private final int prefAssignmentIndent;
		private final int prefSimpleIndent;
		private final int prefBracketIndent;
		private final boolean prefMethodDeclDeepIndent;
		private final boolean prefMethodDeclFirstParameterDeepIndent;
		private final int prefMethodDeclIndent;
		private final boolean prefMethodCallDeepIndent;
		private final boolean prefMethodCallFirstParameterDeepIndent;
		private final int prefMethodCallIndent;
		private final boolean prefParenthesisDeepIndent;
		private final int prefParenthesisIndent;
		private final int prefBlockIndent;
		private final int prefMethodBodyIndent;
		private final int prefTypeIndent;
		private final int prefAccessSpecifierIndent;
		private final int prefAccessSpecifierExtraSpaces;
		private final int prefNamespaceBodyIndent;
		private final boolean prefIndentBracesForBlocks;
		private final boolean prefIndentBracesForArrays;
		private final boolean prefIndentBracesForMethods;
		private final boolean prefIndentBracesForTypes;
		private final int prefContinuationIndent;
		private final boolean prefHasTemplates;
		private final String prefTabChar;

		private final IPreferencesService preferenceService;
		private final IScopeContext[] preferenceContexts;

		/**
		 * Returns the possibly project-specific core preference defined under <code>key</code>.
		 *
		 * @param key the key of the preference
		 * @return the value of the preference
		 */
		private String getCoreFormatterOption(String key) {
			return getCoreFormatterOption(key, null);
		}

		private String getCoreFormatterOption(String key, String defaultValue) {
			return preferenceService.getString(IDEPlugin.PLUGIN_ID, key, defaultValue, preferenceContexts);
		}

		private int getCoreFormatterOption(String key, int defaultValue) {
			return preferenceService.getInt(IDEPlugin.PLUGIN_ID, key, defaultValue, preferenceContexts);
		}

		CorePrefs(IProject project) {
			preferenceService = Platform.getPreferencesService();
			preferenceContexts = project != null ?
					new IScopeContext[] { new ProjectScope(project.getProject()),
										  InstanceScope.INSTANCE, DefaultScope.INSTANCE } :
					new IScopeContext[] { InstanceScope.INSTANCE, DefaultScope.INSTANCE };
			prefUseTabs= prefUseTabs();
			prefArrayDimensionsDeepIndent= prefArrayDimensionsDeepIndent();
			prefContinuationIndent= prefContinuationIndent();
			prefBlockIndent= prefBlockIndent();
			prefArrayIndent= prefArrayIndent();
			prefArrayDeepIndent= prefArrayDeepIndent();
			prefTernaryDeepAlign= false;
			prefTernaryIndent= prefContinuationIndent();
			prefCaseIndent= prefCaseIndent();
			prefCaseBlockIndent= prefCaseBlockIndent();
			prefAssignmentIndent= prefAssignmentIndent();
			prefIndentBracesForBlocks= prefIndentBracesForBlocks();
			prefSimpleIndent= prefSimpleIndent();
			prefBracketIndent= prefBracketIndent();
			prefMethodDeclDeepIndent= prefMethodDeclDeepIndent();
			prefMethodDeclFirstParameterDeepIndent= prefMethodDeclFirstParameterDeepIndent();
			prefMethodDeclIndent= prefMethodDeclIndent();
			prefMethodCallDeepIndent= prefMethodCallDeepIndent();
			prefMethodCallFirstParameterDeepIndent= prefMethodCallFirstParameterDeepIndent();
			prefMethodCallIndent= prefMethodCallIndent();
			prefParenthesisDeepIndent= prefParenthesisDeepIndent();
			prefParenthesisIndent= prefParenthesisIndent();
			prefMethodBodyIndent= prefMethodBodyIndent();
			prefTypeIndent= prefTypeIndent();
			prefAccessSpecifierIndent= prefAccessSpecifierIndent();
			prefAccessSpecifierExtraSpaces= prefAccessSpecifierExtraSpaces();
			prefNamespaceBodyIndent= prefNamespaceBodyIndent();
			prefIndentBracesForArrays= prefIndentBracesForArrays();
			prefIndentBracesForMethods= prefIndentBracesForMethods();
			prefIndentBracesForTypes= prefIndentBracesForTypes();
			prefHasTemplates= hasTemplates();
			prefTabChar= getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		}

		private boolean prefUseTabs() {
			return !IDEPlugin.SPACE.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_TAB_CHAR));
		}

		private boolean prefArrayDimensionsDeepIndent() {
			return true; // sensible default, no formatter setting
		}

		private int prefArrayIndent() {
			String option= getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_INITIALIZER_LIST);
			try {
				if (STPDefaultCodeFormatterConstants.getIndentStyle(option) == STPDefaultCodeFormatterConstants.INDENT_BY_ONE)
					return 1;
			} catch (IllegalArgumentException e) {
				// ignore and return default
			}

			return prefContinuationIndent(); // default
		}

		private boolean prefArrayDeepIndent() {
			String option= getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_INITIALIZER_LIST);
			try {
				return STPDefaultCodeFormatterConstants.getIndentStyle(option) == STPDefaultCodeFormatterConstants.INDENT_ON_COLUMN;
			} catch (IllegalArgumentException e) {
				// ignore and return default
			}

			return true;
		}

		private int prefCaseIndent() {
			if (STPDefaultCodeFormatterConstants.TRUE.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH)))
				return 1;
			else
				return 0;
		}

		private int prefCaseBlockIndent() {
			if (STPDefaultCodeFormatterConstants.TRUE.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES)))
				return 1;
			else
				return 0;
		}

		private int prefAssignmentIndent() {
			return prefContinuationIndent();
		}

		private int prefSimpleIndent() {
			if (prefIndentBracesForBlocks() && prefBlockIndent() == 0)
				return 1;
			else
				return prefBlockIndent();
		}

		private int prefBracketIndent() {
			return prefBlockIndent();
		}

		private boolean prefMethodDeclDeepIndent() {
			String option= getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION);
			try {
				int indentStyle = STPDefaultCodeFormatterConstants.getIndentStyle(option);
				return indentStyle == STPDefaultCodeFormatterConstants.INDENT_ON_COLUMN;
			} catch (IllegalArgumentException e) {
				// ignore and return default
			}

			return false;
		}

		private boolean prefMethodDeclFirstParameterDeepIndent() {
			String option= getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION);
			try {
				int indentStyle = STPDefaultCodeFormatterConstants.getIndentStyle(option);
				int wrappingStyle = STPDefaultCodeFormatterConstants.getWrappingStyle(option);
				return indentStyle == STPDefaultCodeFormatterConstants.INDENT_ON_COLUMN &&
				    	(wrappingStyle == STPDefaultCodeFormatterConstants.WRAP_COMPACT_FIRST_BREAK ||
				    	wrappingStyle == STPDefaultCodeFormatterConstants.WRAP_ONE_PER_LINE);
			} catch (IllegalArgumentException e) {
				// ignore and return default
			}

			return false;
		}

		private int prefMethodDeclIndent() {
			String option= getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION);
			try {
				if (STPDefaultCodeFormatterConstants.getIndentStyle(option) == STPDefaultCodeFormatterConstants.INDENT_BY_ONE)
					return 1;
				else
					return prefContinuationIndent();
			} catch (IllegalArgumentException e) {
				// ignore and return default
			}
			return 1;
		}

		private boolean prefMethodCallDeepIndent() {
			String option= getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION);
			try {
				int indentStyle = STPDefaultCodeFormatterConstants.getIndentStyle(option);
				return indentStyle == STPDefaultCodeFormatterConstants.INDENT_ON_COLUMN;
			} catch (IllegalArgumentException e) {
				// ignore and return default
			}
			return false; // sensible default
		}

		private boolean prefMethodCallFirstParameterDeepIndent() {
			String option= getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION);
			try {
				int indentStyle = STPDefaultCodeFormatterConstants.getIndentStyle(option);
				int wrappingStyle = STPDefaultCodeFormatterConstants.getWrappingStyle(option);
				return indentStyle == STPDefaultCodeFormatterConstants.INDENT_ON_COLUMN &&
				    	(wrappingStyle == STPDefaultCodeFormatterConstants.WRAP_COMPACT_FIRST_BREAK ||
				        wrappingStyle == STPDefaultCodeFormatterConstants.WRAP_ONE_PER_LINE);
			} catch (IllegalArgumentException e) {
				// ignore and return default
			}
			return false; // sensible default
		}

		private int prefMethodCallIndent() {
			String option= getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION);
			try {
				if (STPDefaultCodeFormatterConstants.getIndentStyle(option) == STPDefaultCodeFormatterConstants.INDENT_BY_ONE)
					return 1;
				else
					return prefContinuationIndent();
			} catch (IllegalArgumentException e) {
				// ignore and return default
			}

			return 1; // sensible default
		}

		private boolean prefParenthesisDeepIndent() {
			return false;
		}

		private int prefParenthesisIndent() {
			return prefContinuationIndent();
		}

		private int prefBlockIndent() {
			String option= getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BLOCK);
			if (STPDefaultCodeFormatterConstants.FALSE.equals(option))
				return 0;

			return 1; // sensible default
		}

		private int prefMethodBodyIndent() {
			if (STPDefaultCodeFormatterConstants.FALSE.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BODY)))
				return 0;

			return 1; // sensible default
		}

		private int prefTypeIndent() {
			String option= getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER);
			if (STPDefaultCodeFormatterConstants.FALSE.equals(option))
				return 0;

			return 1; // sensible default
		}

		private int prefAccessSpecifierIndent() {
			if (STPDefaultCodeFormatterConstants.TRUE.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER)))
				return 1;
			else
				return 0;
		}

		private int prefAccessSpecifierExtraSpaces() {
			return getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_EXTRA_SPACES, 0);
		}

		private int prefNamespaceBodyIndent() {
			if (STPDefaultCodeFormatterConstants.TRUE.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_NAMESPACE_HEADER)))
				return prefBlockIndent();
			else
				return 0;
		}

		private boolean prefIndentBracesForBlocks() {
			return STPDefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_BLOCK));
		}

		private boolean prefIndentBracesForArrays() {
			return STPDefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_INITIALIZER_LIST));
		}

		private boolean prefIndentBracesForMethods() {
			return STPDefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION));
		}

		private boolean prefIndentBracesForTypes() {
			return STPDefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION));
		}

		private int prefContinuationIndent() {
			try {
				return Integer.parseInt(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION));
			} catch (NumberFormatException e) {
				// ignore and return default
			}

			return 2; // sensible default
		}

		private boolean hasTemplates() {
			return true;
		}
	}

	/** The document being scanned. */
	private final IDocument fDocument;
	/** The indentation accumulated by <code>findReferencePosition</code>. */
	private int fIndent;
	/** Extra spaces to add on top of fIndent */
	private int fExtraSpaces;
	/**
	 * The absolute (character-counted) indentation offset for special cases
	 * (method definitions, array initializers)
	 */
	private int fAlign;
	/** The stateful scan position for the indentation methods. */
	private int fPosition;
	/** The previous position. */
	private int fPreviousPos;
	/** The most recent token. */
	private int fToken;
	/** The line of <code>fPosition</code>. */
	private int fLine;
	/**
	 * The scanner we will use to scan the document. It has to be installed
	 * on the same document as the one we get.
	 */
	private final STPHeuristicScanner fScanner;
	/**
	 * The CDT Core preferences.
	 */
	private final CorePrefs fPrefs;

	/**
	 * Creates a new instance.
	 *
	 * @param document the document to scan
	 * @param scanner the {@link STPHeuristicScanner} to be used for scanning
	 * the document. It must be installed on the same <code>IDocument</code>.
	 */
	public STPIndenter(IDocument document, STPHeuristicScanner scanner) {
		this(document, scanner, null);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param document the document to scan
	 * @param scanner the {@link STPHeuristicScanner} to be used for scanning
	 *        the document. It must be installed on the same
	 *        <code>IDocument</code>.
	 * @param project the C/C++ project to get the formatter preferences from, or
	 *        <code>null</code> to use the workspace settings
	 */
	public STPIndenter(IDocument document, STPHeuristicScanner scanner, IProject project) {
		Assert.isNotNull(document);
		Assert.isNotNull(scanner);
		fDocument= document;
		fScanner= scanner;
		fPrefs= new CorePrefs(project);
	}

	/**
	 * Computes the indentation at the reference point of <code>position</code>.
	 *
	 * @param offset the offset in the document
	 * @return a String which reflects the indentation at the line in which the
	 *         reference position to <code>offset</code> resides, or <code>null</code>
	 *         if it cannot be determined
	 */
	public StringBuilder getReferenceIndentation(int offset) {
		return getReferenceIndentation(offset, false);
	}

	/**
	 * Computes the indentation at the reference point of <code>position</code>.
	 *
	 * @param offset the offset in the document
	 * @param assumeOpeningBrace <code>true</code> if an opening brace should be assumed
	 * @return a String which reflects the indentation at the line in which the
	 *         reference position to <code>offset</code> resides, or <code>null</code>
	 *         if it cannot be determined
	 */
	private StringBuilder getReferenceIndentation(int offset, boolean assumeOpeningBrace) {
		int unit= findReferencePosition(offset,
				assumeOpeningBrace ? STPSymbols.TokenLBRACE : peekToken(offset));

		// if we were unable to find anything, return null
		if (unit == NOT_FOUND)
			return null;

		return getLeadingWhitespace(unit);
	}

	/**
	 * Computes the indentation at <code>offset</code>.
	 *
	 * @param offset the offset in the document
	 * @return a String which reflects the correct indentation for the line in
	 *         which offset resides, or <code>null</code> if it cannot be
	 *         determined
	 */
	public StringBuilder computeIndentation(int offset) {
		return computeIndentation(offset, false);
	}

	/**
	 * Computes the indentation at <code>offset</code>.
	 *
	 * @param offset the offset in the document
	 * @param assumeOpeningBrace <code>true</code> if an opening brace should be assumed
	 * @return a String which reflects the correct indentation for the line in
	 *         which offset resides, or <code>null</code> if it cannot be
	 *         determined
	 */
	public StringBuilder computeIndentation(int offset, boolean assumeOpeningBrace) {
		StringBuilder reference= getReferenceIndentation(offset, assumeOpeningBrace);

		// handle special alignment
		if (fAlign != NOT_FOUND) {
			try {
				// a special case has been detected.
				IRegion line= fDocument.getLineInformationOfOffset(fAlign);
				int lineOffset= line.getOffset();
				return createIndent(lineOffset, fAlign, false);
			} catch (BadLocationException e) {
				return null;
			}
		}

		if (reference == null)
			return null;

		// Add additional indent
		return createReusingIndent(reference, fIndent, fExtraSpaces);
	}

	/**
	 * Computes the length of a <code>CharacterSequence</code>, counting
	 * a tab character as the size until the next tab stop and every other
	 * character as one.
	 *
	 * @param indent the string to measure
	 * @return the visual length in characters
	 */
	private int computeVisualLength(CharSequence indent) {
		final int tabSize= CodeFormatterUtil.getTabWidth();
		int length= 0;
		for (int i= 0; i < indent.length(); i++) {
			char ch= indent.charAt(i);
			switch (ch) {
			case '\t':
				if (tabSize > 0) {
					int reminder= length % tabSize;
					length += tabSize - reminder;
				}
				break;
			case ' ':
				length++;
				break;
			}
		}
		return length;
	}

	/**
	 * Strips any characters off the end of <code>reference</code> that exceed
	 * <code>indentLength</code>.
	 *
	 * @param reference the string to measure
	 * @param indentLength the maximum visual indentation length
	 * @return the stripped <code>reference</code>
	 */
	private StringBuilder stripExceedingChars(StringBuilder reference, int indentLength) {
		final int tabSize= CodeFormatterUtil.getTabWidth();
		int measured= 0;
		int chars= reference.length();
		int i= 0;
		for (; measured < indentLength && i < chars; i++) {
			char ch= reference.charAt(i);
			switch (ch) {
			case '\t':
				if (tabSize > 0) {
					int reminder= measured % tabSize;
					measured += tabSize - reminder;
				}
				break;
			case ' ':
				measured++;
				break;
			}
		}
		int deleteFrom= measured > indentLength ? i - 1 : i;

		return reference.delete(deleteFrom, chars);
	}

	/**
	 * Returns the indentation of the line at <code>offset</code> as a
	 * <code>StringBuilder</code>. If the offset is not valid, the empty string
	 * is returned.
	 *
	 * @param offset the offset in the document
	 * @return the indentation (leading whitespace) of the line in which
	 * 		   <code>offset</code> is located
	 */
	private StringBuilder getLeadingWhitespace(int offset) {
		StringBuilder indent= new StringBuilder();
		try {
			IRegion line= fDocument.getLineInformationOfOffset(offset);
			int lineOffset= line.getOffset();
			int nonWS= fScanner.findNonWhitespaceForwardInAnyPartition(lineOffset, lineOffset + line.getLength());
			indent.append(fDocument.get(lineOffset, nonWS - lineOffset));
			return indent;
		} catch (BadLocationException e) {
			return indent;
		}
	}

	/**
	 * Creates an indentation string of the length indent - start, consisting of
	 * the content in <code>fDocument</code> in the range [start, indent),
	 * with every character replaced by a space except for tabs, which are kept
	 * as such.
	 * <p>
	 * If <code>convertSpaceRunsToTabs</code> is <code>true</code>, every
	 * run of the number of spaces that make up a tab are replaced by a tab
	 * character. If it is not set, no conversion takes place, but tabs in the
	 * original range are still copied verbatim.
	 * </p>
	 *
	 * @param start the start of the document region to copy the indent from
	 * @param indent the exclusive end of the document region to copy the indent
	 *        from
	 * @param convertSpaceRunsToTabs whether to convert consecutive runs of
	 *        spaces to tabs
	 * @return the indentation corresponding to the document content specified
	 *         by <code>start</code> and <code>indent</code>
	 */
	private StringBuilder createIndent(int start, final int indent, final boolean convertSpaceRunsToTabs) {
		final boolean convertTabs= fPrefs.prefUseTabs && convertSpaceRunsToTabs;
		final int tabLen= CodeFormatterUtil.getTabWidth();
		final StringBuilder ret= new StringBuilder();
		try {
			int spaces= 0;
			while (start < indent) {
				char ch= fDocument.getChar(start);
				if (ch == '\t') {
					ret.append('\t');
					spaces= 0;
				} else if (convertTabs) {
					spaces++;
					if (spaces == tabLen) {
						ret.append('\t');
						spaces= 0;
					}
				} else {
					ret.append(' ');
				}

				start++;
			}
			// remainder
			while (spaces-- > 0)
				ret.append(' ');
		} catch (BadLocationException e) {
		}

		return ret;
	}

	/**
	 * Creates a string with a visual length of the given
	 * <code>indentationSize</code>.
	 *
	 * @param buffer the original indent to reuse if possible
	 * @param additional the additional indentation units to add or subtract to
	 *        reference
	 * @param extraSpaces additional spaces to add to indentation.
	 * @return the modified <code>buffer</code> reflecting the indentation
	 *         adapted to <code>additional</code>
	 */
	public StringBuilder createReusingIndent(StringBuilder buffer, int additional, int extraSpaces) {
		int refLength= computeVisualLength(buffer);
		int addLength= CodeFormatterUtil.getIndentWidth() * additional + extraSpaces; // may be < 0
		int totalLength= Math.max(0, refLength + addLength);

		// copy the reference indentation for the indent up to the last tab
		// stop within the maxCopy area
		int minLength= Math.min(totalLength, refLength);
		int tabSize= CodeFormatterUtil.getTabWidth();
		int maxCopyLength= tabSize > 0 ? minLength - minLength % tabSize : minLength; // maximum indent to copy
		stripExceedingChars(buffer, maxCopyLength);

		// add additional indent
		int missing= totalLength - maxCopyLength;
		int tabs, spaces;
		if (IDEPlugin.SPACE.equals(fPrefs.prefTabChar)) {
			tabs= 0;
			spaces= missing;
		} else {
			tabs= tabSize > 0 ? missing / tabSize : 0;
			spaces= tabSize > 0 ? missing % tabSize : missing;
		}
		for (int i= 0; i < tabs; i++)
			buffer.append('\t');
		for (int i= 0; i < spaces; i++)
			buffer.append(' ');
		return buffer;
	}

	/**
	 * Returns relative indent of continuation lines.
	 * @return a number of indentation units.
	 */
	public int getContinuationLineIndent() {
		return fPrefs.prefContinuationIndent;
	}

	/**
	 * Returns the reference position regarding to indentation for <code>offset</code>,
	 * or {@link STPHeuristicScanner#NOT_FOUND NOT_FOUND}. This method calls
	 * {@link #findReferencePosition(int, int) findReferencePosition(offset, nextChar)} where
	 * <code>nextChar</code> is the next character after <code>offset</code>.
	 *
	 * @param offset the offset for which the reference is computed
	 * @return the reference statement relative to which <code>offset</code>
	 *         should be indented, or {@link STPHeuristicScanner#NOT_FOUND NOT_FOUND}
	 */
	public int findReferencePosition(int offset) {
		return findReferencePosition(offset, peekToken(offset));
	}

	/**
	 * Peeks the next token in the document that comes after <code>offset</code>
	 * on the same line as <code>offset</code>.
	 *
	 * @param offset the offset into document
	 * @return the token symbol of the next element, or TokenEOF if there is none
	 */
	private int peekToken(int offset) {
		if (offset < fDocument.getLength()) {
			try {
				IRegion line= fDocument.getLineInformationOfOffset(offset);
				int lineEnd= line.getOffset() + line.getLength();
				int next= fScanner.nextToken(offset, lineEnd);
				return next;
			} catch (BadLocationException e) {
			}
		}
		return STPSymbols.TokenEOF;
	}

	/**
	 * Peeks the second next token in the document that comes after <code>offset</code>
	 * on the same line as <code>offset</code>.
	 *
	 * @param offset the offset into document
	 * @return the token symbol of the second next element, or TokenEOF if there is none
	 */
	private int peekSecondToken(int offset) {
		if (offset < fDocument.getLength()) {
			try {
				IRegion line= fDocument.getLineInformationOfOffset(offset);
				int lineEnd= line.getOffset() + line.getLength();
				fScanner.nextToken(offset, lineEnd);
				int next = fScanner.nextToken(fScanner.getPosition(), lineEnd);
				return next;
			} catch (BadLocationException e) {
			}
		}
		return STPSymbols.TokenEOF;
	}

	/**
	 * Returns the reference position regarding to indentation for <code>position</code>,
	 * or {@link STPHeuristicScanner#NOT_FOUND NOT_FOUND}.
	 *
	 * <p>If <code>peekNextChar</code> is <code>true</code>, the next token after
	 * <code>offset</code> is read and taken into account when computing the
	 * indentation. Currently, if the next token is the first token on the line
	 * (i.e. only preceded by whitespace), the following tokens are specially
	 * handled:
	 * <ul>
	 * 	<li><code>switch</code> labels are indented relative to the switch block</li>
	 * 	<li>opening curly braces are aligned correctly with the introducing code</li>
	 * 	<li>closing curly braces are aligned properly with the introducing code of
	 * 		the matching opening brace</li>
	 * 	<li>closing parenthesis' are aligned with their opening peer</li>
	 * 	<li>the <code>else</code> keyword is aligned with its <code>if</code>, anything
	 * 		else is aligned normally (i.e. with the base of any introducing statements).</li>
	 *  <li>if there is no token on the same line after <code>offset</code>, the indentation
	 * 		is the same as for an <code>else</code> keyword</li>
	 * </ul>
	 *
	 * @param offset the offset for which the reference is computed
	 * @param nextToken the next token to assume in the document
	 * @return the reference statement relative to which <code>offset</code>
	 *         should be indented, or {@link STPHeuristicScanner#NOT_FOUND NOT_FOUND}
	 */
	private int findReferencePosition(int offset, int nextToken) {
		boolean danglingElse= false;
		boolean cancelIndent= false; // If set to true, fIndent is ignored.
		int extraIndent= 0; // Can be either positive or negative.
		MatchMode matchMode = MatchMode.REGULAR;

		// Account for un-indentation characters already typed in, but after position.
		// If they are on a line by themselves, the indentation gets adjusted accordingly.
		//
		// Also account for a dangling else.
		if (offset < fDocument.getLength()) {
			try {
				IRegion line= fDocument.getLineInformationOfOffset(offset);
				int lineOffset= line.getOffset();
				int prevPos= Math.max(offset - 1, 0);
				boolean isFirstTokenOnLine=
						fDocument.get(lineOffset, prevPos + 1 - lineOffset).trim().length() == 0;
				int prevToken= fScanner.previousToken(prevPos, STPHeuristicScanner.UNBOUND);
				boolean bracelessBlockStart= fScanner.isBracelessBlockStart(prevPos, STPHeuristicScanner.UNBOUND);

				switch (nextToken) {
				case STPSymbols.TokenELSE:
					danglingElse= true;
					break;

				case STPSymbols.TokenCASE:
				case STPSymbols.TokenDEFAULT:
					if (isFirstTokenOnLine)
						matchMode = MatchMode.MATCH_CASE;
					break;

				case STPSymbols.TokenPUBLIC:
				case STPSymbols.TokenPROTECTED:
				case STPSymbols.TokenPRIVATE:
					if (isFirstTokenOnLine && peekSecondToken(offset) != STPSymbols.TokenIDENT)
						matchMode = MatchMode.MATCH_ACCESS_SPECIFIER;
					break;

				case STPSymbols.TokenLBRACE: // for opening-brace-on-new-line style
					if (bracelessBlockStart) {
						extraIndent= fPrefs.prefIndentBracesForBlocks ? 0 : -1;
					} else if (prevToken == STPSymbols.TokenCOLON && !fPrefs.prefIndentBracesForBlocks) {
						extraIndent= -1;
					} else if ((prevToken == STPSymbols.TokenEQUAL || prevToken == STPSymbols.TokenRBRACKET) &&
							!fPrefs.prefIndentBracesForArrays) {
						cancelIndent= true;
					} else if ((prevToken == STPSymbols.TokenRPAREN || prevToken == STPSymbols.TokenCONST) && fPrefs.prefIndentBracesForMethods) {
						extraIndent= 1;
					} else if (prevToken == STPSymbols.TokenIDENT) {
						if (fPrefs.prefIndentBracesForTypes) {
							extraIndent= 1;
						}
						int pos = fPosition;
						fPosition = offset;
						if (matchTypeDeclaration() != NOT_FOUND) {
							matchMode = MatchMode.MATCH_TYPE_DECLARATION;
						}
						fPosition = pos;
					}
					break;

				case STPSymbols.TokenRBRACE: // closing braces get unindented
					if (isFirstTokenOnLine || prevToken != STPSymbols.TokenLBRACE)
						matchMode = MatchMode.MATCH_BRACE;
					break;

				case STPSymbols.TokenRPAREN:
					if (isFirstTokenOnLine)
						matchMode = MatchMode.MATCH_PAREN;
					break;
				}
			} catch (BadLocationException e) {
			}
		} else {
			// Don't assume an else could come if we are at the end of file.
			danglingElse= false;
		}

		int ref= findReferencePosition(offset, danglingElse, matchMode);
		if (cancelIndent) {
			fIndent = 0;
		} else if (extraIndent > 0) {
			fAlign= NOT_FOUND;
			fIndent += extraIndent;
		} else {
			fIndent += extraIndent;
		}
		return ref;
	}

	/**
	 * Enumeration used by {@link #findReferencePosition(int, boolean, MatchMode)} method.
	 */
	public enum MatchMode {
		/**
		 * The reference position should be returned based on the regular code analysis.
		 */
		REGULAR,
		/**
		 * The position of the matching brace should be returned instead of doing code analysis.
		 */
		MATCH_BRACE,
		/**
		 * The position of the matching parenthesis should be returned instead of doing code
		 * analysis.
		 */
		MATCH_PAREN,
		/**
		 * The position of a switch statement reference should be returned (either an earlier case
		 * statement or the switch block brace).
		 */
		MATCH_CASE,
		/**
		 * The position of a class body reference should be returned (either an earlier
		 * public/protected/private or the class body brace).
		 */
		MATCH_ACCESS_SPECIFIER,
		/**
		 * The position of a class declaration should be returned.
		 */
		MATCH_TYPE_DECLARATION
	}

	/**
	 * Returns the reference position regarding to indentation for <code>position</code>,
	 * or {@link STPHeuristicScanner#NOT_FOUND NOT_FOUND}. <code>fIndent</code> will contain
	 * the relative indentation (in indentation units, not characters) after the call. If there is
	 * a special alignment (e.g. for a method declaration where parameters should be aligned),
	 * <code>fAlign</code> will contain the absolute position of the alignment reference
	 * in <code>fDocument</code>, otherwise <code>fAlign</code> is set to
	 * {@link STPHeuristicScanner#NOT_FOUND}.
	 *
	 * @param offset the offset for which the reference is computed
	 * @param danglingElse whether a dangling else should be assumed at <code>position</code>
	 * @param matchMode determines what kind of reference position should be returned.
	 *     See {@link MatchMode}.
	 * @return the reference statement relative to which <code>position</code>
	 *         should be indented, or {@link STPHeuristicScanner#NOT_FOUND}
	 */
	public int findReferencePosition(int offset, boolean danglingElse, MatchMode matchMode) {
		fIndent= 0; // The indentation modification
		fExtraSpaces= 0;
		fAlign= NOT_FOUND;
		fPosition= offset;

		// Forward cases.
		// An unindentation happens sometimes if the next token is special, namely on braces,
		// parens and case labels align braces, but handle the case where we align with the method
		// declaration start instead of the opening brace.
		switch (matchMode) {
		case MATCH_BRACE:
			if (skipScope(STPSymbols.TokenLBRACE, STPSymbols.TokenRBRACE)) {
				try {
					// Align with the opening brace that is on a line by its own
					int lineOffset= fDocument.getLineOffset(fLine);
					if (lineOffset <= fPosition &&
							fDocument.get(lineOffset, fPosition - lineOffset).trim().isEmpty()) {
						return fPosition;
					}
				} catch (BadLocationException e) {
					// Concurrent modification - walk default path
				}
				// If the opening brace is not on the start of the line, skip to the start.
				int pos= skipToStatementStart(true, true);
				fIndent= 0; // indent is aligned with reference position
				return pos;
			} else {
				// If we can't find the matching brace, the heuristic is to unindent
				// by one against the normal position
				int pos= findReferencePosition(offset, danglingElse, MatchMode.REGULAR);
				fIndent--;
				return pos;
			}

		case MATCH_PAREN:
			// Align parentheses.
			if (skipScope(STPSymbols.TokenLPAREN, STPSymbols.TokenRPAREN)) {
				return fPosition;
			} else {
				// If we can't find the matching paren, the heuristic is to unindent by one
				// against the normal position.
				int pos= findReferencePosition(offset, danglingElse, MatchMode.REGULAR);
				fIndent--;
				return pos;
			}

		case MATCH_CASE:
			// The only reliable way to get case labels aligned (due to many different styles of
			// using braces in a block) is to go for another case statement, or the scope opening
			// brace.
			return matchCaseAlignment();

		case MATCH_ACCESS_SPECIFIER:
			// The only reliable way to get access specifiers aligned (due to many different styles
			// of using braces in a block) is to go for another access specifier, or the scope
			// opening brace.
			return matchAccessSpecifierAlignment();

		case MATCH_TYPE_DECLARATION:
			return matchTypeDeclaration();

		case REGULAR:
			break;
		}

		if (peekToken(offset) == STPSymbols.TokenCOLON) {
			int pos= fPosition;
			if (looksLikeTypeInheritanceDecl()) {
				fIndent = fPrefs.prefContinuationIndent;
				return fPosition;
			}
			fPosition = pos;
		}

		nextToken();
		// Skip access specifiers
		while (fToken == STPSymbols.TokenCOLON && isAccessSpecifier()) {
			nextToken();
		}

		int line= fLine;
		switch (fToken) {
		case STPSymbols.TokenGREATERTHAN:
		case STPSymbols.TokenRBRACE:
			// Skip the block and fall through.
			// If we can't complete the scope, reset the scan position
			int pos= fPosition;
			if (!skipScope())
				fPosition= pos;
			return skipToStatementStart(danglingElse, false);

		case STPSymbols.TokenSEMICOLON:
			// This is the 90% case: after a statement block
			// the end of the previous statement / block previous.end
			// search to the end of the statement / block before the previous;
			// the token just after that is previous.start
			return skipToStatementStart(danglingElse, false);

		// Scope introduction: special treat who special is
		case STPSymbols.TokenLPAREN:
		case STPSymbols.TokenLBRACE:
		case STPSymbols.TokenLBRACKET:
			return handleScopeIntroduction(Math.min(offset + 1, fDocument.getLength()), true);

		case STPSymbols.TokenEOF:
			// trap when hitting start of document
			return NOT_FOUND;

		case STPSymbols.TokenEQUAL:
			// indent assignments, but don't do so if there is a String
			// after the assignment because SystemTap doesn't require
			// semi-colons to end lines and so this should be treated as
			// a complete assignment.
			pos = fPosition;
			while (pos < offset) {
				try {
					ITypedRegion partition = ((IDocumentExtension3)fDocument).getPartition(STPPartitionScanner.STP_PARTITIONING, pos, danglingElse);
					if (STPPartitionScanner.STP_STRING.equals(partition.getType()))
						return skipToStatementStart(danglingElse, false);
					pos = partition.getOffset() + partition.getLength();
				} catch (BadLocationException e) {
					break;
				} catch (BadPartitioningException e) {
					break;
				}
			}
			fIndent= fPrefs.prefAssignmentIndent;
			return fPosition;
		case STPSymbols.TokenCOLON:
			pos= fPosition;
			if (looksLikeCaseStatement()) {
				fIndent= fPrefs.prefCaseBlockIndent;
				return pos;
			}
			fPosition= pos;
			if (looksLikeTypeInheritanceDecl()) {
				fIndent= fPrefs.prefContinuationIndent;
				return pos;
			}
			fPosition= pos;
			if (looksLikeConstructorInitializer()) {
				fIndent= fPrefs.prefBlockIndent;
				return pos;
			}
			fPosition= pos;
			if (isConditional()) {
				fPosition= offset;
				fLine= line;
				return skipToPreviousListItemOrListStart();
			}
			fPosition= pos;
			return skipToPreviousListItemOrListStart();

		case STPSymbols.TokenQUESTIONMARK:
			if (fPrefs.prefTernaryDeepAlign) {
				setFirstElementAlignment(fPosition, offset + 1);
			} else {
				fIndent= fPrefs.prefTernaryIndent;
			}
			return fPosition;

		// Indentation for blockless introducers:
		case STPSymbols.TokenDO:
		case STPSymbols.TokenWHILE:
		case STPSymbols.TokenELSE:
			fIndent= fPrefs.prefSimpleIndent;
			return fPosition;

		case STPSymbols.TokenTRY:
			return skipToStatementStart(danglingElse, false);

		case STPSymbols.TokenRETURN:
		case STPSymbols.TokenTYPEDEF:
		case STPSymbols.TokenUSING:
			fIndent = fPrefs.prefContinuationIndent;
			return fPosition;

		case STPSymbols.TokenCONST:
			nextToken();
			if (fToken != STPSymbols.TokenRPAREN) {
				return skipToPreviousListItemOrListStart();
			}
			// could be const method decl
			//$FALL-THROUGH$
		case STPSymbols.TokenRPAREN:
			if (skipScope(STPSymbols.TokenLPAREN, STPSymbols.TokenRPAREN)) {
				int scope= fPosition;
				nextToken();
				if (fToken == STPSymbols.TokenIF || fToken == STPSymbols.TokenWHILE || fToken == STPSymbols.TokenFOR
						|| fToken == STPSymbols.TokenFOREACH) {
					fIndent= fPrefs.prefSimpleIndent;
					return fPosition;
				}
				if (fToken == STPSymbols.TokenSWITCH) {
					return fPosition;
				}
				fPosition= scope;
				if (looksLikeMethodDecl()) {
					return skipToStatementStart(danglingElse, false);
				}
				if (fToken == STPSymbols.TokenCATCH) {
					return skipToStatementStart(danglingElse, false);
				}
				fPosition= scope;
				if (looksLikeAnonymousTypeDecl()) {
					return skipToStatementStart(danglingElse, false);
				}
			}
			// restore
			fPosition= offset;
			fLine= line;
			// else: fall through to default
			return skipToPreviousListItemOrListStart();

		case STPSymbols.TokenCOMMA:
			// Inside a list of some type.
			// Easy if there is already a list item before with its own indentation - we just align.
			// If not: take the start of the list (LPAREN, LBRACE, LBRACKET) and either align or
			// indent by list-indent.
			return skipToPreviousListItemOrListStart();

		case STPSymbols.TokenPLUS:
		case STPSymbols.TokenMINUS:
		case STPSymbols.TokenLESSTHAN:
		case STPSymbols.TokenAGGREGATE:
		case STPSymbols.TokenSHIFTRIGHT:
		case STPSymbols.TokenSHIFTLEFT:
		case STPSymbols.TokenOTHER:
			// Math symbol, use skipToPreviousListItemOrListStart.
			return skipToPreviousListItemOrListStart();
			// Otherwise, fall-through
		default:
			// Inside whatever we don't know about:
			// C would treat this as a list, but in SystemTap we might just have a line that doesn't
			// end in a semi-colon.  We want to indent to the same level as the statement.
			return skipToStatementStart(danglingElse, false);
		}
	}

	/**
	 * Test whether an identifier encountered during scanning is part of
	 * a type declaration, by scanning backward and ignoring any identifiers, commas,
	 * and colons until we hit <code>class</code>, <code>struct</code>, <code>union</code>,
	 * or <code>enum</code>.  If any braces, semicolons, or parentheses are encountered,
	 * this is not a type declaration.
	 * @return the reference offset of the start of the statement
	 */
	private int matchTypeDeclaration() {
		while (true) {
			nextToken();
			if (fToken == STPSymbols.TokenIDENT
					|| fToken == STPSymbols.TokenCOMMA
					|| fToken == STPSymbols.TokenCOLON
					|| fToken == STPSymbols.TokenPUBLIC
					|| fToken == STPSymbols.TokenPROTECTED
					|| fToken == STPSymbols.TokenPRIVATE) {
				continue;
			}
			if (fToken == STPSymbols.TokenCLASS
					|| fToken == STPSymbols.TokenSTRUCT
					|| fToken == STPSymbols.TokenUNION) {
				// inside a type declaration?  Only so if not preceded by '(' or ',' as in
				// a parameter list.  To be safe, only accept ';' or EOF
				int pos= fPosition;
				nextToken();
				if (fToken == STPSymbols.TokenSEMICOLON || fToken == STPSymbols.TokenEOF) {
					return pos;
				} else {
					return NOT_FOUND;
				}
			} else {
				return NOT_FOUND;
			}
		}
	}

	/**
	 * Test whether the colon at the current position marks a case statement
	 *
	 * @return <code>true</code> if this looks like a case statement
	 */
	private boolean looksLikeCaseStatement() {
		nextToken();
		switch (fToken) {
		case STPSymbols.TokenCASE:
			// char literal got skipped
			return true;
		case STPSymbols.TokenIDENT:
			nextToken();
			while (skipQualifiers()) {
				nextToken();
			}
			while (fToken == STPSymbols.TokenMINUS || fToken == STPSymbols.TokenPLUS) {
				nextToken();
			}
			if (fToken == STPSymbols.TokenCASE) {
				return true;
			}
			break;
		case STPSymbols.TokenOTHER:
			nextToken();
			if (fToken == STPSymbols.TokenCASE) {
				return true;
			}
			break;
		case STPSymbols.TokenDEFAULT:
			return true;
		}
		return false;
	}

	/**
	 * Test whether the colon at the current position marks a type inheritance decl.
	 *
	 * @return <code>true</code> if this looks like a type inheritance decl.
	 */
	private boolean looksLikeTypeInheritanceDecl() {
		nextToken();
		switch (fToken) {
		case STPSymbols.TokenIDENT:
			nextToken();
			while (skipQualifiers()) {
				nextToken();
			}
			switch (fToken) {
			case STPSymbols.TokenCLASS:
			case STPSymbols.TokenSTRUCT:
			case STPSymbols.TokenUNION:
				return true;
			}
			break;
		}
		return false;
	}

	/**
	 * Test whether the colon at the current position marks a constructor initializer list.
	 *
	 * @return <code>true</code> if this looks like a constructor initializer list.
	 */
	private boolean looksLikeConstructorInitializer() {
		nextToken();
		if (fToken != STPSymbols.TokenRPAREN) {
			return false;
		}
		if (!skipScope()) {
			return false;
		}
		nextToken();
		if (fToken == STPSymbols.TokenTHROW) {
			nextToken();
			if (fToken != STPSymbols.TokenRPAREN) {
				return false;
			}
			if (!skipScope()) {
				return false;
			}
			nextToken();
		}
		if (fToken != STPSymbols.TokenIDENT) {
			return false;
		}
		nextToken();
		switch (fToken) {
		case STPSymbols.TokenCOLON:
			nextToken();
			switch (fToken) {
			case STPSymbols.TokenCOLON:  // A::A() :
			case STPSymbols.TokenPUBLIC:  // public: A() :
			case STPSymbols.TokenPROTECTED:
			case STPSymbols.TokenPRIVATE:
				return true;
			}
			return false;

		case STPSymbols.TokenLBRACE:  // class A { A() :
		case STPSymbols.TokenRBRACE:
		case STPSymbols.TokenSEMICOLON:
			return true;
		}
		return false;
	}

	/**
	 * Test whether the left brace at the current position marks an enum decl.
	 *
	 * @return <code>true</code> if this looks like an enum decl.
	 */
	private boolean looksLikeEnumDeclaration() {
		int pos = fPosition;
		nextToken();
		if (fToken == STPSymbols.TokenIDENT) {
			nextToken();
			while (skipQualifiers()) {
				nextToken();
			}
		}
		if (fToken == STPSymbols.TokenENUM) {
			fPosition = pos;
			return true;
		}
		fPosition = pos;
		return false;
	}


	/**
	 * Test whether the colon at the current position marks an access specifier.
	 *
	 * @return <code>true</code> if current position marks an access specifier
	 */
	private boolean isAccessSpecifier() {
		int pos= fPosition;
		int token = fToken;
		nextToken();
		switch (fToken) {
		case STPSymbols.TokenPUBLIC:
		case STPSymbols.TokenPROTECTED:
		case STPSymbols.TokenPRIVATE:
			return true;
		}
		fToken = token;
		fPosition= pos;
		return false;
	}

	/**
	 * Skips to the start of a statement that ends at the current position.
	 *
	 * @param danglingElse whether to indent aligned with the last <code>if</code>
	 * @param isInBlock whether the current position is inside a block, which limits the search scope to
	 *   the next scope introducer
	 * @return the reference offset of the start of the statement
	 */
	private int skipToStatementStart(boolean danglingElse, boolean isInBlock) {
		final int NOTHING= 0;
		final int READ_PARENS= 1;
		final int READ_IDENT= 2;
		int mayBeMethodBody= NOTHING;
		boolean isTypeBody= false;
		int startLine = fLine;
		while (true) {
			int prevToken= fToken;
			nextToken();

			if (isInBlock) {
				switch (fToken) {
				// exit on all block introducers
				case STPSymbols.TokenIF:
				case STPSymbols.TokenELSE:
				case STPSymbols.TokenCATCH:
				case STPSymbols.TokenDO:
				case STPSymbols.TokenWHILE:
				case STPSymbols.TokenFOR:
				case STPSymbols.TokenFOREACH:
				case STPSymbols.TokenTRY:
					fIndent += fPrefs.prefIndentBracesForBlocks ? 1 : 0;
					return fPosition;
				case STPSymbols.TokenCLASS:
				case STPSymbols.TokenSTRUCT:
				case STPSymbols.TokenUNION:
					isTypeBody= true;
					break;

				case STPSymbols.TokenSWITCH:
					fIndent= fPrefs.prefCaseIndent;
					return fPosition;
				}
			}

			if (fToken == STPSymbols.TokenSEMICOLON && fLine == startLine) {
				// Skip semicolons on the same line. Otherwise we may never reach beginning of a 'for'
				// statement.
				continue;
			}

			switch (fToken) {
			// scope introduction through: LPAREN, LBRACE, LBRACKET
			// search stop on SEMICOLON, RBRACE, COLON, EOF
			// -> the next token is the start of the statement (i.e. previousPos when backward scanning)
			case STPSymbols.TokenLPAREN:
				if (peekToken() == STPSymbols.TokenFOR) {
					nextToken();  // Consume 'for'
					fIndent = fPrefs.prefContinuationIndent;
					return fPosition;
				}
				break;

			case STPSymbols.TokenLBRACE:
			case STPSymbols.TokenSEMICOLON:
			case STPSymbols.TokenEOF:
				if (isInBlock)
					fIndent= getBlockIndent(mayBeMethodBody == READ_IDENT, isTypeBody);
				return fPreviousPos;

			case STPSymbols.TokenCOLON:
				switch (prevToken) {
				case STPSymbols.TokenPRIVATE:
				case STPSymbols.TokenPROTECTED:
				case STPSymbols.TokenPUBLIC:
					continue; // Don't stop at colon in a class declaration

				case STPSymbols.TokenVIRTUAL:
					switch (peekToken()) {
					case STPSymbols.TokenPRIVATE:
					case STPSymbols.TokenPROTECTED:
					case STPSymbols.TokenPUBLIC:
						break;
					default:
						continue;
					}
				}
				int pos= fPreviousPos;
				if (!isConditional())
					return pos;
				break;

			case STPSymbols.TokenRBRACE:
				// RBRACE is a little tricky: it can be the end of an array definition, but
				// usually it is the end of a previous block
				pos= fPreviousPos; // store state
				if (skipScope()) {
					if (looksLikeArrayInitializerIntro()) {
						continue; // it's an array
					}
					if (prevToken == STPSymbols.TokenSEMICOLON) {
						// end of type def
						continue;
					}
				}
				if (isInBlock)
					fIndent= getBlockIndent(mayBeMethodBody == READ_IDENT, isTypeBody);
				return pos; // it's not - do as with all the above

			// scopes: skip them
			case STPSymbols.TokenRPAREN:
				if (isInBlock)
					mayBeMethodBody= READ_PARENS;
				// fall thru
				pos= fPreviousPos;
				if (skipScope())
					break;
				else
					return pos;
			case STPSymbols.TokenRBRACKET:
				pos= fPreviousPos;
				if (skipScope())
					break;
				else
					return pos;

			// IF / ELSE: align the position after the conditional block with the if
			// so we are ready for an else, except if danglingElse is false
			// in order for this to work, we must skip an else to its if
			case STPSymbols.TokenIF:
				if (danglingElse)
					return fPosition;
				else
					break;
			case STPSymbols.TokenELSE:
				// skip behind the next if, as we have that one covered
				pos= fPosition;
				if (skipNextIF())
					break;
				else
					return pos;

			case STPSymbols.TokenDO:
				// align the WHILE position with its do
				return fPosition;

			case STPSymbols.TokenWHILE:
				// this one is tricky: while can be the start of a while loop
				// or the end of a do - while
				pos= fPosition;
				if (hasMatchingDo()) {
					// continue searching from the DO on
					break;
				} else {
					// continue searching from the WHILE on
					fPosition= pos;
					break;
				}
			case STPSymbols.TokenIDENT:
				if (mayBeMethodBody == READ_PARENS)
					mayBeMethodBody= READ_IDENT;
				break;

			default:
				// keep searching
			}
		}
	}

	private int getBlockIndent(boolean isMethodBody, boolean isTypeBody) {
		if (isTypeBody) {
			return fPrefs.prefTypeIndent + fPrefs.prefAccessSpecifierIndent;
		} else if (isMethodBody) {
			return fPrefs.prefMethodBodyIndent + (fPrefs.prefIndentBracesForMethods ? 1 : 0);
		} else {
			return fIndent;
		}
	}

	/**
	 * Returns <code>true</code> if the colon at the current position is part of a conditional
	 * (ternary) expression, <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if the colon at the current position is part of a conditional
	 */
	private boolean isConditional() {
		while (true) {
			int previous= fToken;
			nextToken();
			switch (fToken) {
			// search for case labels, which consist of (possibly qualified) identifiers or numbers
			case STPSymbols.TokenIDENT:
				if (previous == STPSymbols.TokenIDENT) {
					return false;
				}
				// fall thru
				continue;
			case STPSymbols.TokenDOUBLECOLON:
			case STPSymbols.TokenOTHER:
			case STPSymbols.TokenMINUS:
			case STPSymbols.TokenPLUS:
				continue;

			case STPSymbols.TokenQUESTIONMARK:
				return true;

			case STPSymbols.TokenSEMICOLON:
			case STPSymbols.TokenLBRACE:
			case STPSymbols.TokenRBRACE:
			case STPSymbols.TokenCASE:
			case STPSymbols.TokenDEFAULT:
			case STPSymbols.TokenPUBLIC:
			case STPSymbols.TokenPROTECTED:
			case STPSymbols.TokenPRIVATE:
			case STPSymbols.TokenCLASS:
			case STPSymbols.TokenSTRUCT:
			case STPSymbols.TokenUNION:
				return false;

			default:
				return true;
			}
		}
	}

	/**
	 * Returns as a reference any previous <code>switch</code> labels (<code>case</code>
	 * or <code>default</code>) or the offset of the brace that scopes the switch
	 * statement. Sets <code>fIndent</code> to <code>prefCaseIndent</code> upon
	 * a match.
	 *
	 * @return the reference offset for a <code>switch</code> label
	 */
	private int matchCaseAlignment() {
		while (true) {
			nextToken();
			switch (fToken) {
			// invalid cases: another case label or an LBRACE must come before a case
			// -> bail out with the current position
			case STPSymbols.TokenLPAREN:
			case STPSymbols.TokenLBRACKET:
			case STPSymbols.TokenEOF:
				return fPosition;

			case STPSymbols.TokenSWITCH:
				// start of switch statement
				fIndent= fPrefs.prefCaseIndent;
				return fPosition;

			case STPSymbols.TokenCASE:
			case STPSymbols.TokenDEFAULT:
				// align with previous label
				fIndent= 0;
				return fPosition;

			// scopes: skip them
			case STPSymbols.TokenRPAREN:
			case STPSymbols.TokenRBRACKET:
			case STPSymbols.TokenRBRACE:
				skipScope();
				break;

			default:
				// keep searching
				continue;
			}
		}
	}

	/**
	 * Returns as a reference any previous access specifiers (<code>public</code>,
	 * <code>protected</code> or <code>default</code>) or the offset of the brace that
	 * scopes the class body.
	 * Sets <code>fIndent</code> to <code>prefAccessSpecifierIndent</code> upon
	 * a match.
	 *
	 * @return the reference offset for an access specifier (public/protected/private)
	 */
	private int matchAccessSpecifierAlignment() {
		while (true) {
			nextToken();
			switch (fToken) {
			// invalid cases: another access specifier or an LBRACE must come before an access specifier
			// -> bail out with the current position
			case STPSymbols.TokenLPAREN:
			case STPSymbols.TokenLBRACKET:
			case STPSymbols.TokenEOF:
				return fPosition;

			case STPSymbols.TokenLBRACE:
				// opening brace of class body
				int pos= fPosition;
				int typeDeclPos= matchTypeDeclaration();
				fIndent= fPrefs.prefAccessSpecifierIndent;
				fExtraSpaces = fPrefs.prefAccessSpecifierExtraSpaces;
				if (typeDeclPos != NOT_FOUND) {
					return typeDeclPos;
				}
				return pos;
			case STPSymbols.TokenPUBLIC:
			case STPSymbols.TokenPROTECTED:
			case STPSymbols.TokenPRIVATE:
				// align with previous access specifier
				fIndent= 0;
				return fPosition;

			// scopes: skip them
			case STPSymbols.TokenRPAREN:
			case STPSymbols.TokenRBRACKET:
			case STPSymbols.TokenRBRACE:
				skipScope();
				break;

			default:
				// keep searching
				continue;
			}
		}
	}

	/**
	 * Returns the reference position for a list element. The algorithm
	 * tries to match any previous indentation on the same list. If there is none,
	 * the reference position returned is determined depending on the type of list:
	 * The indentation will either match the list scope introducer (e.g. for
	 * method declarations), so called deep indents, or simply increase the
	 * indentation by a number of standard indents. See also {@link #handleScopeIntroduction(int, boolean)}.
	 * @return the reference position for a list item: either a previous list item
	 * that has its own indentation, or the list introduction start.
	 */
	private int skipToPreviousListItemOrListStart() {
		int startLine= fLine;
		int startPosition= fPosition;
		int linesSkippedInsideScopes = 0;
		boolean continuationLineCandidate =
				fToken == STPSymbols.TokenEQUAL || fToken == STPSymbols.TokenSHIFTLEFT ||
				fToken == STPSymbols.TokenRPAREN;
		while (true) {
			int previous = fToken;
			nextToken();

			// If any line item comes with its own indentation, adapt to it
			if (fLine < startLine - linesSkippedInsideScopes) {
				try {
					int lineOffset= fDocument.getLineOffset(startLine);
					int bound= Math.min(fDocument.getLength(), startPosition + 1);
					if ((fToken == STPSymbols.TokenSEMICOLON || fToken == STPSymbols.TokenRBRACE || fToken == STPSymbols.TokenIDENT ||
							fToken == STPSymbols.TokenLBRACE && !looksLikeArrayInitializerIntro() && !looksLikeEnumDeclaration()) &&
							continuationLineCandidate) {
						fIndent = fPrefs.prefContinuationIndent;
					} else {
						fAlign= fScanner.findNonWhitespaceForwardInAnyPartition(lineOffset, bound);
						// If the reference line starts with a colon, skip the colon.
						if (peekToken(fAlign) == STPSymbols.TokenCOLON) {
							fAlign= fScanner.findNonWhitespaceForwardInAnyPartition(fAlign + 1, bound);
						}
					}
				} catch (BadLocationException e) {
					// Ignore and return just the position
				}
				return startPosition;
			}

			int line = fLine;
			switch (fToken) {
			// scopes: skip them
			case STPSymbols.TokenRPAREN:
				continuationLineCandidate = true;
				//$FALL-THROUGH$
			case STPSymbols.TokenRBRACKET:
			case STPSymbols.TokenRBRACE:
				skipScope();
				linesSkippedInsideScopes = line - fLine;
				break;

			// scope introduction: special treat who special is
			case STPSymbols.TokenLPAREN:
			case STPSymbols.TokenLBRACE:
			case STPSymbols.TokenLBRACKET:
				return handleScopeIntroduction(startPosition + 1, false);

			case STPSymbols.TokenSEMICOLON:
				return fPosition;

			case STPSymbols.TokenQUESTIONMARK:
				if (fPrefs.prefTernaryDeepAlign) {
					setFirstElementAlignment(fPosition - 1, fPosition + 1);
				} else {
					fIndent= fPrefs.prefTernaryIndent;
				}
				return fPosition;

			case STPSymbols.TokenEQUAL:
			case STPSymbols.TokenSHIFTLEFT:
				continuationLineCandidate = true;
				break;

			case STPSymbols.TokenRETURN:
			case STPSymbols.TokenUSING:
				fIndent = fPrefs.prefContinuationIndent;
				return fPosition;

			case STPSymbols.TokenTYPEDEF:
				switch (previous) {
				case STPSymbols.TokenSTRUCT:
				case STPSymbols.TokenUNION:
				case STPSymbols.TokenCLASS:
				case STPSymbols.TokenENUM:
					break;
				default:
					fIndent = fPrefs.prefContinuationIndent;
				}
				return fPosition;

			case STPSymbols.TokenEOF:
				if (continuationLineCandidate) {
					fIndent = fPrefs.prefContinuationIndent;
				}
				return 0;
			}
		}
	}

	/**
	 * Skips a scope and positions the cursor (<code>fPosition</code>) on the
	 * token that opens the scope. Returns <code>true</code> if a matching peer
	 * could be found, <code>false</code> otherwise. The current token when calling
	 * must be one out of <code>STPSymbols.TokenRPAREN</code>, <code>STPSymbols.TokenRBRACE</code>,
	 * and <code>STPSymbols.TokenRBRACKET</code>.
	 *
	 * @return <code>true</code> if a matching peer was found, <code>false</code> otherwise
	 */
	private boolean skipScope() {
		switch (fToken) {
		case STPSymbols.TokenRPAREN:
			return skipScope(STPSymbols.TokenLPAREN, STPSymbols.TokenRPAREN);
		case STPSymbols.TokenRBRACKET:
			return skipScope(STPSymbols.TokenLBRACKET, STPSymbols.TokenRBRACKET);
		case STPSymbols.TokenRBRACE:
			return skipScope(STPSymbols.TokenLBRACE, STPSymbols.TokenRBRACE);
		case STPSymbols.TokenGREATERTHAN:
			if (!fPrefs.prefHasTemplates)
				return false;
			int storedPosition= fPosition;
			int storedToken= fToken;
			nextToken();
			switch (fToken) {
				case STPSymbols.TokenIDENT:
					fPosition = storedPosition;
					if (skipScope(STPSymbols.TokenLESSTHAN, STPSymbols.TokenGREATERTHAN))
						return true;
					break;
				case STPSymbols.TokenQUESTIONMARK:
				case STPSymbols.TokenGREATERTHAN:
					fPosition = storedPosition;
					if (skipScope(STPSymbols.TokenLESSTHAN, STPSymbols.TokenGREATERTHAN))
						return true;
					break;
			}
			// <> are harder to detect - restore the position if we fail
			fPosition= storedPosition;
			fToken= storedToken;
			return false;

		default:
			 // programming error
			Assert.isTrue(false);
			return false;
		}
	}

	/**
	 * Returns the contents of the current token.
	 *
	 * @return the contents of the current token
	 */
	private CharSequence getTokenContent() {
		return new DocumentCharacterIterator(fDocument, fPosition, fPreviousPos);
	}

	/**
	 * Handles the introduction of a new scope. The current token must be one out
	 * of <code>STPSymbols.TokenLPAREN</code>, <code>STPSymbols.TokenLBRACE</code>,
	 * and <code>STPSymbols.TokenLBRACKET</code>. Returns as the reference position
	 * either the token introducing the scope or - if available - the first
	 * token after that.
	 *
	 * <p>Depending on the type of scope introduction, the indentation will align
	 * (deep indenting) with the reference position (<code>fAlign</code> will be
	 * set to the reference position) or <code>fIndent</code> will be set to
	 * the number of indentation units.
	 * </p>
	 *
	 * @param bound the bound for the search for the first token after the scope
	 * introduction.
	 * @param firstToken <code>true</code> if we are dealing with the first token after
	 * the opening parenthesis.
	 * @return the indent
	 */
	private int handleScopeIntroduction(int bound, boolean firstToken) {
		int pos= fPosition; // store

		switch (fToken) {
		// scope introduction: special treat who special is
		case STPSymbols.TokenLPAREN:
			// special: method declaration deep indentation
			if (looksLikeMethodDecl()) {
				if (firstToken ? fPrefs.prefMethodDeclFirstParameterDeepIndent : fPrefs.prefMethodDeclDeepIndent) {
					return setFirstElementAlignment(pos, bound);
				} else {
					fIndent= fPrefs.prefMethodDeclIndent;
					return pos;
				}
			} else {
				fPosition= pos;
				if (looksLikeMethodCall()) {
					if (firstToken ? fPrefs.prefMethodCallFirstParameterDeepIndent : fPrefs.prefMethodCallDeepIndent) {
						return setFirstElementAlignment(pos, bound);
					} else {
						fIndent= fPrefs.prefMethodCallIndent;
						return pos;
					}
				} else if (fPrefs.prefParenthesisDeepIndent) {
					return setFirstElementAlignment(pos, bound);
				}
			}

			// normal: return the parenthesis as reference
			fIndent= fPrefs.prefParenthesisIndent;
			return pos;

		case STPSymbols.TokenLBRACE:
			final boolean looksLikeArrayInitializerIntro= looksLikeArrayInitializerIntro();
			// special: array initializer
			if (looksLikeArrayInitializerIntro) {
				if (fPrefs.prefArrayDeepIndent)
					return setFirstElementAlignment(pos, bound);
				else
					fIndent= fPrefs.prefArrayIndent;
			} else if (isNamespace() || isLinkageSpec()) {
				fIndent= fPrefs.prefNamespaceBodyIndent;
			} else if (looksLikeEnumDeclaration()) {
				fIndent = fPrefs.prefTypeIndent;
			} else {
				int typeDeclPos = matchTypeDeclaration();
				if (typeDeclPos == NOT_FOUND) {
					fIndent= fPrefs.prefBlockIndent;
				} else {
					fIndent= fPrefs.prefAccessSpecifierIndent + fPrefs.prefTypeIndent;
				}
			}

			// normal: skip to the statement start before the scope introducer
			// opening braces are often on differently ending indents than e.g. a method definition
			if (!looksLikeArrayInitializerIntro) {
				fPosition= pos; // restore
				return skipToStatementStart(true, true); // set to true to match the first if
			} else {
				return pos;
			}

		case STPSymbols.TokenLBRACKET:
			// special: method declaration deep indentation
			if (fPrefs.prefArrayDimensionsDeepIndent) {
				return setFirstElementAlignment(pos, bound);
			}

			// normal: return the bracket as reference
			fIndent= fPrefs.prefBracketIndent;
			return pos; // restore

		default:
			// programming error
			Assert.isTrue(false);
			return -1; // dummy
		}
	}

	/**
	 * Sets the deep indent offset (<code>fAlign</code>) to either the offset
	 * right after <code>scopeIntroducerOffset</code> or - if available - the
	 * first C token after <code>scopeIntroducerOffset</code>, but before
	 * <code>bound</code>.
	 *
	 * @param scopeIntroducerOffset the offset of the scope introducer
	 * @param bound the bound for the search for another element
	 * @return the reference position
	 */
	private int setFirstElementAlignment(int scopeIntroducerOffset, int bound) {
		int firstPossible= scopeIntroducerOffset + 1; // align with the first position after the scope intro
		fAlign= fScanner.findNonWhitespaceForwardInAnyPartition(firstPossible, bound);
		if (fAlign == NOT_FOUND) {
			fAlign= firstPossible;
		} else {
			try {
				IRegion lineRegion = fDocument.getLineInformationOfOffset(scopeIntroducerOffset);
				if (fAlign > lineRegion.getOffset() + lineRegion.getLength()) {
					fAlign= firstPossible;
				}
			} catch (BadLocationException e) {
				// Ignore.
			}
		}
		return fAlign;
	}

	/**
	 * Returns <code>true</code> if the next token received after calling
	 * <code>nextToken</code> is either an equal sign, an opening brace,
	 * a comma or an array designator ('[]').
	 *
	 * @return <code>true</code> if the next elements look like the start of an array definition
	 */
	private boolean looksLikeArrayInitializerIntro() {
		int pos= fPosition;
		nextToken();
		switch (fToken) {
		case STPSymbols.TokenEQUAL:
			return true;
		case STPSymbols.TokenRBRACKET:
			return skipBrackets();
		case STPSymbols.TokenLBRACE:
			if (looksLikeArrayInitializerIntro()) {
				fPosition= pos;
				return true;
			}
			return false;
		case STPSymbols.TokenCOMMA:
			fPosition= pos;
			return true;
		}
		fPosition= pos;
		return false;
	}

	/**
	 * Returns <code>true</code> if the the current token is "namespace", or the current token
	 * is an identifier and the previous token is "namespace".
	 *
	 * @return <code>true</code> if the next elements look like the start of a namespace declaration.
	 */
	private boolean isNamespace() {
		int pos = fPosition;
		nextToken();
		if (fToken == STPSymbols.TokenNAMESPACE) {
			fPosition = pos;
			return true;		// Anonymous namespace
		} else if (fToken == STPSymbols.TokenIDENT) {
			nextToken();		// Get previous token
			if (fToken == STPSymbols.TokenNAMESPACE) {
				fPosition = pos;
				return true;	// Named namespace
			}
		}
		fPosition = pos;
		return false;
	}

	/**
	 * Returns <code>true</code> if the current token is keyword "extern".
	 *
	 * @return <code>true</code> if the next elements look like the start of a linkage spec.
	 */
	private boolean isLinkageSpec() {
		int pos = fPosition;
		nextToken();
		if (fToken == STPSymbols.TokenEXTERN) {
			fPosition = pos;
			return true;
		}
		fPosition = pos;
		return false;
	}

	/**
	 * Skips over the next <code>if</code> keyword. The current token when calling
	 * this method must be an <code>else</code> keyword. Returns <code>true</code>
	 * if a matching <code>if</code> could be found, <code>false</code> otherwise.
	 * The cursor (<code>fPosition</code>) is set to the offset of the <code>if</code>
	 * token.
	 *
	 * @return <code>true</code> if a matching <code>if</code> token was found, <code>false</code> otherwise
	 */
	private boolean skipNextIF() {
		Assert.isTrue(fToken == STPSymbols.TokenELSE);

		while (true) {
			nextToken();
			switch (fToken) {
			// scopes: skip them
			case STPSymbols.TokenRPAREN:
			case STPSymbols.TokenRBRACKET:
			case STPSymbols.TokenRBRACE:
				skipScope();
				break;

			case STPSymbols.TokenIF:
				// found it, return
				return true;
			case STPSymbols.TokenELSE:
				// recursively skip else-if blocks
				skipNextIF();
				break;

			// shortcut scope starts
			case STPSymbols.TokenLPAREN:
			case STPSymbols.TokenLBRACE:
			case STPSymbols.TokenLBRACKET:
			case STPSymbols.TokenEOF:
				return false;
			}
		}
	}

	/**
	 * while(condition); is ambiguous when parsed backwardly, as it is a valid
	 * statement by its own, so we have to check whether there is a matching
	 * do. A <code>do</code> can either be separated from the while by a
	 * block, or by a single statement, which limits our search distance.
	 *
	 * @return <code>true</code> if the <code>while</code> currently in
	 *         <code>fToken</code> has a matching <code>do</code>.
	 */
	private boolean hasMatchingDo() {
		Assert.isTrue(fToken == STPSymbols.TokenWHILE);
		nextToken();
		switch (fToken) {
		case STPSymbols.TokenRBRACE:
			skipScope(); // and fall thru
			skipToStatementStart(false, false);
			return fToken == STPSymbols.TokenDO;

		case STPSymbols.TokenSEMICOLON:
			skipToStatementStart(false, false);
			return fToken == STPSymbols.TokenDO;
		}
		return false;
	}

	/**
	 * Skips pointer operators if the current token is a pointer operator.
	 *
	 * @return <code>true</code> if a <code>*</code> or <code>&amp;</code> could be scanned, the
	 *         current token is left at the operator.
	 */
	private boolean skipPointerOperators() {
		if (fToken == STPSymbols.TokenOTHER) {
			CharSequence token= getTokenContent().toString().trim();
			if (token.length() == 1 && token.charAt(0) == '*' || token.charAt(0) == '&') {
				return true;
			}
		} else if (fToken == STPSymbols.TokenCONST) {
			return true;
		}
		return false;
	}

	/**
	 * Skips brackets if the current token is a RBRACKET. There can be nothing
	 * but whitespace in between, this is only to be used for <code>[]</code> elements.
	 *
	 * @return <code>true</code> if a <code>[]</code> could be scanned, the
	 *         current token is left at the LBRACKET.
	 */
	private boolean skipBrackets() {
		if (fToken == STPSymbols.TokenRBRACKET) {
			nextToken();
			if (fToken == STPSymbols.TokenLBRACKET) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Skips scope qualifiers of identifiers.
	 *
	 * @return <code>true</code> if a qualifier was encountered, the last token
	 *         will be an IDENT.
	 */
	private boolean skipQualifiers() {
		if (fToken == STPSymbols.TokenDOUBLECOLON) {
			nextToken();
			if (fToken == STPSymbols.TokenIDENT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reads the next token in backward direction from the heuristic scanner
	 * and sets the fields <code>fToken, fPreviousPosition</code> and <code>fPosition</code>
	 * accordingly.
	 */
	private void nextToken() {
		nextToken(fPosition);
	}

	/**
	 * Reads the next token in backward direction of <code>start</code> from
	 * the heuristic scanner and sets the fields <code>fToken, fPreviousPosition</code>
	 * and <code>fPosition</code> accordingly.
	 *
	 * @param start the start offset from which to scan backwards
	 */
	private void nextToken(int start) {
		fToken= fScanner.previousToken(start - 1, STPHeuristicScanner.UNBOUND);
		fPreviousPos= start;
		fPosition= fScanner.getPosition() + 1;
		try {
			fLine= fDocument.getLineOfOffset(fPosition);
		} catch (BadLocationException e) {
			fLine= -1;
		}
	}

	/**
	 * Reads the next token in backward direction from the heuristic scanner
	 * and returns that token without changing the current position.
	 */
	private int peekToken() {
		return fScanner.previousToken(fPosition - 1, STPHeuristicScanner.UNBOUND);
	}

	/**
	 * Returns <code>true</code> if the current tokens look like a method
	 * declaration header (i.e. only the return type and method name). The
	 * heuristic calls <code>nextToken</code> and expects an identifier
	 * (method name) and an optional return type declaration.
	 *
	 * @return <code>true</code> if the current position looks like a method
	 *         declaration header.
	 */
	private boolean looksLikeMethodDecl() {
		nextToken();
		switch (fToken) {
		case STPSymbols.TokenIDENT: // method name
			int pos= fPosition;
			nextToken();
			// check destructor tilde
			if (fToken == STPSymbols.TokenTILDE) {
				return true;
			}
			if (skipQualifiers()) {
				return true;
			}
			// optional brackets for array valued return types
			while (skipBrackets()) {
				nextToken();
			}
			while (skipPointerOperators()) {
				nextToken();
			}
			switch (fToken) {
			case STPSymbols.TokenIDENT:
				return true;
			case STPSymbols.TokenSEMICOLON:
			case STPSymbols.TokenRBRACE:
				fPosition= pos;
				return true;
			case STPSymbols.TokenLBRACE:
				if (fScanner.looksLikeCompositeTypeDefinitionBackward(fPosition, STPHeuristicScanner.UNBOUND)) {
					fPosition= pos;
					return true;
				}
				break;
			case STPSymbols.TokenCOMMA:
				nextToken();
				if (fToken == STPSymbols.TokenRPAREN) {
					// field initializer
					if (skipScope()) {
						return looksLikeMethodDecl();
					}
				}
				break;
			case STPSymbols.TokenCOLON:
				nextToken();
				switch (fToken) {
				case STPSymbols.TokenPUBLIC:
				case STPSymbols.TokenPROTECTED:
				case STPSymbols.TokenPRIVATE:
					fPosition= pos;
					return true;
				case STPSymbols.TokenRPAREN:
					// constructor initializer
					if (skipScope()) {
						pos = fPosition;
						nextToken();
						// optional throw
						if (fToken == STPSymbols.TokenTHROW) {
							nextToken();
							if (fToken != STPSymbols.TokenRPAREN || !skipScope()) {
								return false;
							}
						} else {
							fPosition = pos;
						}
						return looksLikeMethodDecl();
					}
					break;
				}
			}
			break;
		case STPSymbols.TokenARROW:
		case STPSymbols.TokenCOMMA:
		case STPSymbols.TokenEQUAL:
		case STPSymbols.TokenGREATERTHAN:
		case STPSymbols.TokenLESSTHAN:
		case STPSymbols.TokenMINUS:
		case STPSymbols.TokenPLUS:
		case STPSymbols.TokenSHIFTRIGHT:
		case STPSymbols.TokenSHIFTLEFT:
		case STPSymbols.TokenDELETE:
		case STPSymbols.TokenNEW:
			nextToken();
			return fToken == STPSymbols.TokenOPERATOR;
		case STPSymbols.TokenRPAREN:
			nextToken();
			if (fToken != STPSymbols.TokenLPAREN)
				return false;
			nextToken();
			return fToken == STPSymbols.TokenOPERATOR;
		case STPSymbols.TokenRBRACKET:
			nextToken();
			if (fToken != STPSymbols.TokenLBRACKET)
				return false;
			nextToken();
			if (fToken == STPSymbols.TokenNEW || fToken == STPSymbols.TokenDELETE)
				nextToken();
			return fToken == STPSymbols.TokenOPERATOR;
		case STPSymbols.TokenOTHER:
			if (getTokenContent().length() == 1) {
				nextToken();
				if (fToken == STPSymbols.TokenOPERATOR)
					return true;
			}
			if (getTokenContent().length() == 1) {
				nextToken();
				if (fToken == STPSymbols.TokenOPERATOR)
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the current tokens look like an anonymous type declaration
	 * header (i.e. a type name (potentially qualified) and a new keyword). The heuristic calls
	 * <code>nextToken</code> and expects a possibly qualified identifier (type name) and a new
	 * keyword
	 *
	 * @return <code>true</code> if the current position looks like a anonymous type declaration
	 *         header.
	 */
	private boolean looksLikeAnonymousTypeDecl() {
		nextToken();
		if (fToken == STPSymbols.TokenIDENT) { // type name
			nextToken();
			while (fToken == STPSymbols.TokenOTHER) { // dot of qualification
				nextToken();
				if (fToken != STPSymbols.TokenIDENT) // qualifying name
					return false;
				nextToken();
			}
			return fToken == STPSymbols.TokenNEW;
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the current tokens look like beginning of a method
	 * call (i.e. an identifier as opposed to a keyword taking parenthesized parameters
	 * such as <code>if</code>).
	 * <p>The heuristic calls <code>nextToken</code> and expects an identifier
	 * (method name).
	 *
	 * @return <code>true</code> if the current position looks like a method call
	 *         header.
	 */
	private boolean looksLikeMethodCall() {
		nextToken();
		if (fToken == STPSymbols.TokenGREATERTHAN) {
			if (!skipScope())
				return false;
			nextToken();
		}
		return fToken == STPSymbols.TokenIDENT; // method name
	}

	/**
	 * Scans tokens for the matching opening peer. The internal cursor
	 * (<code>fPosition</code>) is set to the offset of the opening peer if found.
	 *
	 * @param openToken the opening peer token
	 * @param closeToken the closing peer token
	 * @return <code>true</code> if a matching token was found, <code>false</code>
	 *         otherwise
	 */
	private boolean skipScope(int openToken, int closeToken) {
		int depth= 1;

		while (true) {
			nextToken();

			if (fToken == closeToken) {
				depth++;
			} else if (fToken == openToken) {
				depth--;
				if (depth == 0)
					return true;
			} else if (fToken == STPSymbols.TokenEOF) {
				return false;
			}
		}
	}
}
