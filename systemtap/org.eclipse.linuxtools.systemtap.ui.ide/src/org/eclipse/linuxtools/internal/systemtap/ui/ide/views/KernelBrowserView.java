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

import java.io.File;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.hidden.KernelSourceAction;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.KernelSourceTree;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;



/**
 * The Kernel Source Browser module for the SystemTap GUI. This browser provides a list of kernel source
 * files and allows the user to open those files in an editor in order to place probes in arbitary locations.
 * @author Henry Hughes
 * @author Ryan Morse
 */

@SuppressWarnings("deprecation")
public class KernelBrowserView extends BrowserView {
	public KernelBrowserView() {
		super();
		LogManager.logInfo("Initializing", this);
	}
	
	/**
	 * Creates the UI on the given <code>Composite</code>
	 */
	public void createPartControl(Composite parent) {
		LogManager.logDebug("Start createPartControl: parent-" + parent, this);
		super.createPartControl(parent);

		refresh();
		makeActions();
		LogManager.logDebug("End createPartControl", this);
	}

	/**
	 * Wires up all of the actions for this browser, such as double and right click handlers.
	 */
	public void makeActions() {
		LogManager.logDebug("Start makeActions:", this);
		doubleClickAction = new KernelSourceAction(getSite().getWorkbenchWindow(), this);
		dblClickListener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				LogManager.logDebug("Start doubleClick: event-" + event, this);
				doubleClickAction.run();
				LogManager.logDebug("End doubleClick:", this);
			}
		};
		viewer.addDoubleClickListener(dblClickListener);
		IDEPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
		LogManager.logDebug("End makeActions:", this);
	}

	/**
	 * Updates the kernel source displayed to the user with the new kernel source tree. Usually
	 * a response to the user changing the preferences related to the kernel source location, requiring
	 * that the application update the kernel source information.
	 */
	public void refresh() {
		LogManager.logDebug("Start refresh:", this);
		KernelSourceTree kst = new KernelSourceTree();
		
		IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
		String kernelSource = p.getString(IDEPreferenceConstants.P_KERNEL_SOURCE);
		if(null == kernelSource || kernelSource.length() < 1) {
			LogManager.logInfo("Kernel Source Directory not found, querying", this);
			
			DirectoryDialog dialog= new DirectoryDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN);
			dialog.setText(Localization.getString("KernelBrowserView.WhereKernelSource"));
			kernelSource = dialog.open();

			if(null == kernelSource)
				kernelSource = "";
			p.setValue(IDEPreferenceConstants.P_KERNEL_SOURCE, kernelSource);
		}
		
		String[] excluded = p.getString(IDEPreferenceConstants.P_EXCLUDED_KERNEL_SOURCE).split(File.pathSeparator);
		
		kst.buildKernelTree(kernelSource, excluded);
		if(null != kst)
			super.viewer.setInput(kst.getTree());
		else
			super.viewer.setInput(new TreeNode("", false));

		kst.dispose();
		LogManager.logDebug("End refresh:", this);
	}
	
	/**
	 * A <code>IPropertyChangeListener</code> that detects changes to the Kernel Source location
	 * and runs the <code>updateKernelSourceTree</code> method.
	 */
	private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			LogManager.logDebug("Start propertyChange: event-" + event, this);
			if(event.getProperty().equals(IDEPreferenceConstants.P_KERNEL_SOURCE)) {
				refresh();
			}
			LogManager.logDebug("End propertyChange:", this);
		}
	};
	
	public void dispose() {
		LogManager.logInfo("Disposing", this);
		super.dispose();
		IDEPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(propertyChangeListener);
		if(null != viewer)
			viewer.removeDoubleClickListener(dblClickListener);
		dblClickListener = null;
		if(null != doubleClickAction)
			doubleClickAction.dispose();
		doubleClickAction = null;
	}
	
	public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.views.KernelBrowserView";
	private KernelSourceAction doubleClickAction;
	private IDoubleClickListener dblClickListener;
}
