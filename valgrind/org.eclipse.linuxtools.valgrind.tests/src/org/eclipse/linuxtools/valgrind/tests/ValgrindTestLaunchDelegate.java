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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchConfigurationDelegate;

public class ValgrindTestLaunchDelegate extends ValgrindLaunchConfigurationDelegate {

	public static final String SYSTEM_PROPERTY_RUN_VALGRIND = "eclipse.valgrind.tests.runValgrind"; //$NON-NLS-1$
	protected static final String ERROR_CODE_FILE = "errorCode"; //$NON-NLS-1$

	@Override
	protected ValgrindCommand getValgrindCommand() {
		if (System.getProperty(SYSTEM_PROPERTY_RUN_VALGRIND, "yes").equals("no")) {   //$NON-NLS-1$//$NON-NLS-2$
			int exitcode = 0;
			try {
				exitcode = readErrorCode();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new ValgrindMockCommand(exitcode);
		}
		else {
			return super.getValgrindCommand();
		}
	}

	@Override
	protected void createDirectory(IPath path) throws IOException {
		if (!System.getProperty(SYSTEM_PROPERTY_RUN_VALGRIND, "yes").equals("no")) { //$NON-NLS-1$ //$NON-NLS-2$
			super.createDirectory(path);
		}
	}

	@Override
	protected void setOutputPath(ILaunchConfiguration config)
	throws CoreException, IOException {
		// Do nothing, done manually
	}

	@Override
	protected void handleValgrindError() throws IOException {
		writeErrorCode();
		super.handleValgrindError();
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
