/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.callgraph.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.callgraph.core.PluginConstants;
import org.eclipse.linuxtools.callgraph.core.SystemTapParser;
import org.eclipse.linuxtools.callgraph.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.callgraph.core.SystemTapView;
import org.eclipse.ui.progress.UIJob;

/**
 * Initializes and runs a StapGraph and TreeViewer within the SystemTap View
 * 
 * @author chwang
 * 
 */
public class GraphUIJob extends UIJob {
	private SystemTapParser parser;
	private String viewID;
	private SystemTapView viewer;

	public GraphUIJob(String name, SystemTapParser parser, String viewID) {
		super(name);
		// CREATE THE SHELL
		this.parser = parser;
		this.viewID = viewID;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {

		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = reg.getConfigurationElementsFor(
				PluginConstants.VIEW_RESOURCE, PluginConstants.VIEW_NAME,
				viewID);

		if (extensions == null || extensions.length < 1) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
					Messages.getString("GraphUIJob.0"), Messages.getString("GraphUIJob.1"), //$NON-NLS-1$ //$NON-NLS-2$
					"Could not load view with id: " + viewID); //$NON-NLS-1$
			mess.schedule();
			return Status.CANCEL_STATUS;
		}

		IConfigurationElement element = extensions[0];

		SystemTapView view;
		try {
			view = (SystemTapView) element
					.createExecutableExtension(PluginConstants.ATTR_CLASS);
			view.forceDisplay();
			viewer = view.getSingleInstance();
			if (!viewer.setParser(parser))
				return Status.CANCEL_STATUS;
			viewer.initialize(this.getDisplay(), monitor);
			if (!parser.realTime) {
				viewer.updateMethod();
			}
			viewer.setKillButtonEnabled(true);
			 
			return Status.OK_STATUS;
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return Status.CANCEL_STATUS;
	}

	/**
	 * For easier JUnit testing only. Allows public access to run method without
	 * scheduling an extra job.
	 * 
	 * @param m
	 * @return
	 */
	public IStatus testRun(IProgressMonitor m) {
		return runInUIThread(m);
	}

	public SystemTapView getViewer() {
		return viewer;
	}
}
