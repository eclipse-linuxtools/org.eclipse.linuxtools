/*******************************************************************************
 * Copyright (c) 2000, 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *     Red Hat Inc. - modified for use in SystemTap
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.IndentUtil;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPDefaultCodeFormatterConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPHeuristicScanner;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPIndenter;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPPartitionScanner;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Indents a line or range of lines in a C document to its correct position. No
 * complete AST must be present, the indentation is computed using heuristics.
 * The algorithm used is fast for single lines, but does not store any
 * information and therefore not so efficient for large line ranges.
 * 
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPHeuristicScanner
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPIndenter
 */
public class IndentHandler extends AbstractHandler {
	/** The caret offset after an indent operation. */
	private int fCaretOffset;

	private class STPRunnable implements Runnable {
		private ITextEditor editor;

		public STPRunnable(ITextEditor editor) {
			this.editor = editor;
		}

		public ITextEditor getTextEditor() {
			return editor;
		}

		@Override
		public void run() {
		}
	}

	/**
	 * Whether this is the action invoked by TAB. When <code>true</code>,
	 * indentation behaves differently to accommodate normal TAB operation.
	 */
	private final boolean fIsTabAction = false;

	@Override
	public Object execute(ExecutionEvent event) { // Update has been called by
													// the framework
		if (!isEnabled())
			return null;

		ITextEditor editor = (ITextEditor) HandlerUtil.getActiveEditor(event);
		if (editor == null || !editor.isEditable()) {
			return null;
		}

		ITextSelection selection = getSelection(editor);
		final IDocument document = getDocument(editor);

		if (document != null) {
			final int offset = selection.getOffset();
			final int length = selection.getLength();
			final Position end = new Position(offset + length);
			final int firstLine, nLines;
			fCaretOffset = -1;

			try {
				firstLine = document.getLineOfOffset(offset);
				// check for marginal (zero-length) lines
				int minusOne = length == 0 ? 0 : 1;
				nLines = document.getLineOfOffset(offset + length - minusOne)
						- firstLine + 1;
				document.addPosition(end);
			} catch (BadLocationException e) {
				// will only happen on concurrent modification
				IDEPlugin.log(new Status(IStatus.ERROR,
						IDEPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
				return null;
			}

			Runnable runnable = new STPRunnable(editor) {
				@Override
				public void run() {
					IRewriteTarget target = (IRewriteTarget) getTextEditor()
							.getAdapter(IRewriteTarget.class);
					if (target != null) {
						target.beginCompoundChange();
					}

					try {
						STPHeuristicScanner scanner = new STPHeuristicScanner(
								document);
						STPIndenter indenter = new STPIndenter(document,
								scanner, getProject(getTextEditor()));
						final boolean multiLine = nLines > 1;
						boolean hasChanged = false;
						for (int i = 0; i < nLines; i++) {
                            hasChanged |= indentLine(document, firstLine + i,
                                    offset, indenter, scanner, multiLine);
						}

						// update caret position: move to new position when
						// indenting just one line
						// keep selection when indenting multiple
						int newOffset, newLength;
						if (!fIsTabAction && multiLine) {
							newOffset = offset;
							newLength = end.getOffset() - offset;
						} else {
							newOffset = fCaretOffset;
							newLength = 0;
						}

						// always reset the selection if anything was replaced
						// but not when we had a single line non-tab invocation
						if (newOffset != -1
								&& (hasChanged || newOffset != offset || newLength != length))
							selectAndReveal(getTextEditor(), newOffset,
									newLength);

					} catch (BadLocationException e) {
						// will only happen on concurrent modification
						IDEPlugin.log(new Status(IStatus.ERROR, IDEPlugin
								.PLUGIN_ID, IStatus.OK,
								"ConcurrentModification in IndentAction", e)); //$NON-NLS-1$
					} finally {
						document.removePosition(end);
						if (target != null) {
							target.endCompoundChange();
						}
					}
				}
			};

			if (nLines > 50) {
				Display display = editor.getEditorSite().getWorkbenchWindow()
						.getShell().getDisplay();
				BusyIndicator.showWhile(display, runnable);
			} else {
				runnable.run();
			}
		}

		return null;
	}

	/**
	 * Selects the given range on the editor.
	 * 
	 * @param newOffset
	 *            the selection offset
	 * @param newLength
	 *            the selection range
	 */
	private void selectAndReveal(ITextEditor editor, int newOffset,
			int newLength) {
		Assert.isTrue(newOffset >= 0);
		Assert.isTrue(newLength >= 0);
		if (editor instanceof STPEditor) {
			ISourceViewer viewer = ((STPEditor) editor).getMySourceViewer();
			if (viewer != null) {
				viewer.setSelectedRange(newOffset, newLength);
			}
		} else {
			// this is too intrusive, but will never get called anyway
			editor.selectAndReveal(newOffset, newLength);
		}
	}

	/**
	 * Indents a single line using the heuristic scanner. Multiline comments are
	 * indented as specified by the <code>CCommentAutoIndentStrategy</code>.
	 * 
	 * @param document
	 *            the document
	 * @param line
	 *            the line to be indented
	 * @param caret
	 *            the caret position
	 * @param indenter
	 *            the indenter
	 * @param scanner
	 *            the heuristic scanner
	 * @param multiLine
	 *            <code>true</code> if more than one line is being indented
	 * @return <code>true</code> if <code>document</code> was modified,
	 *         <code>false</code> otherwise
	 * @throws BadLocationException
	 *             if the document got changed concurrently
	 */
	private boolean indentLine(IDocument document,
			int line, int caret, STPIndenter indenter,
			STPHeuristicScanner scanner, boolean multiLine)
			throws BadLocationException {
		IRegion currentLine = document.getLineInformation(line);
		int offset = currentLine.getOffset();
		int wsStart = offset; // where we start searching for non-WS; after the
								// "//" in single line comments

		String indent = null;
		if (offset < document.getLength()) {
			ITypedRegion partition = TextUtilities.getPartition(document,
					STPPartitionScanner.STP_PARTITIONING, offset, true);
			ITypedRegion startingPartition = TextUtilities.getPartition(
					document, STPPartitionScanner.STP_PARTITIONING, offset,
					false);
			String type = partition.getType();
			if (type.equals(STPPartitionScanner.STP_MULTILINE_COMMENT)) {
				indent = computeCommentIndent(document, line, scanner,
						startingPartition);
			} else if (startingPartition.getType().equals(
					STPPartitionScanner.STP_CONDITIONAL)) {
				indent = computePreprocessorIndent(document, line,
						startingPartition);
			} else if (startingPartition.getType().equals(
					STPPartitionScanner.STP_STRING)
					&& offset > startingPartition.getOffset()) {
				// don't indent inside (raw-)string
				return false;
			} else if (!fIsTabAction
					&& startingPartition.getOffset() == offset
					&& startingPartition.getType().equals(
							STPPartitionScanner.STP_COMMENT)) {
				// line comment starting at position 0 -> indent inside
				if (indentInsideLineComments()) {
					int max = document.getLength() - offset;
					int slashes = 2;
					while (slashes < max - 1
							&& document.get(offset + slashes, 2).equals("//")) //$NON-NLS-1$
						slashes += 2;

					wsStart = offset + slashes;

					StringBuilder computed = indenter
							.computeIndentation(offset);
					if (computed == null)
						computed = new StringBuilder(0);
					int tabSize = getTabSize();
					while (slashes > 0 && computed.length() > 0) {
						char c = computed.charAt(0);
						if (c == '\t') {
							if (slashes > tabSize) {
								slashes -= tabSize;
							} else {
								break;
							}
						} else if (c == ' ') {
							slashes--;
						} else {
							break;
						}

						computed.deleteCharAt(0);
					}

					indent = document.get(offset, wsStart - offset) + computed;
				}
			}
		}

		// standard C code indentation
		if (indent == null) {
			StringBuilder computed = indenter.computeIndentation(offset);
			if (computed != null) {
				indent = computed.toString();
			} else {
				indent = ""; //$NON-NLS-1$
			}
		}

		// change document:
		// get current white space
		int lineLength = currentLine.getLength();
		int end = scanner.findNonWhitespaceForwardInAnyPartition(wsStart,
				offset + lineLength);
		if (end == STPHeuristicScanner.NOT_FOUND) {
			// an empty line
			end = offset + lineLength;
			if (multiLine && !indentEmptyLines()) {
				indent = ""; //$NON-NLS-1$
			}
		}
		int length = end - offset;
		String currentIndent = document.get(offset, length);

		// set the caret offset so it can be used when setting the selection
		if (caret >= offset && caret <= end) {
			fCaretOffset = offset + indent.length();
		} else {
			fCaretOffset = -1;
		}

		// only change the document if it is a real change
		if (!indent.equals(currentIndent)) {
			document.replace(offset, length, indent);
			return true;
		}
		return false;
	}

	/**
	 * Computes and returns the indentation for a block comment line.
	 * 
	 * @param document
	 *            the document
	 * @param line
	 *            the line in document
	 * @param scanner
	 *            the scanner
	 * @param partition
	 *            the comment partition
	 * @return the indent, or <code>null</code> if not computable
	 * @throws BadLocationException
	 */
	private String computeCommentIndent(IDocument document, int line,
			STPHeuristicScanner scanner, ITypedRegion partition)
			throws BadLocationException {
		return IndentUtil.computeCommentIndent(document, line, scanner,
				partition);
	}

	/**
	 * Computes and returns the indentation for a preprocessor line.
	 * 
	 * @param document
	 *            the document
	 * @param line
	 *            the line in document
	 * @param partition
	 *            the comment partition
	 * @return the indent, or <code>null</code> if not computable
	 * @throws BadLocationException
	 */
	private String computePreprocessorIndent(IDocument document, int line,
			ITypedRegion partition) throws BadLocationException {
		return IndentUtil.computePreprocessorIndent(document, line, partition);
	}

	/**
	 * Returns the tab size used by the editor, which is deduced from the
	 * formatter preferences.
	 * 
	 * @return the tab size as defined in the current formatter preferences
	 */
	private int getTabSize() {
		return getCoreFormatterOption(
				STPDefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, 4);
	}

	/**
	 * Returns <code>true</code> if empty lines should be indented,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if empty lines should be indented,
	 *         <code>false</code> otherwise
	 */
	private boolean indentEmptyLines() {
		return STPDefaultCodeFormatterConstants.TRUE
				.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES));
	}

