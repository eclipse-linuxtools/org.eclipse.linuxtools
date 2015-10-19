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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.vagrant.core.Activator;
import org.eclipse.linuxtools.internal.vagrant.core.VagrantConnection;
import org.eclipse.linuxtools.internal.vagrant.ui.views.DVMessages;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;

public class CreateVmCommandHandler extends BaseBoxesCommandHandler {

	private static final String CREATE_VM_MSG = "CreateVM.msg"; //$NON-NLS-1$
	private static final String CRATE_VM_TITLE = "CreateVM.title"; //$NON-NLS-1$

	@Override
	String getJobName(List<IVagrantBox> selectedBoxes) {
		return DVMessages.getFormattedString(CREATE_VM_MSG);
	}

	@Override
	String getTaskName(IVagrantBox box) {
		return DVMessages.getFormattedString(CRATE_VM_TITLE, box.getName());
	}

	@Override
	void executeInJob(IVagrantBox box, IProgressMonitor monitor) {
		IVagrantConnection connection = VagrantConnection.getInstance();
		String stateLoc = Activator.getDefault().getStateLocation().toOSString();
		File vagrantDir = Paths.get(stateLoc, box.getName()).toFile();
		vagrantDir.mkdir();
		connection.init(vagrantDir);

		Path vagrantFilePath = Paths.get(stateLoc, box.getName(), "Vagrantfile");
		String defaultContent;
		StringBuffer bcontent = new StringBuffer();
		try {
			defaultContent = new String(Files.readAllBytes(vagrantFilePath), StandardCharsets.UTF_8);
			for (String line : defaultContent.split("\n")) {
				if (line.contains("config.vm.box")) {
					String defLine = line.replaceAll("config.vm.box = \".*\"", "config.vm.define :" + box.getName());
					String boxLine = line.replaceAll("config.vm.box = \".*\"", "config.vm.box = \"" + box.getName() + "\"");
					bcontent.append(defLine + "\n");
					bcontent.append(boxLine + "\n");
				} else {
					bcontent.append(line + "\n");
				}
			}

			Files.write(vagrantFilePath, bcontent.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
		}

		Process p = connection.up(vagrantDir);
		BufferedReader buff = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		try {
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
	}
}
