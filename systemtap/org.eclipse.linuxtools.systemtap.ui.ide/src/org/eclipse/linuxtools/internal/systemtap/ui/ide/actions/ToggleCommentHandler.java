/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPPartitionScanner;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Handler for command in charge of toggling comment prefixes. Based on
 * org.eclipse.cdt.internal.ui.actions.ToggleCommentAction.
 */
public class ToggleCommentHandler extends AbstractHandler {

	/** The text operation target */
	private ITextOperationTarget operationTarget;

	/**
	 * Checks if the selected lines are all commented or not and
	 * uncomments/comments them respectively.
	 */
	@Override
	public Object execute(ExecutionEvent event) {
		ITextEditor editor = (ITextEditor) HandlerUtil.getActiveEditor(event);
		if (editor == null || !editor.isEditable()) {
			return null;
		}

		updateOpTarget(editor);
		if (operationTarget == null) {
			return null;
		}

		ISelection selection = editor.getSelectionProvider().getSelection();
		IDocument document = editor.getDocumentProvider().getDocument(
				editor.getEditorInput());

		final int operationCode;
		if (isSelectionCommented(selection, document)) {
			operationCode = ITextOperationTarget.STRIP_PREFIX;
		} else {
			operationCode = ITextOperationTarget.PREFIX;
		}

		Shell shell = editor.getSite().getShell();
		if (!operationTarget.canDoOperation(operationCode)) {
			if (shell != null) {
				MessageDialog.openError(shell,
						Localization.getString("ToggleComment_error_title"), //$NON-NLS-1$
						Localization.getString("ToggleComment_error_message")); //$NON-NLS-1$
			}
			return null;
		}

		Display display = null;
		if (shell != null && !shell.isDisposed()) {
			display = shell.getDisplay();
		}

		BusyIndicator.showWhile(display, new Runnable() {
			@Override
			public void run() {
				operationTarget.doOperation(operationCode);
			}
		});

		return null;
	}

	/**
	 * Creates a region describing the text block (something that starts at the
	 * beginning of a line) completely containing the current selection.
	 *
	 * Note, the implementation has to match org.eclipse.jface.text.TextViewer;
	 * .getTextBlockFromSelection().
	 *
	 * @param selection The selection to use
	 * @param document The document
	 * @return the region describing the text block comprising the given
	 *         selection
	 * @throws BadLocationException
	 */
	public IRegion getTextBlockFromSelection(ITextSelection selection,
			IDocument document) throws BadLocationException {
		int start = document.getLineOffset(selection.getStartLine());
		int end;
		int endLine = selection.getEndLine();
		if (document.getNumberOfLines() > endLine + 1) {
			end = document.getLineOffset(endLine + 1);
		} else {
			end = document.getLength();
		}
		return new Region(start, end - start);
	}

	/**
	 * Is the given selection on the specified document single-line commented?
	 *
	 * @param selection Selection to check
	 * @param document The document
	 * @return <code>true</code> iff all selected lines are commented
	 */
	public boolean isSelectionCommented(ISelection selection,
			IDocument document) {

		if (!(selection instanceof ITextSelection)) {
			return false;
		}

		ITextSelection textSelection = (ITextSelection) selection;
		if (textSelection.getStartLine() < 0 || textSelection.getEndLine() < 0) {
			return false;
		}

		try {
			IRegion block = getTextBlockFromSelection(textSelection, document);
			ITypedRegion[] regions = TextUtilities.computePartitioning(
					document, STPPartitionScanner.STP_PARTITIONING,
					block.getOffset(), block.getLength(), false);

			int[] lines = new int[regions.length * 2]; // [startline, endline,
														// startline, endline,
														// ...]

			// For each partition in the text selection, figure out the
			// startline and endline.
			// Count the number of lines that are selected.
			for (int i = 0, j = 0; i < regions.length; i++, j += 2) {
				// Start line of region
				lines[j] = getFirstCompleteLineOfRegion(regions[i], document);
				// End line of region
				int length = regions[i].getLength();
				int offset = regions[i].getOffset() + length;
				if (length > 0) {
					offset--;
				}

				// If there is no startline for this region (startline = -1),
				// then there is no endline,
				// otherwise, get the line number of the endline and store it in
				// the array.
				lines[j + 1] = (lines[j] == -1 ? -1 : document
						.getLineOfOffset(offset));

				assert i < regions.length;
				assert j < regions.length * 2;
			}

			// Perform the check
			boolean hasComment = false;
			for (int i = 0, j = 0; i < regions.length; i++, j += 2) {
				String prefix = "//"; //$NON-NLS-1$
				if (lines[j] >= 0 && lines[j + 1] >= 0) {
					if (isBlockCommented(lines[j], lines[j + 1], prefix,
							document)) {
						hasComment = true;
					} else if (!isBlockEmpty(lines[j], lines[j + 1], document)) {
						return false;
					}
				}
			}
			return hasComment;
		} catch (BadLocationException e) {
			ExceptionErrorDialog.openError(e.getLocalizedMessage(), e);
		}

		return false;
	}

