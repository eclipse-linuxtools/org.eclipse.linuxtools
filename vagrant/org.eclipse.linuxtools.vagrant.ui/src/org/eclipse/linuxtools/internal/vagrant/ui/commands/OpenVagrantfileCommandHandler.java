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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.vagrant.ui.Activator;
import org.eclipse.linuxtools.internal.vagrant.ui.views.DVMessages;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

public class OpenVagrantfileCommandHandler extends BaseVMCommandHandler {

	private static final String OPEN_VM_FILE_MSG = "OpenVMFile.msg"; //$NON-NLS-1$
	private static final String OPEN_VM_FILE_TITLE = "OpenVMFile.title"; //$NON-NLS-1$

	@Override
	String getJobName(List<IVagrantVM> selectedVMs) {
		return DVMessages.getFormattedString(OPEN_VM_FILE_MSG);
	}

	@Override
	String getTaskName(IVagrantVM vm) {
		return DVMessages.getFormattedString(OPEN_VM_FILE_TITLE, vm.name(),
				vm.id(), vm.directory().getAbsolutePath());
	}

	@Override
	void executeInJob(IVagrantVM vm, IProgressMonitor monitor) {
		Display.getDefault().asyncExec(() -> {
			IWorkbenchPage activePage = Activator.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			IPath vgFilePath = new Path(vm.directory().getAbsolutePath())
					.append("Vagrantfile"); //$NON-NLS-1$
			IFileStore file = EFS.getLocalFileSystem().getStore(vgFilePath);
			try {
				IDE.openEditorOnFileStore(activePage, file);
			} catch (PartInitException e) {
				Activator.log(e);
			}
		});
	}

}
