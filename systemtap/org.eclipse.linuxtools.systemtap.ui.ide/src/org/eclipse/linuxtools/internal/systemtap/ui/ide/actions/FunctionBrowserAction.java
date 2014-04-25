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

import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDESessionSettings;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.ui.IWorkbenchWindow;



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
        Object o = getSelectedElement();
        if (o instanceof TreeNode) {
            TreeNode t = (TreeNode) o;
            if (t.isClickable()) {
                STPEditor stpeditor = IDESessionSettings.getOrAskForActiveSTPEditor(true);
                if (stpeditor != null) {
                    stpeditor.insertTextAtCurrent(t.toString() + "\n"); //$NON-NLS-1$
                }
            } else {
                runExpandAction();
            }
        }
    }

}
