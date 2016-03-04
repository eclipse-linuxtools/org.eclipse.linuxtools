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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.vagrant.core.Activator;
import org.eclipse.linuxtools.internal.vagrant.core.EnvironmentsManager;
import org.eclipse.linuxtools.internal.vagrant.core.VagrantConnection;
import org.eclipse.linuxtools.internal.vagrant.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.vagrant.ui.wizards.CreateVMWizard;
import org.eclipse.linuxtools.internal.vagrant.ui.wizards.WizardMessages;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.linuxtools.vagrant.core.VagrantException;
import org.eclipse.linuxtools.vagrant.core.VagrantService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class CreateVmCommandHandler extends AbstractHandler {

	private static final String CREATE_VM_MSG = "CreateVM.msg"; //$NON-NLS-1$
	private static final String CRATE_VM_TITLE = "CreateVM.title"; //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent event) {
		if (VagrantConnection.findVagrantPath() == null) {
			Display.getDefault()
					.syncExec(() -> MessageDialog.openError(Display.getCurrent()
							.getActiveShell(),
							WizardMessages.getString("VagrantCommandNotFound.title"), //$NON-NLS-1$
							WizardMessages.getString("VagrantCommandNotFound.msg"))); //$NON-NLS-1$
		} else {
			final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
			final List<IVagrantBox> selectedBoxes = CommandUtils
					.getSelectedImages(activePart);
			if (selectedBoxes.size() <= 1) {
				IVagrantBox selectedBox = selectedBoxes.isEmpty() ? null
						: selectedBoxes.get(0);
				final CreateVMWizard wizard = new CreateVMWizard(selectedBox);
				final boolean finished = CommandUtils.openWizard(wizard,
						HandlerUtil.getActiveShell(event));
				if (finished) {
					performCreateVM(wizard.getVMName(), wizard.getBoxReference(),
							wizard.getVMFile(), wizard.getVMEnvironment());
				}
			}
		}
		return null;
	}

	private void performCreateVM(String vmName, String boxRef, String vmFile,
			Map<String, String> environment) {
		final Job createVMJob = new Job(DVMessages.getFormattedString(CREATE_VM_MSG)) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(DVMessages.getFormattedString(CRATE_VM_TITLE, vmName),
						IProgressMonitor.UNKNOWN);
				IVagrantConnection connection = VagrantService.getInstance();
				if (findVM(connection, vmName) != null) {
					Display.getDefault()
					.syncExec(() -> MessageDialog.openError(Display.getCurrent()
							.getActiveShell(),
							WizardMessages.getString("CreateVmCommandHandler.VMExists.title"), //$NON-NLS-1$
							WizardMessages.getString("CreateVmCommandHandler.VMExists.msg"))); //$NON-NLS-1$
					return Status.CANCEL_STATUS;
				}

				IVagrantBox box = null;
				File vagrantDir;
				String boxName = boxRef;
				if (vmFile == null) {
					// The boxRef is a reference to an actual box file/url
					boolean isValidURL = true;
					try {
						new URL(boxRef);
					} catch (MalformedURLException e1) {
						isValidURL = false;
					}
					if (Paths.get(boxRef).toFile().canRead() || isValidURL) {
						try {
							String boxPath = boxRef;
							// Generate the box name from the file name (basename)
							boxName = boxRef.substring(
									boxRef.lastIndexOf(File.separator) + 1)
									.replace(".box", ""); //$NON-NLS-1$ //$NON-NLS-2$
							connection.addBox(boxName, boxPath, isValidURL);
						} catch (VagrantException e) {
						} catch (InterruptedException e) {
						}
					}

					// Init a new vagrant folder inside plugin metadata
					vagrantDir = performInit(vmName, boxName, connection);

					// box download job might not be complete so we wait
					box = findBox(connection, boxName);
					while (box == null && isValidURL) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
						connection.getVMs(true);
						box = findBox(connection, boxName);
						if (monitor.isCanceled()) {
							CommandUtils.delete(vagrantDir);
							return Status.CANCEL_STATUS;
						}
					}
				} else {
					vagrantDir = Paths.get(vmFile).getParent().toFile();
				}
				EnvironmentsManager.getSingleton().setEnvironment(vagrantDir,
						environment);

				String provider = (box == null ? null : box.getProvider());
				connection.up(vagrantDir, provider);
				connection.getVMs(true);
				return Status.OK_STATUS;
			}
		};
		createVMJob.setUser(true);
		createVMJob.schedule();
	}

	private IVagrantBox findBox(IVagrantConnection connection, String boxName) {
		IVagrantBox box = null;
		for (IVagrantBox b : connection.getBoxes()) {
			if (b.getName().equals(boxName)) {
				box = b;
				break;
			}
		}
		return box;
	}

	private IVagrantVM findVM(IVagrantConnection connection, String vmName) {
		IVagrantVM vm = null;
		for (IVagrantVM v : connection.getVMs()) {
			if (v.name().equals(vmName)) {
				vm = v;
				break;
			}
		}
		return vm;
	}

	/*
	 * init the folder in this bundle's state location, and ensure the
	 * Vagrantfile has the proper vm name and box name
	 */
	private File performInit(String vmName, String boxName,
			IVagrantConnection connection) {
		String stateLoc = Activator.getDefault().getStateLocation()
				.toOSString();
		File vagrantDir = Paths.get(stateLoc, vmName).toFile();
		// Project folder exists (but VM cannot exist here)
		// Safe to remove the stale Vagrant project entry
		if (!vagrantDir.mkdir()) {
			CommandUtils.delete(vagrantDir);
			vagrantDir.mkdir();
		}
		connection.init(vagrantDir);

		Path vagrantFilePath = Paths.get(stateLoc, vmName, "Vagrantfile"); //$NON-NLS-1$
		String defaultContent;
		StringBuffer bcontent = new StringBuffer();
		try {
			defaultContent = new String(Files.readAllBytes(vagrantFilePath),
					StandardCharsets.UTF_8);
			for (String line : defaultContent.split("\n")) { //$NON-NLS-1$
				if (line.contains("config.vm.box")) { //$NON-NLS-1$
					String defLine = line.replaceAll("config.vm.box = \".*\"", //$NON-NLS-1$
							"config.vm.define :\"" + vmName + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					String boxLine = line.replaceAll("config.vm.box = \".*\"", //$NON-NLS-1$
							"config.vm.box = \"" + boxName + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					bcontent.append(defLine + '\n');
					bcontent.append(boxLine + '\n');
				} else {
					bcontent.append(line + '\n');
				}
			}

			Files.write(vagrantFilePath,
					bcontent.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
		}
		return vagrantDir;
	}
}
