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

	public static final String DATA_PATH = ValgrindPlugin.getDefault().getStateLocation().toOSString();
	protected static final String OUTDIR_PREFIX = ".launch"; //$NON-NLS-1$
	
	protected File dataDir;
	protected Process process;
	protected String[] args;

	public ValgrindCommand() throws IOException {
		dataDir = new File(DATA_PATH + File.separator + OUTDIR_PREFIX + HistoryFile.getInstance().getNextIndex());
	}

	public void execute(String[] commandArray, String[] env, File wd, boolean usePty) throws IOException {
		args = commandArray;
		try {
			createDataDir();
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
	
	public File getDataDir() {
		return dataDir;
	}

	protected void createDataDir() throws IOException {
		if (dataDir.exists()) {
			// delete any preexisting files
			deleteFiles();
		}
		else if (!dataDir.mkdir()) {
			dataDir = null;
			throw new IOException(NLS.bind(Messages.getString("ValgrindCommand.Couldnt_create"), DATA_PATH)); //$NON-NLS-1$
		}
	}


	protected void deleteFiles() throws IOException {
		for (File output : dataDir.listFiles()) {
			if (!output.delete()) {
				throw new IOException(NLS.bind(Messages.getString("ValgrindCommand.Couldnt_delete"), output.getCanonicalPath())); //$NON-NLS-1$
			}
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
}
