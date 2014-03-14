/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.editor.actions.file.NewFileAction;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;



/**
 * This <code>Action</code> is fired when the user double clicks on an entry in the
 * IDE's current <code>FunctionBrowserView</code>. The behavior of this <code>Action</code> is
 * to expand or collapse the function tree if the user clicks on a non-function (say a file containing
 * functions), or to insert a blank call to the function if the user double clicks on a function
 * (defined by the clickable property in the <code>TreeNode</code> class, retrieved through
 * <code>TreeNode.isClickable</code>.
 * @author Henry Hughes
 * @author Ryan Morse
 * @see org.eclipse.linuxtools.systemtap.structures.TreeNode#isClickable()
 * @see org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor#insertTextAtCurrent(String)
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.TreeExpandCollapseAction
 */
public class FunctionBrowserAction extends BrowserViewAction {
	private static final String ID = "org.eclipse.linuxtools.systemtap.ui.ide.FunctionAction"; //$NON-NLS-1$

	public FunctionBrowserAction(IWorkbenchWindow window, FunctionBrowserView browser) {
		super(window, browser);
		setId(ID);
		setActionDefinitionId(ID);
		setText(Localization.getString("FunctionBrowserAction.Insert")); //$NON-NLS-1$
		setToolTipText(Localization
				.getString("FunctionBrowserAction.InsertFunction")); //$NON-NLS-1$
	}

	/**
	 * The main action code, invoked when this action is fired. This code checks the current
	 * selection's clickable property, and either invokes the <code>TreeExpandCollapseAction</code> if
	 * the selection is not clickable (i.e. the selection is not a function, but a category of functions),
	 * or it inserts text for a function call to the selected function in the active STPEditor
	 * (creating a new editor if there is not one currently open).
	 */
	@Override
	public void run() {
		IWorkbenchPage page = getWindow().getActivePage();
		Object o = getSelectedElement();
		if (o instanceof TreeNode) {
			TreeNode t = (TreeNode) o;
			if(t.isClickable()) {
				IEditorInput input;
				IEditorPart ed = page.getActiveEditor();
				if(ed == null) {
					NewFileAction action = new NewFileAction();
					action.run();
					if (action.isSuccessful())
						ed = page.getWorkbenchWindow().getActivePage().getActiveEditor();
					else
						return;
				}
				input = ed.getEditorInput();
				IEditorPart editor;
				try {
					editor = page.openEditor(input, STPEditor.ID);

					if(editor instanceof STPEditor) {
						STPEditor stpeditor = (STPEditor)editor;
						//build the string
						String s = t.toString() + "\n"; //$NON-NLS-1$
						stpeditor.insertTextAtCurrent(s);

					}
				} catch (PartInitException e) {
					ExceptionErrorDialog.openError(Localization.getString("FunctionBrowserAction.UnableToInsertFunction"), e); //$NON-NLS-1$
				}
			} else {
				runExpandAction();
			}
		}
	}

}
