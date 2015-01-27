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

import java.util.List;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.ProbeAliasAction;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ProbeNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ProbevarNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.tparsers.ProbeParser;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


/**
 * The Probe Alias Browser module of the SystemTap GUI. This class provides a list of all probe aliases
 * defined in the tapset (both the standard, and user-specified tapsets), and allows the user to insert
 * template probes into an editor.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class ProbeAliasBrowserView extends TapsetBrowserView {
    public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView"; //$NON-NLS-1$

    public ProbeAliasBrowserView() {
        super(ProbeParser.getInstance());
    }

    @Override
    protected Image getEntryImage(TreeNode treeObj) {
        //Probe variables
        if (treeObj.getData() instanceof ProbevarNodeData) {
            List<String> varTypes = ((ProbevarNodeData) treeObj.getData()).getTypes();
            if (varTypes.get(varTypes.size()-1).endsWith("*")) { //Pointers //$NON-NLS-1$
                return IDEPlugin.getImageDescriptor("icons/vars/var_long.gif").createImage(); //$NON-NLS-1$
            }
            if (varTypes.contains("struct")) {//$NON-NLS-1$
                return IDEPlugin.getImageDescriptor("icons/vars/var_struct.gif").createImage(); //$NON-NLS-1$
            }
            if (varTypes.contains("string")) {//$NON-NLS-1$
                return IDEPlugin.getImageDescriptor("icons/vars/var_str.gif").createImage(); //$NON-NLS-1$
            }
            if (varTypes.contains("unknown")) {//$NON-NLS-1$
                return IDEPlugin.getImageDescriptor("icons/vars/var_unk.gif").createImage(); //$NON-NLS-1$
            }
            // All other types are displayed as long
            return IDEPlugin.getImageDescriptor("icons/vars/var_long.gif").createImage(); //$NON-NLS-1$
        }

        //Non-variable icons
        if (treeObj.getData() instanceof ProbeNodeData) {
            return IDEPlugin.getImageDescriptor("icons/misc/probe_obj.gif").createImage(); //$NON-NLS-1$
        }
        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    }

    @Override
    protected void displayContents() {
        setViewerInput(TapsetLibrary.getProbes());
        setRefreshable(true);
    }

    @Override
    protected void makeActions() {
        doubleClickAction = new ProbeAliasAction(getSite().getWorkbenchWindow(), this);
        viewer.addDoubleClickListener(doubleClickAction);
        registerContextMenu("probePopup"); //$NON-NLS-1$
    }

}
