/*******************************************************************************
 * Copyright (c) 2011, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Otavio Busatto Pontes <obusatto@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tools.launch.core.factory;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IProject;

/**
 * Create process using org.eclipse.cdt.utils.spawner.ProcessFactory and
 * prepends the 'Linux tools path' project property to the environment
 * PATH.
 * Use this factory instead of Cdt ProcessFactory if the command you are
 * running may be in the linux tools path selected in the project property
 * page.
 */
public class CdtSpawnerProcessFactory extends LinuxtoolsProcessFactory {
    private static CdtSpawnerProcessFactory instance = null;

    public static CdtSpawnerProcessFactory getFactory() {
        if (instance == null) {
            instance = new CdtSpawnerProcessFactory();
        }
        return instance;
    }

    public Process exec(String[] cmdarray, IProject project) throws IOException {
        return exec(cmdarray, null, project);
    }

    public Process exec(String[] cmdarray, String[] envp, IProject project) throws IOException {
        envp = updateEnvironment(envp, project);
        return ProcessFactory.getFactory().exec(cmdarray, envp);
    }

    public Process exec(String cmdarray[], String[] envp, File dir, IProject project)
        throws IOException {
        envp = updateEnvironment(envp, project);
        return ProcessFactory.getFactory().exec(cmdarray, envp, dir);
    }

    public Process exec(String cmdarray[], String[] envp, File dir, PTY pty, IProject project)
        throws IOException {
        envp = updateEnvironment(envp, project);
        return ProcessFactory.getFactory().exec(cmdarray, envp, dir, pty);
    }

    /**
     * Executes a command array using pty
     *
     * @param cmdarray -- Split a command string on the ' ' character
     * @param envp -- Use <code>getEnvironment(ILaunchConfiguration)</code> in the AbstractCLaunchDelegate.
     * @param dir -- Working directory
     * @param usePty -- A value of 'true' usually suffices
     * @return A properly formed process, or null
     * @since 3.0
     */
    public Process exec(String[] cmdarray, String[] envp, File dir,
            boolean usePty) {
        Process process = null;
        try {
            if (dir == null) {
                process = ProcessFactory.getFactory().exec(cmdarray, envp);
            } else {
                if (PTY.isSupported() && usePty) {
                    process = ProcessFactory.getFactory().exec(cmdarray,
                            envp, dir, new PTY());
                } else {
                    process = ProcessFactory.getFactory().exec(cmdarray,
                            envp, dir);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return process;
    }
}
