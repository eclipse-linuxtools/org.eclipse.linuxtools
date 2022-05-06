/*******************************************************************************
 * Copyright (c) 2015, 2019 Red Hat.
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
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyImageNode;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class ImageInspectPropertySection extends BasePropertySection {

	private final static String PropertiesInfoError = "PropertiesInfoError.msg"; //$NON-NLS-1$
	private final static String PropertiesLoadingImageInfo = "PropertiesLoadingImageInfo.msg"; //$NON-NLS-1$

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		getTreeViewer().setContentProvider(new ImageInspectContentProvider());
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		final Object input = getSelection(selection);
		final IDockerConnection parentConnection = getConnection(part,
				selection);
		final IDockerImageInfo imageInfo = getImageInfo(parentConnection,
				input);
		if (getTreeViewer() != null && imageInfo != null) {
			getTreeViewer().setInput(imageInfo);
			getTreeViewer().expandAll();
		}
	}

	private IDockerImageInfo getImageInfo(
			final IDockerConnection parentConnection, final Object input) {
		if (parentConnection == null) {
			Activator.logWarningMessage(DVMessages.getString(
					"ImageInspectPropertySection.noconnection.error")); //$NON-NLS-1$
			return null;
		}
		Assert.isTrue(input instanceof IDockerImage
				|| input instanceof IDockerImageHierarchyImageNode);
		if (input instanceof IDockerImage) {
			return getImageInfo(parentConnection, (IDockerImage) input);
		}
		return getImageInfo(parentConnection,
				((IDockerImageHierarchyImageNode) input).getElement());
	}


	/**
	 * @return the {@link IDockerImageInfo} for the given
	 *         {@link IDockerConnection} and {@link IDockerImage}, or
	 *         <code>null</code> if none was found or if an underlying problem
	 *         occurred.
	 * @param connection
	 *            the current {@link IDockerConnection}.
	 * @param image
	 *            the {@link IDockerImage} to inspect.
	 */
	private IDockerImageInfo getImageInfo(final IDockerConnection connection,
			final IDockerImage image) {
		final BlockingQueue<IDockerImageInfo> result = new ArrayBlockingQueue<>(
				1);
		final Job loadConnectionInfoJob = new Job(
				DVMessages.getString(PropertiesLoadingImageInfo)) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(
						DVMessages.getString(PropertiesLoadingImageInfo), 1);
				final IDockerImageInfo imageInfo = connection
						.getImageInfo(image.id());
				if (imageInfo != null) {
					result.add(imageInfo);
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
