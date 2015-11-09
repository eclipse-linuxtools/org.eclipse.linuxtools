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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.VagrantException;
import org.eclipse.linuxtools.vagrant.core.VagrantService;

public class RemoveBoxesCommandHandler extends BaseBoxesCommandHandler {

	@Override
	String getJobName(List<IVagrantBox> selectedImages) {
		return "Removing Boxes...";
	}

	@Override
	String getTaskName(IVagrantBox image) {
		return "Removing " + image.getName();
	}

	@Override
	void executeInJob(IVagrantBox image, IProgressMonitor monitor) {
		IVagrantConnection connection = VagrantService.getInstance();
		try {
			connection.removeBox(image.getName());
		} catch (VagrantException | InterruptedException e) {
			final String errorMessage = "Error in deleting " + image.getName();
				openError(errorMessage, e);
		} finally {
			// always get images as we sometimes get errors on intermediate
			// images
			// being removed but we will remove some top ones successfully
			connection.getBoxes(true);
		}
	}
}
