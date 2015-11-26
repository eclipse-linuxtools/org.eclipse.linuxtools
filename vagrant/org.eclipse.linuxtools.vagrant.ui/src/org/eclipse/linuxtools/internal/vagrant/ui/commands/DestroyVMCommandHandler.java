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

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.vagrant.core.Activator;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.linuxtools.vagrant.core.VagrantException;
import org.eclipse.linuxtools.vagrant.core.VagrantService;

public class DestroyVMCommandHandler extends BaseVMCommandHandler {

	@Override
	String getJobName(List<IVagrantVM> selectedVMs) {
		return Messages.DestroyVMCommandHandler_removing_title;
	}

	@Override
	String getTaskName(IVagrantVM vm) {
		return Messages.DestroyVMCommandHandler_removing_msg + vm.id();
	}

	@Override
	void executeInJob(IVagrantVM vm, IProgressMonitor monitor) {
		IVagrantConnection connection = VagrantService.getInstance();
		try {
			connection.destroyVM(vm);
			String stateLoc = Activator.getDefault().getStateLocation().toOSString();
			File vagrantDir = Paths.get(stateLoc, vm.name()).toFile();
			CommandUtils.delete(vagrantDir);
		} catch (VagrantException | InterruptedException e) {
			final String errorMessage = Messages.DestroyVMCommandHandler_error + vm.id();
			openError(errorMessage, e);
		} finally {
			// always get images as we sometimes get errors on intermediate
			// images
			// being removed but we will remove some top ones successfully
			connection.getVMs(true);
		}
	}
}
