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

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.osgi.util.NLS;

public class ValgrindCommand {
	public static final String VALGRIND_CMD = "valgrind"; //$NON-NLS-1$

	public static final String OPT_TOOL = "--tool"; //$NON-NLS-1$

	public static final String OPT_XML = "--xml"; //$NON-NLS-1$
	public static final String OPT_LOGFILE = "--log-file"; //$NON-NLS-1$

	public static final String OPT_TRACECHILD = "--trace-children"; //$NON-NLS-1$
	public static final String OPT_CHILDSILENT = "--child-silent-after-fork"; //$NON-NLS-1$
	public static final String OPT_TRACKFDS = "--track-fds"; //$NON-NLS-1$
	public static final String OPT_TIMESTAMP = "--time-stamp"; //$NON-NLS-1$
	public static final String OPT_FREERES = "--run-libc-freeres"; //$NON-NLS-1$
	public static final String OPT_DEMANGLE = "--demangle"; //$NON-NLS-1$
	public static final String OPT_NUMCALLERS = "--num-callers"; //$NON-NLS-1$
	public static final String OPT_ERRLIMIT = "--error-limit"; //$NON-NLS-1$
	public static final String OPT_BELOWMAIN = "--show-below-main"; //$NON-NLS-1$
	public static final String OPT_MAXFRAME = "--max-stackframe"; //$NON-NLS-1$
	public static final String OPT_SUPPFILE = "--suppressions"; //$NON-NLS-1$

	public static final String LOG_PATH = "/tmp/" + ValgrindPlugin.PLUGIN_ID; //$NON-NLS-1$
	
	protected File tempDir;
	protected Process process;
	protected String[] args;

	public ValgrindCommand() {		
		tempDir = new File(LOG_PATH);
	}

	public void execute(String[] commandArray, String[] env, File wd, boolean usePty) throws IOException {
		args = commandArray;
		try {
			createLogDir();
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
	
	public File getTempDir() {
		return tempDir;
	}

	protected void createLogDir() throws IOException {
		if (tempDir.exists()) {
			deleteLogDir();
		}
		if (!tempDir.mkdir()) {
			tempDir = null;
			throw new IOException(NLS.bind(Messages.getString("ValgrindCommand.Couldnt_create"), LOG_PATH)); //$NON-NLS-1$
		}
	}


	protected void deleteLogDir() throws IOException {
		for (File log : tempDir.listFiles()) {
			if (!log.delete()) {
				throw new IOException(NLS.bind(Messages.getString("ValgrindCommand.Couldnt_delete"), log.getCanonicalPath())); //$NON-NLS-1$
			}
		}
		if (!tempDir.delete()) {
			throw new IOException(NLS.bind(Messages.getString("ValgrindCommand.Couldnt_delete"), LOG_PATH)); //$NON-NLS-1$
		}
	}

	public String getLogPath() {
		return LOG_PATH;
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
}