	/**
	 * Returns <code>true</code> if line comments at column 0 should be indented
	 * inside, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if line comments at column 0 should be indented
	 *         inside, <code>false</code> otherwise.
	 */
	private boolean indentInsideLineComments() {
		return STPDefaultCodeFormatterConstants.TRUE
				.equals(getCoreFormatterOption(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_INSIDE_LINE_COMMENTS));
	}

	/**
	 * Returns the possibly project-specific core preference defined under
	 * <code>key</code>.
	 * 
	 * @param key
	 *            the key of the preference
	 * @return the value of the preference
	 */
	private String getCoreFormatterOption(String key) {
		return "false"; //$NON-NLS-1$
	}

	/**
	 * Returns the possibly project-specific core preference defined under
	 * <code>key</code>, or <code>def</code> if the value is not a integer.
	 * 
	 * @param key
	 *            the key of the preference
	 * @param def
	 *            the default value
	 * @return the value of the preference
	 */
	private int getCoreFormatterOption(String key, int def) {
		try {
			return Integer.parseInt(getCoreFormatterOption(key));
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Returns the <code>IProject</code> of the current editor input, or
	 * <code>null</code> if it cannot be found.
	 * 
	 * @return the <code>IProject</code> of the current editor input, or
	 *         <code>null</code> if it cannot be found
	 */
	private IProject getProject(ITextEditor editor) {
		if (editor == null)
			return null;

		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput)
			return ((IFileEditorInput) input).getFile().getProject();
		return null;
	}

	/**
	 * Returns the editor's selection provider.
	 * 
	 * @return the editor's selection provider or <code>null</code>
	 */
	private ISelectionProvider getSelectionProvider(ITextEditor editor) {
		if (editor != null) {
			return editor.getSelectionProvider();
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */

	/**
	 * Returns the document currently displayed in the editor, or
	 * <code>null</code> if none can be obtained.
	 * 
	 * @return the current document or <code>null</code>
	 */
	private IDocument getDocument(ITextEditor editor) {
		if (editor != null) {
			IDocumentProvider provider = editor.getDocumentProvider();
			IEditorInput input = editor.getEditorInput();
			if (provider != null && input != null)
				return provider.getDocument(input);

		}
		return null;
	}

	/**
	 * Returns the selection on the editor or an invalid selection if none can
	 * be obtained. Returns never <code>null</code>.
	 * 
	 * @return the current selection, never <code>null</code>
	 */
	private ITextSelection getSelection(ITextEditor editor) {
		ISelectionProvider provider = getSelectionProvider(editor);
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof ITextSelection)
				return (ITextSelection) selection;
		}

		// null object
		return TextSelection.emptySelection();
	}
}
