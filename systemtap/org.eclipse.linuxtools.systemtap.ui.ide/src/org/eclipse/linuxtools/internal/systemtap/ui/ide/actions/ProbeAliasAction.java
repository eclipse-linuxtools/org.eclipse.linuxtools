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
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ProbeNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ProbevarNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
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
public class ProbeAliasAction extends BrowserViewAction {
    private static final String ID = "org.eclipse.linuxtools.systemtap.ui.ide.ProbeAliasAction"; //$NON-NLS-1$

    public ProbeAliasAction(IWorkbenchWindow window, ProbeAliasBrowserView view) {
        super(window, view);
        setId(ID);
        setActionDefinitionId(ID);
        setText(Localization.getString("ProbeAliasAction.Insert")); //$NON-NLS-1$
        setToolTipText(Localization
                .getString("ProbeAliasAction.InsertSelectedProbe")); //$NON-NLS-1$
    }

    /**
     * The main body of the action. This method checks for the current editor, creating one
     * if there is no active <code>STPEditor</code>, and then inserts a template probe for the
     * item that the user clicked on.
     */
    @Override
    public void run() {
        Object o = getSelectedElement();
        if (o instanceof TreeNode) {
            TreeNode t = (TreeNode) o;
            if (t.isClickable()) {
                STPEditor stpeditor = IDESessionSettings.getOrAskForActiveSTPEditor(true);
                if (stpeditor != null) {
                    stpeditor.insertText(buildString((TreeNode) o));
                }
            } else {
                runExpandAction();
            }
        }
    }

    private String buildString(TreeNode t) {
        //build the string
        StringBuilder s = new StringBuilder("\nprobe " + t.toString()); //$NON-NLS-1$
        if (t.getChildCount() > 0 && t.getChildAt(0).getData() instanceof ProbeNodeData) {
            s.append(".*"); //$NON-NLS-1$
        }
        s.append("\n{\n"); //$NON-NLS-1$
        if (t.getChildCount() > 0 && t.getChildAt(0).getData() instanceof ProbevarNodeData) {
            s.append("\t/*\n\t * " + //$NON-NLS-1$
                    Localization
                    .getString("ProbeAliasAction.AvailableVariables") + //$NON-NLS-1$
                    "\n\t * "); //$NON-NLS-1$
            boolean first = true;
            for(int i = 0; i < t.getChildCount(); i++) {
                if(first) {
                    first = false;
                } else {
                    s.append(", "); //$NON-NLS-1$
                }
                s.append(t.getChildAt(i).toString());
            }
            s.append("\n\t */\n"); //$NON-NLS-1$
        }
        s.append("\n}\n"); //$NON-NLS-1$
        return s.toString();
    }

}
