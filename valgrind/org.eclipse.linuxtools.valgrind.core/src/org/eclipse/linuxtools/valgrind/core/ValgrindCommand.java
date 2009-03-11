/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

public class ValgrindCommand {
	protected static final String WHICH_CMD = "which"; //$NON-NLS-1$
	protected static final String VALGRIND_CMD = "valgrind"; //$NON-NLS-1$

	protected Process process;
	protected String[] args;

	public String whichValgrind() throws IOException {
		StringBuffer out = new StringBuffer();
		Process p = Runtime.getRuntime().exec(WHICH_CMD + " " + VALGRIND_CMD); //$NON-NLS-1$
		readIntoBuffer(out, p);
		return out.toString().trim();
	}

	public String whichVersion(String whichValgrind) throws IOException {
		StringBuffer out = new StringBuffer();
		Process p = Runtime.getRuntime().exec(new String[] { whichValgrind, CommandLineConstants.OPT_VERSION });
		readIntoBuffer(out, p);
		return out.toString().trim();
	}
	
	public void execute(String[] commandArray, String[] env, File wd, boolean usePty) throws IOException {
		args = commandArray;
		try {
			if (wd == null) {
				process = ProcessFactory.getFactory().exec(commandArray, env);
			}
			else {
				if (PTY.isSupported() && usePty) {
					process = ProcessFactory.getFactory().exec(commandArray, env, wd, new PTY());
				}
				else {
					process = ProcessFactory.getFactory().exec(commandArray, env, wd);
				}
			}
		}
		catch (IOException e) {
			if (process != null) {
				process.destroy();
			}
			throw e;		
		}
	}

	public Process getProcess() {
		return process;
	}

	public String getCommandLine() {
		StringBuffer ret = new StringBuffer();
		for (String arg : args) {
			ret.append(arg + " "); //$NON-NLS-1$
		}
		return ret.toString().trim();
	}

	private void readIntoBuffer(StringBuffer out, Process p) throws IOException {
		boolean success;
		InputStream in;
		try {
			if (success = (p.waitFor() == 0)) {
				in = p.getInputStream();
			}
			else {
				in = p.getErrorStream();
			}
			int ch;
			while ((ch = in.read()) != -1) {
				out.append((char) ch);
			}
			if (!success) {
				throw new IOException(out.toString());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
