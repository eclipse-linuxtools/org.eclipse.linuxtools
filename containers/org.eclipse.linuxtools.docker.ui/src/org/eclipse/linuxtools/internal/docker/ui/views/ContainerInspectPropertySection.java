/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyContainerNode;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * Property section for the {@link IDockerContainer} detailed info.
 */
public class ContainerInspectPropertySection extends BasePropertySection {

	private final static String PropertiesInfoError = "PropertiesInfoError.msg"; //$NON-NLS-1$
	private final static String PropertiesLoadingContainerInfo = "PropertiesLoadingContainerInfo.msg"; //$NON-NLS-1$

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		getTreeViewer().setContentProvider(new ContainerInspectContentProvider());
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		final Object input = getSelection(selection);
		final IDockerConnection parentConnection = getConnection(part,
				selection);
		final IDockerContainerInfo containerInfo = getContainerInfo(
				parentConnection, input);
		if (getTreeViewer() != null && containerInfo != null) {
			getTreeViewer().setInput(containerInfo);
			getTreeViewer().expandAll();
		}
	}

	private IDockerContainerInfo getContainerInfo(
			final IDockerConnection parentConnection,
			final Object input) {
		Assert.isTrue(input instanceof IDockerContainer
				|| input instanceof IDockerImageHierarchyContainerNode);
		if (input instanceof IDockerContainer) {
			return getContainerInfo(parentConnection, (IDockerContainer) input);
		}
		return getContainerInfo(parentConnection,
				((IDockerImageHierarchyContainerNode) input).getElement());
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
				final IDockerContainerInfo containerInfo = connection.getContainerInfo(container.id());
				if (containerInfo != null) {
					result.add(containerInfo);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		loadConnectionInfoJob.schedule();
		try {
			return result.poll(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Activator.log(Status.error(DVMessages.getFormattedString(PropertiesInfoError, connection.getName()), e));
			return null;
		}
	}
}
