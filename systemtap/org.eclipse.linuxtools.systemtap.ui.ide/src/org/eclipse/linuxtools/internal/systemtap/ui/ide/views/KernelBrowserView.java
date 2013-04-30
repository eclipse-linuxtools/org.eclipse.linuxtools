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

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.KernelSourceAction;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.PathPreferencePage;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.structures.KernelSourceTree;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

/**
 * The Kernel Source Browser module for the SystemTap GUI. This browser provides a list of kernel source
 * files and allows the user to open those files in an editor in order to place probes in arbitrary locations.
 * @author Henry Hughes
 * @author Ryan Morse
 */

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

		@Override
		public IStatus run(IProgressMonitor monitor) {
			IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
			KernelSourceTree kst = new KernelSourceTree();
			String excluded[] = p.getString(IDEPreferenceConstants.P_EXCLUDED_KERNEL_SOURCE).split(File.pathSeparator);
			if (remote) {
				try {
					kst.buildKernelTree(kernelLocationURI, excluded, proxy, monitor);
				} catch (CoreException e) {
					ExceptionErrorDialog.openError(Localization.getString("KernelBrowserView.CouldNotInitializeTree"), e); //$NON-NLS-1$
				}
			} else {
				kst.buildKernelTree(kernelSource, excluded);
			}
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
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

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			monitor.beginTask(Localization.getString("KernelBrowserView.UpdateKernelBrowser"), 100); //$NON-NLS-1$
			if (kst == null) {
				return Status.OK_STATUS;
			}
			viewer.setInput(kst.getTree());
			kst.dispose();
			monitor.done();
			return Status.OK_STATUS;
		}
	}

	public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.views.KernelBrowserView"; //$NON-NLS-1$
	private KernelSourceAction doubleClickAction;

	/**
	 * Creates the UI on the given <code>Composite</code>
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		refresh();
		makeActions();
	}

	/**
	 * Wires up all of the actions for this browser, such as double and right click handlers.
	 */
	public void makeActions() {
		doubleClickAction = new KernelSourceAction(getSite().getWorkbenchWindow(), this);
		viewer.addDoubleClickListener(doubleClickAction);
		IDEPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}

	/**
	 * Updates the kernel source displayed to the user with the new kernel source tree. Usually
	 * a response to the user changing the preferences related to the kernel source location, requiring
	 * that the application update the kernel source information.
	 */
	@Override
	public void refresh() {
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
				kernelLocationURI = IDEPlugin.getDefault().createRemoteUri(kernelSource);
				if (kernelLocationURI == null) {
					error = true;
				} else {
					proxy = RemoteProxyManager.getInstance().getFileProxy(kernelLocationURI);
					if (!validateProxy(proxy, kernelSource)) {
						error = true;
					}
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
	}

	private boolean validateProxy(IRemoteFileProxy proxy, String kernelSource) {
		if (proxy == null) {
			return false;
		}
		IFileStore fs = proxy.getResource(kernelSource);
		if (fs == null) {
			return false;
		}
		IFileInfo info = fs.fetchInfo();
		if (info == null) {
			return false;
		}
		if (!info.exists()) {
			return false;
		}
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
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if(event.getProperty().equals(IDEPreferenceConstants.P_KERNEL_SOURCE) ||
				event.getProperty().equals(IDEPreferenceConstants.P_REMOTE_LOCAL_KERNEL_SOURCE) ||
				event.getProperty().equals(IDEPreferenceConstants.P_EXCLUDED_KERNEL_SOURCE)) {
				refresh();
			}
		}
	};

	@Override
	public void dispose() {
		super.dispose();
		IDEPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
		if(null != viewer) {
			viewer.removeDoubleClickListener(doubleClickAction);
		}
		if(null != doubleClickAction) {
			doubleClickAction.dispose();
		}
		doubleClickAction = null;
	}
}
