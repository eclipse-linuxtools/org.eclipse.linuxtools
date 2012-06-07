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
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.hidden.KernelSourceAction;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.PathPreferencePage;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.KernelSourceTree;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

/**
 * The Kernel Source Browser module for the SystemTap GUI. This browser provides a list of kernel source
 * files and allows the user to open those files in an editor in order to place probes in arbitary locations.
 * @author Henry Hughes
 * @author Ryan Morse
 */

@SuppressWarnings("deprecation")
public class KernelBrowserView extends BrowserView {
	private class KernelRefreshJob extends Job {
		private boolean remote;
		private URI kernelLocationURI;
		private IRemoteFileProxy proxy;
		private String kernelSource;

		public KernelRefreshJob(boolean remote, URI kernelLocationURI, IRemoteFileProxy proxy, String kernelSource) {
			super(Localization.getString("KernelBrowserView.RefreshingKernelSource")); //$NON-NLS-1$
			this.remote = remote;
			this.kernelLocationURI = kernelLocationURI;
			this.proxy = proxy;
			this.kernelSource = kernelSource;
		}

		public IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(Localization.getString("KernelBrowserView.ReadingKernelSourceTree"), 100); //$NON-NLS-1$
			IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
			KernelSourceTree kst = new KernelSourceTree();
			String excluded[] = p.getString(IDEPreferenceConstants.P_EXCLUDED_KERNEL_SOURCE).split(File.pathSeparator);
			if (remote)
				kst.buildKernelTree(kernelLocationURI, excluded, proxy);
			else
				kst.buildKernelTree(kernelSource, excluded);
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			UpdateKernelBrowserJob job = new UpdateKernelBrowserJob(kst);
			job.schedule();
			monitor.done();
			return Status.OK_STATUS;
		}
	}

	private class UpdateKernelBrowserJob extends UIJob {
		KernelSourceTree kst;
		public UpdateKernelBrowserJob(KernelSourceTree kst) {
			super(Localization.getString("KernelBrowserView.UpdateKernelBrowser")); //$NON-NLS-1$
			this.kst = kst;
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			monitor.beginTask(Localization.getString("KernelBrowserView.UpdateKernelBrowser"), 100); //$NON-NLS-1$
			if (kst == null)
				return Status.OK_STATUS;
			viewer.setInput(kst.getTree());
			kst.dispose();
			monitor.done();
			return Status.OK_STATUS;
		}
	}

	public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.views.KernelBrowserView"; //$NON-NLS-1$
	private KernelSourceAction doubleClickAction;
	private IDoubleClickListener dblClickListener;

	public KernelBrowserView() {
		super();
		LogManager.logInfo("Initializing", this); //$NON-NLS-1$
	}

	/**
	 * Creates the UI on the given <code>Composite</code>
	 */
	public void createPartControl(Composite parent) {
		LogManager.logDebug("Start createPartControl: parent-" + parent, this); //$NON-NLS-1$
		super.createPartControl(parent);

		refresh();
		makeActions();
		LogManager.logDebug("End createPartControl", this); //$NON-NLS-1$
	}

	/**
	 * Wires up all of the actions for this browser, such as double and right click handlers.
	 */
	public void makeActions() {
		LogManager.logDebug("Start makeActions:", this); //$NON-NLS-1$
		doubleClickAction = new KernelSourceAction(getSite().getWorkbenchWindow(), this);
		dblClickListener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				LogManager.logDebug("Start doubleClick: event-" + event, this); //$NON-NLS-1$
				doubleClickAction.run();
				LogManager.logDebug("End doubleClick:", this); //$NON-NLS-1$
			}
		};
		viewer.addDoubleClickListener(dblClickListener);
		IDEPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
		LogManager.logDebug("End makeActions:", this); //$NON-NLS-1$
	}

	/**
	 * Updates the kernel source displayed to the user with the new kernel source tree. Usually
	 * a response to the user changing the preferences related to the kernel source location, requiring
	 * that the application update the kernel source information.
	 */
	public void refresh() {
		LogManager.logDebug("Start refresh:", this); //$NON-NLS-1$
		
		IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
		String kernelSource = p.getString(IDEPreferenceConstants.P_KERNEL_SOURCE);
		if(null == kernelSource || kernelSource.length() < 1) {
			showBrowserErrorMessage(Localization.getString("KernelBrowserView.NoKernelSourceFound")); //$NON-NLS-1$
			return;
		}

		String localOrRemote = p.getString(IDEPreferenceConstants.P_REMOTE_LOCAL_KERNEL_SOURCE);
		URI kernelLocationURI = null;
		IRemoteFileProxy proxy = null;
		boolean remote = localOrRemote.equals(PathPreferencePage.REMOTE);
		if (remote) {
			boolean error = false;
			try {
				kernelLocationURI = createUri(kernelSource);
				if (kernelLocationURI == null)
					error = true;
				else {
					proxy = RemoteProxyManager.getInstance().getFileProxy(kernelLocationURI);
					if (!validateProxy(proxy, kernelSource))
						error = true;
				}
			} catch (CoreException e2) {
				error = true;
			}
			if (error) {
				showBrowserErrorMessage(Localization.getString("KernelBrowserView.KernelSourceDirNotFound")); //$NON-NLS-1$
				return;
			}
		}

		KernelRefreshJob refreshJob = new KernelRefreshJob(remote, kernelLocationURI, proxy, kernelSource);
		refreshJob.setUser(true);
		refreshJob.setPriority(Job.SHORT);
		refreshJob.schedule();
		LogManager.logDebug("End refresh:", this); //$NON-NLS-1$
	}

	private boolean validateProxy(IRemoteFileProxy proxy, String kernelSource) {
		if (proxy == null)
			return false;
		IFileStore fs = proxy.getResource(kernelSource);
		if (fs == null)
			return false;
		IFileInfo info = fs.fetchInfo();
		if (info == null)
			return false;
		if (!info.exists())
			return false;
		return true;
	}
	
	private void showBrowserErrorMessage(String message) {
		TreeNode t = new TreeNode("", "", false); //$NON-NLS-1$ //$NON-NLS-2$
		t.add(new TreeNode("", message, false)); //$NON-NLS-1$
		viewer.setInput(t);
	}

	/**
	 * A <code>IPropertyChangeListener</code> that detects changes to the Kernel Source location
	 * and runs the <code>updateKernelSourceTree</code> method.
	 */
	private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			LogManager.logDebug("Start propertyChange: event-" + event, this); //$NON-NLS-1$
			if(event.getProperty().equals(IDEPreferenceConstants.P_KERNEL_SOURCE)) {
				refresh();
			}
			LogManager.logDebug("End propertyChange:", this); //$NON-NLS-1$
		}
	};
	
	public void dispose() {
		LogManager.logInfo("Disposing", this); //$NON-NLS-1$
		super.dispose();
		IDEPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(propertyChangeListener);
		if(null != viewer)
			viewer.removeDoubleClickListener(dblClickListener);
		dblClickListener = null;
		if(null != doubleClickAction)
			doubleClickAction.dispose();
		doubleClickAction = null;
	}

	private URI createUri(String path) {
		Preferences p = ConsoleLogPlugin.getDefault().getPluginPreferences();
		String user = p.getString(ConsoleLogPreferenceConstants.SCP_USER);
		String host = p.getString(ConsoleLogPreferenceConstants.HOST_NAME);
		try {
			URI uri = new URI("ssh", user, host, -1, path, null, null); //$NON-NLS-1$
			return uri;
		} catch (URISyntaxException uri) {
			return null;
		}
	}
}
