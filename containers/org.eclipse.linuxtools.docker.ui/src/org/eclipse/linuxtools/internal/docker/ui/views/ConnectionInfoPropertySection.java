/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.views;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionInfo;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * @author xcoulon
 *
 */
public class ConnectionInfoPropertySection extends BasePropertySection {

	private IDockerConnection selectedConnection = null;
	private IDockerConnectionInfo connectionInfo;

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
		this.selectedConnection = (IDockerConnection) input;
		this.connectionInfo = getConnectionInfo(selectedConnection);
		if (getTreeViewer() != null) {
			getTreeViewer().setInput(connectionInfo);
			getTreeViewer().expandAll();
		}
	}

	private IDockerConnectionInfo getConnectionInfo(final IDockerConnection connection) {
		final BlockingQueue<IDockerConnectionInfo> result = new ArrayBlockingQueue<>(1);
		final Job loadConnectionInfoJob = new Job("Loading connection info...") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Loading connection info...", 1);
				try {
					result.add(connection.getInfo());
				} catch (DockerException e) {
					Activator.log(e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		loadConnectionInfoJob.schedule();
		try {
			return result.poll(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to retrieve general info on connection '" + connection.getName() + "'", e));
			return null;
		}
	}

}
