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

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.systemtap.structures.FunctionNodeData;
import org.eclipse.linuxtools.systemtap.structures.TreeDefinitionNode;
import org.eclipse.linuxtools.systemtap.ui.editor.actions.file.OpenFileAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;



/**
 * This <code>Action</code> is used when the user right clicks on an item in the Tapset Browsers.
 * Right-clicking on items in the browsers causes a menu item called "Go to definition" to appear,
 * which fires this action upon selection. The result is that the file containing the definition
 * for the entity that the user selected is opened in an <code>STPEditor</code> in the current window.
 * @author Ryan Morse
 * @see org.eclipse.jface.action.Action
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.TapsetBrowserView
 */
public class DefinitionAction extends Action implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	private IStructuredSelection selection = null;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * The main body of the event. This code gets the filename from the selected entry in the viewer,
	 * then opens a new <code>STPEditor</code> for that file.
	 */
	@Override
	public void run(IAction action) {
		if(!isEnabled())
			return;
		Object o = selection.getFirstElement();
		if(!(o instanceof TreeDefinitionNode))
			return;
		TreeDefinitionNode t = (TreeDefinitionNode)o;
		String filename = t.getDefinition();
		File file = new File(filename);
		OpenFileAction open = new OpenFileAction();
		open.run(file);
		if (open.isSuccessful()) {
			IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			STPEditor editor = (STPEditor)editorPart;
			int line;

			if (!(t.getData() instanceof FunctionNodeData))
				line = probeFind(t, editor);
			else
				line = functionFind(t, editor);

			editor.jumpToLocation(++line, 0);
		}
	}

	/**
	 * Tries to find the line of code that corresponds to the provided
	 * function node within the file open in the provided editor.
	 * @param t The tree node that we want to look up
	 * @param editor The STPEditor with the file we are searching in
	 * @return int representing the line where the node is defined
	 */
	private int functionFind(TreeDefinitionNode t, STPEditor editor) {
		int line = editor.find(t.getData().toString());
		if(line < 0) {
			line = editor.findRegex("(?<!\\w)function " + t.toString()); //$NON-NLS-1$
			if(line < 0) {
				line = editor.find(t.toString());
			}
		}
		return Math.max(line, 0);
	}

	/**
	 * Tries to find the line of code that corresponds to the provided
	 * probe node within the file open in the provided editor.
	 * @param t The tree node that we want to look up
	 * @param editor The STPEditor with the file we are searching in
	 * @return int representing the line where the node is defined
	 */
	private int probeFind(TreeDefinitionNode t, STPEditor editor) {
		int line = editor.find("probe " + t.toString()); //$NON-NLS-1$

		if(line < 0)
			line = editor.find(t.getData().toString());
		if(line < 0)
			line = editor.find(t.getData().toString().replace(" ", "")); //$NON-NLS-1$ //$NON-NLS-2$
		return Math.max(line, 0);
	}

	/**
	 * Updates <code>selection</code> with the current selection whenever the user changes
	 * the current selection.
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection)selection;
			Object o = this.selection.getFirstElement();
			if(o instanceof TreeDefinitionNode)
			{
				TreeDefinitionNode t = (TreeDefinitionNode)o;
				String s = t.getDefinition();
				if(s != null)
					setEnabled(true);
				else
					setEnabled(false);
			}
			else
				setEnabled(false);
		} else {
			this.setEnabled(false);
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {}

	@Override
	public void dispose() {}

}
