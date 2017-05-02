/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.jdt.docker.launcher;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.launching.StandardVMRunner;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.ui.launch.ContainerLauncher;

public class ContainerVMRunner extends StandardVMRunner {

	public ContainerVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}

	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		return exec(cmdLine, workingDirectory, null);
	}

	protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
		String connectionUri = DockerConnectionManager.getInstance().getFirstConnection().getUri();
		String command = String.join(" ", cmdLine); //$NON-NLS-1$

		ContainerLauncher launch = new ContainerLauncher();
		launch.launch("org.eclipse.linuxtools.jdt.docker.launcher", null, connectionUri,
				"fedora-java", command, null, workingDirectory.getAbsolutePath(), null,
				System.getenv(), null,
				null, false, true, true);

		return null;
	}
}
