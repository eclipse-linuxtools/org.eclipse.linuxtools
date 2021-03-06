/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.outline;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class SpecfileContentOutlinePage extends ContentOutlinePage {

	private ITextEditor editor;
	private IEditorInput input;

	public SpecfileContentOutlinePage(ITextEditor editor) {
		this.editor = editor;
	}

	public void setInput(IEditorInput editorInput) {
		this.input = editorInput;
		update();
	}

	public void update() {
		// set the input so that the outlines parse can be called
		// update the tree viewer state
		final TreeViewer viewer = getTreeViewer();

		if (viewer != null) {
			final Control control = viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.getDisplay().asyncExec(() -> {
					if (!control.isDisposed()) {
						control.setRedraw(false);
						if (input != null) {
							viewer.setInput(input);
						}
						viewer.expandAll();
						control.setRedraw(true);
					}
				});
			}
		}
	}

	@Override
	public void createControl(Composite parent) {

		super.createControl(parent);

		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new SpecfileContentProvider(editor));
		SpecfileLabelProvider labelProvider = new SpecfileLabelProvider();
		viewer.setLabelProvider(labelProvider);
		viewer.addSelectionChangedListener(this);

		if (input != null) {
			viewer.setInput(input);
		}
	}

	/*
	 * Change in selection
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);

		// find out which item in tree viewer we have selected, and set
		// highlight range accordingly
		IStructuredSelection selection = event.getStructuredSelection();
		if (selection.isEmpty()) {
			editor.resetHighlightRange();
		} else {
			SpecfileElement element = (SpecfileElement) selection.getFirstElement();

			int start = element.getLineStartPosition();
			try {
				editor.setHighlightRange(start, 1, true);
			} catch (IllegalArgumentException e) {
				editor.resetHighlightRange();
			}
		}
	}

}
