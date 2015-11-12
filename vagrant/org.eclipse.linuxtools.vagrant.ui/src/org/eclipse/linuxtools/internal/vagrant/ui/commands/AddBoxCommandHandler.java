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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.vagrant.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.vagrant.ui.wizards.AddBoxWizard;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.VagrantException;
import org.eclipse.linuxtools.vagrant.core.VagrantService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddBoxCommandHandler extends AbstractHandler {

	private static final String PULL_IMAGE_JOB_TITLE = "ImagePull.title"; //$NON-NLS-1$
	private static final String PULL_IMAGE_JOB_TASK = "ImagePull.msg"; //$NON-NLS-1$
	private static final String ERROR_PULLING_IMAGE = "ImagePullError.msg"; //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent event) {
		final AddBoxWizard wizard = new AddBoxWizard();
		final boolean pullImage = CommandUtils.openWizard(wizard,
				HandlerUtil.getActiveShell(event));
		if (pullImage) {
			performPullImage(wizard.getBoxName(), wizard.getBoxLoc());
		}
		return null;
	}

	private void performPullImage(final String boxName, final String boxLoc) {
		final Job pullImageJob = new Job(DVMessages
				.getFormattedString(PULL_IMAGE_JOB_TITLE, boxName)) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(DVMessages.getString(PULL_IMAGE_JOB_TASK),
						IProgressMonitor.UNKNOWN);
				// pull the image and let the progress
				// handler refresh the images when done
				try {
					IVagrantConnection connection = VagrantService.getInstance();
					connection.addBox(boxName, boxLoc);
					connection.getBoxes(true);
				} catch (final VagrantException e) {
					Display.getDefault()
							.syncExec(() -> MessageDialog.openError(
									Display.getCurrent().getActiveShell(),
									DVMessages.getFormattedString(
											ERROR_PULLING_IMAGE, boxName),
							e.getMessage()));
					// for now
				} catch (InterruptedException e) {
					// do nothing
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};

		pullImageJob.schedule();

	}

}
