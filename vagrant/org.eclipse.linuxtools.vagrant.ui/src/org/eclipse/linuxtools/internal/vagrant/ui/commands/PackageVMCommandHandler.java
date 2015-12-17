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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.internal.vagrant.ui.wizards.PackageVMWizard;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.linuxtools.vagrant.core.VagrantException;
import org.eclipse.linuxtools.vagrant.core.VagrantService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

public class PackageVMCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		List<IVagrantVM> vms = CommandUtils.getSelectedContainers(selection);
		IVagrantVM vm = vms.iterator().next();
		PackageVMWizard wizard = new PackageVMWizard();
		boolean finished = CommandUtils.openWizard(wizard, HandlerUtil.getActiveShell(event));
		if (finished) {
			performPackageVM(vm, wizard.getBoxName(), Paths.get(wizard.getBoxFolder()));
		}
		return null;
	}

	private void performPackageVM(IVagrantVM vm, String name, Path dest) {
		final Job packageVMJob = new Job(Messages.PackageVMCommandHandler_title) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(NLS.bind(Messages.PackageVMCommandHandler_msg, vm.id()), IProgressMonitor.UNKNOWN);
				IVagrantConnection connection = VagrantService.getInstance();
				try {
					connection.packageVM(vm, name);
					// Wait for the box to be created
					Path vmBox = Paths.get(vm.directory().getAbsolutePath(), name);
					while (!vmBox.toFile().exists()) {
						try {
							Thread.sleep(1000);
							if (monitor.isCanceled()) {
								return Status.CANCEL_STATUS;
							}
						} catch (InterruptedException e) {
						}
					}
					/*
					 * Manually move the new box to the desired destination
					 * since 'virtualbox' and 'libvirt' providers differ
					 * slightly in their support for this option.
					 */
						Files.move(Paths.get(vm.directory().getAbsolutePath(), name),
								dest.resolve(name), StandardCopyOption.REPLACE_EXISTING);
				} catch (VagrantException | InterruptedException | IOException e) {
					try {
						Files.delete(Paths.get(vm.directory().getAbsolutePath(), name));
					} catch (IOException e1) {
					}
					Display.getDefault()
							.syncExec(() -> MessageDialog.openError(
									Display.getCurrent().getActiveShell(),
							Messages.PackageVMCommandHandler_failed,
							NLS.bind(Messages.PackageVMCommandHandler_failed_desc,
									new String [] {vm.id(), dest.toString(), name})));
				} finally {
					connection.getVMs(true);
				}
				return Status.OK_STATUS;
			}
		};
		packageVMJob.setUser(true);
		packageVMJob.schedule();
	}
}
