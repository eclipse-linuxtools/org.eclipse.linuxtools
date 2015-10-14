/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.vagrant.ui.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class BaseBoxesCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final List<IVagrantBox> selectedImages = CommandUtils.getSelectedImages(activePart);
		final Job job = new Job(getJobName(selectedImages)) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				if (confirmed(selectedImages)) {
					monitor.beginTask(getJobName(selectedImages),
							selectedImages.size());
					for (final IVagrantBox image : selectedImages) {
						monitor.setTaskName(getTaskName(image));
						executeInJob(image);
						monitor.worked(1);
					}
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.LONG);
		job.setUser(true);
		job.schedule();
		return null;
	}

	void openError(final String errorMessage, final Exception e) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						errorMessage,
						e.getMessage());
			}
		});
	}

	// allow commands to add confirmation dialog
	boolean confirmed(
			@SuppressWarnings("unused") List<IVagrantBox> selectedImages) {
		return true;
	}
	
	abstract String getJobName(final List<IVagrantBox> selectedImages);

	abstract String getTaskName(final IVagrantBox image);
	
	abstract void executeInJob(final IVagrantBox image);
}
