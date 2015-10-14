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
import org.eclipse.linuxtools.internal.vagrant.ui.views.DVMessages;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.linuxtools.vagrant.core.VagrantException;

public class StopVMCommandHandler extends BaseVMCommandHandler {

	private static final String CONTAINERS_STOP_MSG = "ContainersStop.msg"; //$NON-NLS-1$
	private static final String CONTAINER_STOP_MSG = "ContainerStop.msg"; //$NON-NLS-1$
	private static final String CONTAINER_STOP_ERROR_MSG = "ContainerStopError.msg"; //$NON-NLS-1$

	@Override
	void executeInJob(final IVagrantVM vm) {
		IVagrantConnection connection = VagrantConnection.getInstance();
		try {
			connection.haltVM(vm.id());
		} catch (VagrantException | InterruptedException e) {
			final String errorMessage = DVMessages.getFormattedString(CONTAINER_STOP_ERROR_MSG, vm.id());
			openError(errorMessage, e);
		} finally {
			connection.getVMs(true);
		}
	}

	@Override
	String getJobName(final List<IVagrantVM> selectedVMs) {
		return DVMessages.getString(CONTAINERS_STOP_MSG);
	}

	@Override
	String getTaskName(final IVagrantVM vm) {
		return DVMessages.getFormattedString(CONTAINER_STOP_MSG, vm.id());
	}
}
