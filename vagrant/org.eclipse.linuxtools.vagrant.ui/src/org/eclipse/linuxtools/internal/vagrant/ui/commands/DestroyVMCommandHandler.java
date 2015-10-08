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

import org.eclipse.linuxtools.internal.vagrant.core.VagrantConnection;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.linuxtools.vagrant.core.VagrantException;

public class DestroyVMCommandHandler extends BaseVMCommandHandler {

	@Override
	String getJobName(List<IVagrantVM> selectedVMs) {
		return "Removing VMs...";
	}

	@Override
	String getTaskName(IVagrantVM vm) {
		return "Removing " + vm.id();
	}

	@Override
	void executeInJob(IVagrantVM vm) {
		IVagrantConnection connection = VagrantConnection.getInstance();
		try {
			connection.destroyVM(vm.id());
		} catch (VagrantException | InterruptedException e) {
			final String errorMessage = "Error in deleting " + vm.id();
			openError(errorMessage, e);
		} finally {
			// always get images as we sometimes get errors on intermediate
			// images
			// being removed but we will remove some top ones successfully
			connection.getVMs(true);
		}
	}
}