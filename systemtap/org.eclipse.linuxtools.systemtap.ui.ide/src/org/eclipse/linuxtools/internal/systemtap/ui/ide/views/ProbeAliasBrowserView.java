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
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.ProbeNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.ProbevarNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;


/**
 * The Probe Alias Browser module of the SystemTap GUI. This class provides a list of all probe aliases
 * defined in the tapset (both the standard, and user-specified tapsets), and allows the user to insert
 * template probes into an editor.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class ProbeAliasBrowserView extends BrowserView {
	public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView"; //$NON-NLS-1$
	private ProbeAliasAction doubleClickAction;

	/**
	 * Creates the UI on the given <code>Composite</code>
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		TapsetLibrary.init();
		TapsetLibrary.addProbeListener(new ViewUpdater());
		refresh();
		makeActions();
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
		return getGenericImage(treeObj);
	}

	/**
	 * Refreshes the list of probe aliases in the viewer.
	 */
	@Override
	public void refresh() {
		TreeNode probes = TapsetLibrary.getProbes();
		if (probes != null){
			super.viewer.setInput(probes);
		}
	}

	/**
	 * Wires up all of the actions for this browser, such as double and right click handlers.
	 */
	private void makeActions() {
		doubleClickAction = new ProbeAliasAction(getSite().getWorkbenchWindow(), this);
		viewer.addDoubleClickListener(doubleClickAction);
		registerContextMenu("probePopup"); //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		super.dispose();
		if (null != viewer) {
			viewer.removeDoubleClickListener(doubleClickAction);
		}
		if (null != doubleClickAction) {
			doubleClickAction.dispose();
		}
		doubleClickAction = null;
	}
}
