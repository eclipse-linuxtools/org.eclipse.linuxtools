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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class ContainerInspectPropertySection extends BasePropertySection {

	private final static String PropertiesInfoError = "PropertiesInfoError.msg"; //$NON-NLS-1$
	private final static String PropertiesLoadingContainerInfo = "PropertiesLoadingContainerInfo.msg"; //$NON-NLS-1$

	private IDockerContainer selectedContainer;
	private Object containerInfo;

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		getTreeViewer().setContentProvider(new ContainerInspectContentProvider());
	}
	
	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		Object input = null;
		if (selection instanceof ITreeSelection)
			input = ((ITreeSelection) selection).getFirstElement();
		else if (selection instanceof IStructuredSelection)
			input = ((IStructuredSelection) selection).getFirstElement();
		Assert.isTrue(input instanceof IDockerContainer);
		this.selectedContainer = (IDockerContainer) input;
		final IDockerConnection parentConnection;
		if (part instanceof DockerContainersView) {
			parentConnection = ((DockerContainersView) part).getConnection();
		} else {
			parentConnection = (IDockerConnection) ((ITreeSelection) selection)
					.getPathsFor(selectedContainer)[0].getFirstSegment();
		}
		this.containerInfo = getContainerInfo(parentConnection, selectedContainer);
		if (getTreeViewer() != null && this.containerInfo != null) {
			getTreeViewer().setInput(containerInfo);
			getTreeViewer().expandAll();
		}
	}

	/**
	 * @return the {@link IDockerContainerInfo} for the given
	 *         {@link IDockerConnection} and {@link IDockerContainer}, or
	 *         <code>null</code> if none was found or if an underlying problem
	 *         occurred.
	 * @param connection the current {@link IDockerConnection}.
	 * @param container the {@link IDockerContainer} to inspect.
	 */
	private IDockerContainerInfo getContainerInfo(final IDockerConnection connection, final IDockerContainer container) {
		final BlockingQueue<IDockerContainerInfo> result = new ArrayBlockingQueue<>(1);
		final Job loadConnectionInfoJob = new Job(
				DVMessages.getString(PropertiesLoadingContainerInfo)) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(
						DVMessages.getString(PropertiesLoadingContainerInfo), 1);
				result.add(connection.getContainerInfo(container.id()));
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		loadConnectionInfoJob.schedule();
		try {
			return result.poll(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					DVMessages.getFormattedString(PropertiesInfoError,
							connection.getName()), e));
			return null;
		}
	}
}
