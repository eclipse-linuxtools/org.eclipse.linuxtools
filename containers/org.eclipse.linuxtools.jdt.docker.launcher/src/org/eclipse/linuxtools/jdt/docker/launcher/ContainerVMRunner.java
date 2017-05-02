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

public class ContainerVMRunner extends StandardVMRunner {

	public ContainerVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}

	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		System.out.println("Ran : " + String.join(" ", cmdLine) + " in " + workingDirectory.getAbsolutePath());
		return null;
	}

	protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
		System.out.println("Ran : " + String.join(" ", cmdLine) + " in " + workingDirectory.getAbsolutePath() + " with env.");
		return null;
	}
}