	/**
	 * Returns the index of the first line whose start offset is in the given
	 * text range.
	 *
	 * @param region the text range in characters where to find the line
	 * @param document The document
	 * @return the first line whose start index is in the given range, -1 if
	 *         there is no such line
	 */
	public int getFirstCompleteLineOfRegion(IRegion region, IDocument document) {
		try {
			int startLine = document.getLineOfOffset(region.getOffset());

			int offset = document.getLineOffset(startLine);
			if (offset >= region.getOffset()) {
				return startLine;
			}

			offset = document.getLineOffset(startLine + 1);
			return (offset > region.getOffset() + region.getLength() ? -1
				: startLine + 1);
		} catch (BadLocationException e) {
			ExceptionErrorDialog.openError(e.getLocalizedMessage(), e);
		}

		return -1;
	}

	/**
	 * Determines whether each line is empty
	 *
	 * @param startLine Start line in document
	 * @param endLine End line in document
	 * @param document The document
	 * @return <code>true</code> if each line from <code>startLine</code> to and
	 *         including <code>endLine</code> is empty
	 */
	public boolean isBlockEmpty(int startLine, int endLine, IDocument document) {
		try {
			for (int i = startLine; i <= endLine; i++) {
				IRegion line = document.getLineInformation(i);
				String text = document.get(line.getOffset(), line.getLength());

				boolean isEmptyLine = text.trim().length() == 0;
				if (!isEmptyLine) {
					return false;
				}
			}
			return true;
		} catch (BadLocationException e) {
			ExceptionErrorDialog.openError(e.getLocalizedMessage(), e);
		}

		return false;
	}

	/**
	 * Determines whether each line is prefixed by one of the prefixes.
	 *
	 * @param startLine Start line in document
	 * @param endLine End line in document
	 * @param prefix Comment prefix
	 * @param document The document
	 * @return <code>true</code> iff each line from <code>startLine</code> to
	 *         and including <code>endLine</code> is prepended by the
	 *         <code>prefix</code>, ignoring whitespace at the begin of line
	 */
	public boolean isBlockCommented(int startLine, int endLine, String prefix,
			IDocument document) {
		try {
			// Check for occurrences of prefixes in the given lines
			boolean hasComment = false;
			for (int i = startLine; i <= endLine; i++) {
				IRegion line = document.getLineInformation(i);
				String text = document.get(line.getOffset(), line.getLength());

				boolean isEmptyLine = text.trim().length() == 0;
				if (isEmptyLine) {
					continue;
				}

				int prefixIndex = text.indexOf(prefix, 0);

				if (prefixIndex == -1) {
					// Found a line which is not commented
					return false;
				}
				String s = document.get(line.getOffset(), prefixIndex);
				s = s.trim();
				if (s.length() != 0) {
					// Found a line which is not commented
					return false;
				}
				hasComment = true;
			}
			return hasComment;
		} catch (BadLocationException e) {
			ExceptionErrorDialog.openError(e.getLocalizedMessage(), e);
		}

		return false;
	}

	/**
	 * Update text operation target based on the specified text editor.
	 *
	 * @param editor ITextEditor editor to associate operation target to.
	 */
	private void updateOpTarget(ITextEditor editor) {
		if (editor != null) {
			operationTarget = (ITextOperationTarget) editor
					.getAdapter(ITextOperationTarget.class);
		}
	}
}
