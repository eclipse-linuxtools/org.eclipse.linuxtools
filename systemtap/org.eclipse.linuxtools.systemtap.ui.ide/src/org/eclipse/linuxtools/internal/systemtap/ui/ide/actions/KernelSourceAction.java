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

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.KernelBrowserView;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;

/**
 * This <code>Action</code> is raised by <code>KernelBrowserView</code> whenever the user selects
 * an item in the view (usually by double clicking). This <code>Action</code> either passes
 * the event on to <code>TreeExpandCollapseAction</code> if the selection is not clickable, or
 * it opens a new CEditor for the file selected if the selection is clickable.
 * @author Henry Hughes
 * @author Ryan Morse
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c.CEditor
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.TreeExpandCollapseAction
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.KernelBrowserView
 */
public class KernelSourceAction extends BrowserViewAction {
    public final static String ID = "org.eclipse.linuxtools.systemtap.ui.ide.KBAction"; //$NON-NLS-1$
    private static final String CDT_EDITOR_ID = "org.eclipse.cdt.ui.editor.CEditor"; //$NON-NLS-1$

    /**
     * The default constructor for the <code>KernelSourceAction</code>. Takes the window that it affects
     * and the <code>KernelBrowserView</code> that will fire the event as arguments.
     * @param window    The <code>IWorkbenchWindow</code> that the action operates on.
     * @param browser    The <code>KernelBrowserView</code> that fires this action.
     */
    public KernelSourceAction(IWorkbenchWindow window, KernelBrowserView browser) {
        super(window, browser);
        setId(ID);
        setActionDefinitionId(ID);
        setText(Localization.getString("KernelSourceAction.Insert")); //$NON-NLS-1$
        setToolTipText(Localization
                .getString("KernelSourceAction.InsertSelectedFunction")); //$NON-NLS-1$
    }

    /**
     * The main code body for this action. Causes one of the following to occur:
     * <ul>
     *     <li>If the selected node is clickable, as specified in <code>TreeNode.isClickable</code>
     *         the browser creates an instance of <code>CEditor</code> on the file specified in the selection
     *         (<code>KernelBrowserView</code>'s tree only marks clickable on files, not folders) and
     *         opens it on the current window</li>
     *     <li>If the selected node is not clickable, the code runs the action specified in
     *         <code>TreeExpandCollapseAction</code></li>
     * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c.CEditor
     * @see TreeNode#isClickable()
     * @see TreeExpandCollapseAction
     */
    @Override
    public void run() {
        IWorkbench wb = PlatformUI.getWorkbench();
        Object o = getSelectedElement();
        if (o instanceof TreeNode) {
            TreeNode t = (TreeNode) o;
            if (t.isClickable()) {
                IFileStore fs = (IFileStore) t.getData();
                if (fs != null) {
                    IEditorInput input = new FileStoreEditorInput(fs);
                    try {
                        wb.getActiveWorkbenchWindow().getActivePage().openEditor(input, CDT_EDITOR_ID);
                    } catch (PartInitException e) {
                        ExceptionErrorDialog.openError(Localization.getString("KernelSourceAction.CantOpenEditor"), e); //$NON-NLS-1$
                    }
                }
            } else {
                runExpandAction();
            }
        }
    }

}
