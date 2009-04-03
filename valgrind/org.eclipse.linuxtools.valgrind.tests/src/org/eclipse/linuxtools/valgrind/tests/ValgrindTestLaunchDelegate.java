/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.tests;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchConfigurationDelegate;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;

public class ValgrindTestLaunchDelegate extends ValgrindLaunchConfigurationDelegate {

	protected static final String ERROR_CODE_FILE = ".errorCode"; //$NON-NLS-1$
	
	@Override
	protected ValgrindCommand getValgrindCommand() {
		if (!ValgrindTestsPlugin.RUN_VALGRIND) {
			return new ValgrindStubCommand();
		}
		else {
			return super.getValgrindCommand();
		}
	}

	@Override
	protected void createDirectory(IPath path) throws IOException {
		if (ValgrindTestsPlugin.RUN_VALGRIND) {
			super.createDirectory(path);
		}
	}
	
	@Override
	protected IProcess createNewProcess(ILaunch launch, Process systemProcess,
			String programName) {
		IProcess process;
		if (ValgrindTestsPlugin.RUN_VALGRIND) {
			process = super.createNewProcess(launch, systemProcess, programName);
		}
		else {
			try {
				int exitcode = readErrorCode();
				process = new ValgrindStubProcess(launch, programName, exitcode);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return process;
	}

	@Override
	protected void setOutputPath(ILaunchConfiguration config)
	throws CoreException, IOException {
		if (!ValgrindTestsPlugin.GENERATE_FILES && ValgrindTestsPlugin.RUN_VALGRIND) {
			super.setOutputPath(config);
		}
	}

	@Override
	protected void handleValgrindError() throws IOException {
		if (ValgrindTestsPlugin.GENERATE_FILES) {
			writeErrorCode();
		}
		super.handleValgrindError();
	}
	
	@Override
	protected ValgrindLaunchPlugin getPlugin() {
		return ValgrindTestLaunchPlugin.getDefault();
	}
	
	private void writeErrorCode() throws IOException {
		FileWriter fw = null;
		try {
			int exitcode = process.getExitValue();
			IPath path = verifyOutputPath(config).append(ERROR_CODE_FILE);

			fw = new FileWriter(path.toFile());
			fw.write(exitcode);
		} catch (CoreException e) {
			throw new IOException(e.getLocalizedMessage());
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
	}
	
	private int readErrorCode() throws IOException {
		FileReader fr = null;
		try {
			IPath path = verifyOutputPath(config).append(ERROR_CODE_FILE);
			int exitcode = 0;
			if (path.toFile().exists()) {
				fr = new FileReader(path.toFile());
				exitcode = fr.read();
			}
			return exitcode;
		} catch (CoreException e) {
			throw new IOException(e.getLocalizedMessage());
		} finally {
			if (fr != null) {
				fr.close();
			}
		}
	}

}
