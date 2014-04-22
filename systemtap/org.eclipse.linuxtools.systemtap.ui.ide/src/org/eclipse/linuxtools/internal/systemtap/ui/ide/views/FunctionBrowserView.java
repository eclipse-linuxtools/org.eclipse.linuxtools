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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.views;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.FunctionBrowserAction;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.FunctionParser;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ISingleTypedNode;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;



/**
 * This class is the Function Tapset Browser, which provides a list of all of the functions
 * defined in the tapset library for the user to browse through.
 * @author Ryan Morse
 * @author Henry Hughes
 */
public class FunctionBrowserView extends BrowserView {
    public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView"; //$NON-NLS-1$

    /**
     * Creates the UI on the given <code>Composite</code>
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        FunctionParser.getInstance().addListener(viewUpdater);
        refresh();
        makeActions();
    }

    @Override
    protected Image getEntryImage(TreeNode treeObj) {
        if (!(treeObj.getData() instanceof ISingleTypedNode)) {
            return IDEPlugin.getImageDescriptor("icons/vars/var_unk.gif").createImage(); //$NON-NLS-1$
        }
        String type = ((ISingleTypedNode) treeObj.getData()).getType();
        if (type == null) {
            return IDEPlugin.getImageDescriptor("icons/vars/var_void.gif").createImage(); //$NON-NLS-1$
        } else if (type.equals("long")) {//$NON-NLS-1$
            return IDEPlugin.getImageDescriptor("icons/vars/var_long.gif").createImage(); //$NON-NLS-1$
        } else if (type.equals("string")) {//$NON-NLS-1$
            return IDEPlugin.getImageDescriptor("icons/vars/var_str.gif").createImage(); //$NON-NLS-1$
        } else {
            return IDEPlugin.getImageDescriptor("icons/vars/var_unk.gif").createImage(); //$NON-NLS-1$
        }
    }

    /**
     * Refreshes the list of functions in the viewer.
     */
    @Override
    public void refresh() {
        tree = TapsetLibrary.getFunctions();
        if (tree != null) {
            viewer.setInput(tree);
        }
    }

    /**
     * Wires up all of the actions for this browser, such as double and right click handlers.
     */
    private void makeActions() {
        doubleClickAction = new FunctionBrowserAction(getSite().getWorkbenchWindow(), this);
        viewer.addDoubleClickListener(doubleClickAction);
        registerContextMenu("functionPopup"); //$NON-NLS-1$
    }

    @Override
    public void dispose() {
        super.dispose();
        FunctionParser.getInstance().removeListener(viewUpdater);
    }

}
