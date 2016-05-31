/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionState;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionInfo;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * @author xcoulon
 *
 */
public class ConnectionInfoPropertySection extends BasePropertySection {

	private final static String LoadingConnectionInfo = "PropertiesLoadingConnectionInfo.msg"; //$NON-NLS-1$

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage propertySheetPage) {
		super.createControls(parent, propertySheetPage);
		getTreeViewer().setContentProvider(new ConnectionInfoContentProvider());
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof ITreeSelection);
		Object input = ((ITreeSelection) selection).getFirstElement();
		Assert.isTrue(input instanceof IDockerConnection);
		updateConnectionInfo((IDockerConnection) input);
	}

	private void updateConnectionInfo(final IDockerConnection connection) {
		// Set the tree empty while we wait for the real data
		if (getTreeViewer() != null) {
			getTreeViewer().setInput(null);
			getTreeViewer().expandAll();
		}

		final Job loadConnectionInfoJob = new Job(DVMessages.getString(LoadingConnectionInfo)) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(DVMessages.getString(LoadingConnectionInfo), 1);
				if (connection
						.getState() != EnumDockerConnectionState.ESTABLISHED) {
					return Status.OK_STATUS;
				}
				try {
					final IDockerConnectionInfo info = connection.getInfo();
					Display.getDefault().asyncExec(() -> {
						if (info != null && getTreeViewer() != null) {
							getTreeViewer().setInput(info);
							getTreeViewer().expandAll();
						}
					});
				} catch (DockerException e) {
					Activator.log(e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		loadConnectionInfoJob.schedule();
	}

}
