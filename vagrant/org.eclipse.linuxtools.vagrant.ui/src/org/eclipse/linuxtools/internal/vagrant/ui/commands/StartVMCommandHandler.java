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
import org.eclipse.linuxtools.internal.vagrant.ui.views.DVMessages;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.linuxtools.vagrant.core.VagrantService;

public class StartVMCommandHandler extends BaseVMCommandHandler {

	private static final String START_VM_MSG = "StartVM.msg"; //$NON-NLS-1$
	private static final String START_VM_TITLE = "StartVM.title"; //$NON-NLS-1$

	@Override
	String getJobName(List<IVagrantVM> selectedvms) {
		return DVMessages.getFormattedString(START_VM_MSG);
	}

	@Override
	String getTaskName(IVagrantVM vm) {
		return DVMessages.getFormattedString(START_VM_TITLE, vm.name());
	}

	@Override
	void executeInJob(IVagrantVM vm, IProgressMonitor monitor) {
		IVagrantConnection connection = VagrantService.getInstance();
		connection.up(vm.directory(), vm.provider());
		connection.getVMs(true);
	}

}
