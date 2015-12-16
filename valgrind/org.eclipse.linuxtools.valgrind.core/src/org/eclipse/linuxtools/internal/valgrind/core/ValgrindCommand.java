/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Alena Laskavaia - javadoc comments and cleanup
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.tools.launch.core.factory.CdtSpawnerProcessFactory;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.linuxtools.valgrind.core.CommandLineConstants;

/**
 * Helper class to run valgrind
 */
public class ValgrindCommand {
    protected static final String WHICH_CMD = "which"; //$NON-NLS-1$
    protected static final String VALGRIND_CMD = "valgrind"; //$NON-NLS-1$

    protected Process process;
    protected String[] args;

    /**
     * The valgrind executable name
     * @return command line name
     */
    public String getValgrindCommand() {
        return VALGRIND_CMD;
    }

    /**
     * Attempt to local valgrind version
     * @param project - project to get execution context
     * @return version or emptry string if execution failed
     * @throws IOException if failed to create a process
     */
	public String whichVersion(IProject project) throws IOException {
		Process p = RuntimeProcessFactory.getFactory()
				.exec(new String[] { getValgrindCommand(), CommandLineConstants.OPT_VERSION }, project);
		try {
			StringBuffer out = new StringBuffer();
			readIntoBuffer(out, p);
			return out.toString().trim();
		} catch (IOException e) {
			e.printStackTrace(); // TODO fix
		}
		return ""; //$NON-NLS-1$
	}

    /**
     * Execute command
     * @param commandArray - command line arguments, first argument is executable itelv
     * @param env - environment variables
     * @param wd - working directory
     * @param usePty - option to allocate pty or not
     * @param project - project in context of which to launch the process
     * @throws IOException - if cannot execute the command
     */
    public void execute(String[] commandArray, String[] env, File wd, boolean usePty, IProject project) throws IOException {
        args = commandArray;
        try {
            process = startProcess(commandArray, env, wd, usePty, project);
        } catch (IOException e) {
            if (process != null) {
                process.destroy();
            }
            throw e;
        }
    }

    /**
     * Get current process
     * @return process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Get current valgrind command line, not escaped
     * @return process
     */
    public String getCommandLine() {
        StringBuffer ret = new StringBuffer();
        for (String arg : args) {
            ret.append(arg).append(" "); //$NON-NLS-1$
        }
        return ret.toString().trim();
    }

    private Process startProcess(String[] commandArray, String[] env, File workDir, boolean usePty, IProject project) throws IOException {
        if (workDir == null) {
            return CdtSpawnerProcessFactory.getFactory().exec(commandArray, env, project);
        }
        if (PTY.isSupported() && usePty) {
            return CdtSpawnerProcessFactory.getFactory().exec(commandArray, env, workDir, new PTY(), project);
        } else {
            return CdtSpawnerProcessFactory.getFactory().exec(commandArray, env, workDir, project);
        }
    }

	private void readIntoBuffer(StringBuffer out, Process p) throws IOException {
		if (p == null) {
			throw new IOException("Null Process object: unabled to read input into buffer"); //$NON-NLS-1$
		}
		// We need to get the inputs before calling waitFor
		try (InputStream err = p.getErrorStream(); InputStream input = p.getInputStream()) {
			boolean success;
			InputStream in;
			if (success = (p.waitFor() == 0)) {
				in = input;
			} else {
				in = err;
			}
			int ch;
			while ((ch = in.read()) != -1) {
				out.append((char) ch); // TODO fix reading char by char ??
			}
			if (!success) {
				throw new IOException(out.toString());
			}
		} catch (InterruptedException e) {
			e.printStackTrace(); // TODO fix
		} finally {
			p.getOutputStream().close();
		}
	}
}
