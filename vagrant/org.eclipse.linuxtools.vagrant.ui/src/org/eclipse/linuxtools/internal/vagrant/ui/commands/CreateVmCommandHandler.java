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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.vagrant.core.Activator;
import org.eclipse.linuxtools.internal.vagrant.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.vagrant.ui.wizards.CreateVMWizard;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.VagrantService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class CreateVmCommandHandler extends AbstractHandler {

	private static final String CREATE_VM_MSG = "CreateVM.msg"; //$NON-NLS-1$
	private static final String CRATE_VM_TITLE = "CreateVM.title"; //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final List<IVagrantBox> selectedBoxes = CommandUtils.getSelectedImages(activePart);
		if (selectedBoxes.size() <= 1) {
			IVagrantBox selectedBox = selectedBoxes.isEmpty() ? null : selectedBoxes.get(0);
			final CreateVMWizard wizard = new CreateVMWizard(selectedBox);
			final boolean finished = CommandUtils.openWizard(wizard, HandlerUtil.getActiveShell(event));
			if (finished) {
				performCreateVM(wizard.getVMName(), wizard.getBoxName(), wizard.getVMFile());
			}
		}
		return null;
	}

	private void performCreateVM(String vmName, String boxName, String vmFile) {
		final Job createVMJob = new Job(DVMessages.getFormattedString(CREATE_VM_MSG)) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(DVMessages.getFormattedString(CRATE_VM_TITLE, vmName),
						IProgressMonitor.UNKNOWN);

				IVagrantConnection connection = VagrantService.getInstance();
				File vagrantDir;
				if (vmFile == null) {
					String stateLoc = Activator.getDefault().getStateLocation().toOSString();
					vagrantDir = Paths.get(stateLoc, vmName).toFile();
					vagrantDir.mkdir();
					connection.init(vagrantDir);

					Path vagrantFilePath = Paths.get(stateLoc, vmName, "Vagrantfile");
					String defaultContent;
					StringBuffer bcontent = new StringBuffer();
					try {
						defaultContent = new String(Files.readAllBytes(vagrantFilePath), StandardCharsets.UTF_8);
						for (String line : defaultContent.split("\n")) {
							if (line.contains("config.vm.box")) {
								String defLine = line.replaceAll("config.vm.box = \".*\"", "config.vm.define :" + vmName);
								String boxLine = line.replaceAll("config.vm.box = \".*\"", "config.vm.box = \"" + boxName + "\"");
								bcontent.append(defLine + '\n');
								bcontent.append(boxLine + '\n');
							} else {
								bcontent.append(line + '\n');
							}
						}

						Files.write(vagrantFilePath, bcontent.toString().getBytes(StandardCharsets.UTF_8));
					} catch (IOException e) {
					}
				} else {
					vagrantDir = Paths.get(vmFile).getParent().toFile();
				}

				IVagrantBox box = null;
				for (IVagrantBox b : connection.getBoxes()) {
					if (b.getName().equals(boxName)) {
						box = b;
						break;
					}
				}
				Process p = connection.up(vagrantDir, box != null ? box.getProvider() : null);
				String line;
				try (BufferedReader buff = new BufferedReader(
						new InputStreamReader(p.getInputStream()))) {
					while ((line = buff.readLine()) != null) {
						if (monitor.isCanceled()) {
							p.destroy();
							break;
						}
						line = line.replaceAll("(=)+>", "");
						monitor.subTask(line);
					}
				} catch (IOException e) {
				}
				connection.getVMs(true);
				return Status.OK_STATUS;
			}
		};
		createVMJob.setUser(true);
		createVMJob.schedule();
	}
}
