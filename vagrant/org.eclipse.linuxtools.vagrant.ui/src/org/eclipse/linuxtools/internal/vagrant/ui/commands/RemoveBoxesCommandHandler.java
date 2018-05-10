/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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
package org.eclipse.linuxtools.internal.vagrant.ui.commands;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.VagrantException;
import org.eclipse.linuxtools.vagrant.core.VagrantService;

public class RemoveBoxesCommandHandler extends BaseBoxesCommandHandler {

	@Override
	String getJobName(List<IVagrantBox> selectedImages) {
		return Messages.RemoveBoxesCommandHandler_removing_title;
	}

	@Override
	String getTaskName(IVagrantBox image) {
		return Messages.RemoveBoxesCommandHandler_removing_msg + image.getName();
	}

	@Override
	void executeInJob(IVagrantBox image, IProgressMonitor monitor) {
		IVagrantConnection connection = VagrantService.getInstance();
		try {
			connection.removeBox(image.getName());
		} catch (VagrantException | InterruptedException e) {
			final String errorMessage = Messages.RemoveBoxesCommandHandler_error + image.getName();
				openError(errorMessage, e);
		} finally {
			// always get images as we sometimes get errors on intermediate
			// images
			// being removed but we will remove some top ones successfully
			connection.getBoxes(true);
		}
	}
}
