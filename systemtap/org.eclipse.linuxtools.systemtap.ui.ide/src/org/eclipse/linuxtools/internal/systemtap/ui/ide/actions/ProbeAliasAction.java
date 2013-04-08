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
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.editor.actions.file.NewFileAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;



/**
 * This <code>Action</code> is fired when the user selects an item in the <code>ProbeAliasBrowserView</code>.
 * The action taken is to insert a template probe in the current <code>STPEditor</code>, if available, or to
 * insert the probe into a new <code>STPEditor</code> if one does not exist.
 * @author Henry Hughes
 * @author Ryan Morse
 * @see org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor#insertText(String)
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView
 * @see org.eclipse.jface.action.Action
 */
public class ProbeAliasAction extends Action implements ISelectionListener, IDoubleClickListener {
	private final IWorkbenchWindow window;
	private final ProbeAliasBrowserView viewer;
	private static final String ID = "org.eclipse.linuxtools.systemtap.ui.ide.ProbeAliasAction"; //$NON-NLS-1$
	private IStructuredSelection selection;

	/**
	 * The Default Constructor. Takes the <code>IWorkbenchWindow</code> that it effects
	 * as well as the <code>ProbeAliasBrowserView</code> that will fire this action.
	 * @param window	window effected by this event
	 * @param view	browser that fires this action
	 */
	public ProbeAliasAction(IWorkbenchWindow window, ProbeAliasBrowserView view) {
		this.window = window;
		setId(ID);
		setActionDefinitionId(ID);
		setText(Localization.getString("ProbeAliasAction.Insert")); //$NON-NLS-1$
		setToolTipText(Localization
				.getString("ProbeAliasAction.InsertSelectedProbe")); //$NON-NLS-1$
		window.getSelectionService().addSelectionListener(this);
		viewer = view;
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

	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	/**
	 * The main body of the action. This method checks for the current editor, creating one
	 * if there is no active <code>STPEditor</code>, and then inserts a template probe for the
	 * item that the user clicked on.
	 */
	@Override
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		if(null == editor) {
			NewFileAction action = new NewFileAction();
			action.run();
			editor = page.getWorkbenchWindow().getActivePage().getActiveEditor();
		}
		ISelection incoming = viewer.getViewer().getSelection();
		IStructuredSelection selection = (IStructuredSelection)incoming;
		Object o = selection.getFirstElement();
		if (o instanceof TreeNode) {
			TreeNode t = (TreeNode) o;
			if(editor instanceof STPEditor) {
				STPEditor stpeditor = (STPEditor)editor;
				//build the string
				StringBuilder s = new StringBuilder("\nprobe " + t.toString()); //$NON-NLS-1$
				if(!t.isClickable())
					if(0 <t.getChildCount())
						s.append(".*"); //$NON-NLS-1$
					else
						return;
				s.append("\n{\n"); //$NON-NLS-1$
				if(t.isClickable() && t.getChildCount() > 0) {
					s.append("\t/*\n\t * " + //$NON-NLS-1$
							Localization
									.getString("ProbeAliasAction.AvailableVariables") + //$NON-NLS-1$
							"\n\t * "); //$NON-NLS-1$
					boolean first = true;
					for(int i = 0; i < t.getChildCount(); i++) {
						if(first) first = false;
						else
							s.append(", "); //$NON-NLS-1$
						s.append(t.getChildAt(i).toString());
					}
					s.append("\n\t */\n"); //$NON-NLS-1$
				}
				s.append("\n}\n"); //$NON-NLS-1$
				stpeditor.insertText(s.toString());
			}
		}
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		run();
	}
}
