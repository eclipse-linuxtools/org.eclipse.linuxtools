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

package org.eclipse.linuxtools.callgraph.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
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
public class InsertMarkerAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private static final String SYSTEMTAP_MARKER_INSERTED = 
		"//SYSTEMTAP marker function - this code should appear //STAPSTAPSTAP\n" +//$NON-NLS-1$
		"//once at the top of your program //STAPSTAPSTAP\n" //$NON-NLS-1$
			+ "void ___STAP_MARKER___(const char*); //STAPSTAPSTAP\n " //$NON-NLS-1$
			+ "void ___STAP_MARKER___(const char* i) { //STAPSTAPSTAP\n" //$NON-NLS-1$
			+ "return; //STAPSTAPSTAP\n" //$NON-NLS-1$
			+ "} //STAPSTAPSTAP\n"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public InsertMarkerAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		IWorkbenchPage page = window.getActivePage();
		IEditorPart part = page.getActiveEditor();
		if (!(part instanceof AbstractTextEditor))
			return;
		ITextEditor editor = (ITextEditor) part;
		IDocumentProvider dp = editor.getDocumentProvider();
		IDocument doc = dp.getDocument(editor.getEditorInput());

		StyledText st = (StyledText) editor.getAdapter(Control.class);

		try {
			int offset = st.getCaretOffset();
			doc.replace(offset, 0, "if (true) { //STAPSTAPSTAP\n" + //$NON-NLS-1$
				"\tchar* stapMarker = new char[20]; //STAPSTAPSTAP\n" + //$NON-NLS-1$
				"\tsprintf(stapMarker, \"\"); //STAPSTAPSTAP\n" + //$NON-NLS-1$
				// "\tprintf(\"%s\\n\", stapMarker); //STAPSTAPSTAP\n"
				// +
				"\t___STAP_MARKER___(stapMarker); //STAPSTAPSTAP\n\t} //STAPSTAPSTAP\n"); //$NON-NLS-1$
			st.setCaretOffset(offset + 68);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		// TODO: if this is too slow, fix
		if (!doc.get().contains(SYSTEMTAP_MARKER_INSERTED)) {
			try {
				int offset;
				offset = doc.getLineOffset(0);
				String output = SYSTEMTAP_MARKER_INSERTED;

				doc.replace(offset, 0, output);
			} catch (org.eclipse.jface.text.BadLocationException e) {
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
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}