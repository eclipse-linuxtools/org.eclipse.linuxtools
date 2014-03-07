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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.editor.actions.file.NewFileAction;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
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
public class FunctionBrowserAction extends Action implements ISelectionListener, IDoubleClickListener {
	private final IWorkbenchWindow window;
	private final FunctionBrowserView viewer;
	private static final String ID = "org.eclipse.linuxtools.systemtap.ui.ide.FunctionAction"; //$NON-NLS-1$
	private IStructuredSelection selection;
	private TreeExpandCollapseAction expandAction;

	/**
	 * The Default Constructor. Takes the <code>IWorkbenchWindow</code> that it effects
	 * as well as the <code>FunctionBrowserView</code> that will fire this action.
	 * @param window	window effected by this event
	 * @param browser	browser that fires this action
	 */
	public FunctionBrowserAction(IWorkbenchWindow window, FunctionBrowserView browser) {
		this.window = window;
		setId(ID);
		setActionDefinitionId(ID);
		setText(Localization.getString("FunctionBrowserAction.Insert")); //$NON-NLS-1$
		setToolTipText(Localization
				.getString("FunctionBrowserAction.InsertFunction")); //$NON-NLS-1$
		window.getSelectionService().addSelectionListener(this);
		viewer = browser;
		expandAction = new TreeExpandCollapseAction(viewer);
	}

	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
		selection = null;
		expandAction.dispose();
		expandAction = null;
	}

	/**
	 * Updates <code>selection</code> with the current selection whenever the user changes
	 * the current selection.
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		if (incoming instanceof IStructuredSelection) {
			selection = (IStructuredSelection) incoming;
			setEnabled(selection.size() == 1);
		} else {
			// Other selections, for example containing text or of other kinds.
			setEnabled(false);
		}
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
		IWorkbenchPage page = window.getActivePage();
		ISelection incoming = viewer.getViewer().getSelection();
		IStructuredSelection selection = (IStructuredSelection)incoming;
		Object o = selection.getFirstElement();
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
				expandAction.run();
			}
		}
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		run();
	}
}
