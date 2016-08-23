/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.IDockerConnection2;
import org.eclipse.linuxtools.internal.docker.ui.jobs.RetrieveImageHierarchyJob;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImageHierarchyView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for the Images and Containers Hierarchy View
 */
public class OpenInHierarchyViewCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		// retrieve the selected image
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final IDockerConnection2 currentConnection = (IDockerConnection2) CommandUtils
				.getCurrentConnection(activePart);
		// run a job to retrieve the image hierarchy
		final RetrieveImageHierarchyJob retrieveImageHierarchyJob = new RetrieveImageHierarchyJob(
				currentConnection, CommandUtils.getSelectedElement(activePart));
		retrieveImageHierarchyJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				// open the Image Hierarchy View and set the selected image as
				// the new
				// input
				Display.getDefault().asyncExec(() -> {
					try {
						final DockerImageHierarchyView dockerImageHierarchyView = (DockerImageHierarchyView) PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage()
								.showView(DockerImageHierarchyView.VIEW_ID);
						dockerImageHierarchyView.show(
								retrieveImageHierarchyJob.getImageHierarchy());
					} catch (PartInitException e) {
						Activator.logErrorMessage(CommandMessages.getString(
								"command.showIn.propertiesView.failure"), //$NON-NLS-1$
								e);

					}
				});
			}
		});
		retrieveImageHierarchyJob.schedule();
		//
		return null;
	}

}
