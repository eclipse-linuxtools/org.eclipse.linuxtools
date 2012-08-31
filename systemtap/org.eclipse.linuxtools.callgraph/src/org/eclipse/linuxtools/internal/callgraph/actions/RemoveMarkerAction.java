/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.callgraph.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class RemoveMarkerAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private String stapCommentMarker = "//STAPSTAPSTAP"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public RemoveMarkerAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	@Override
	public void run(IAction action) {
		IWorkbenchPage page = window.getActivePage();
		IEditorPart part = page.getActiveEditor();
		if (!(part instanceof AbstractTextEditor))
			return;
		ITextEditor editor = (ITextEditor) part;
		IDocumentProvider dp = editor.getDocumentProvider();
		IDocument doc = dp.getDocument(editor.getEditorInput());

		for (int i = 0; i < doc.getNumberOfLines(); i++) {
			try {
				int start = doc.getLineOffset(i);
				int length = doc.getLineLength(i);
				if (doc.get(start, length).contains(stapCommentMarker)) {
					doc.replace(start, length, ""); //$NON-NLS-1$
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
		//TODO: Figure out why I need to do this 3 times to remove all STAPSTAPSTAP...
		for (int i = 0; i < doc.getNumberOfLines(); i++) {
			try {
				int start = doc.getLineOffset(i);
				int length = doc.getLineLength(i);
				if (doc.get(start, length).contains(stapCommentMarker)) {
					doc.replace(start, length, ""); //$NON-NLS-1$
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
		for (int i = 0; i < doc.getNumberOfLines(); i++) {
			try {
				int start = doc.getLineOffset(i);
				int length = doc.getLineLength(i);
				if (doc.get(start, length).contains(stapCommentMarker)) {
					doc.replace(start, length, ""); //$NON-NLS-1$
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	@Override
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}