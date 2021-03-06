/*******************************************************************************
 * Copyright (c) 2012, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.ssh.proxy;

import java.io.OutputStream;
import java.net.URI;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SSHCommandLauncher extends SSHBase implements IRemoteCommandLauncher {
    private SSHProcess fProcess;

    /**
     * Creates a new launcher Fills in stderr and stdout output to the given
     * streams. Streams can be set to <code>null</code>, if output not
     * required
     */
    public SSHCommandLauncher(URI uri) {
        super(uri);
    }

    @Override
    public Process execute(IPath commandPath, String[] args, String[] env,
            IPath changeToDirectory, IProgressMonitor monitorm, PTY pty)
            throws CoreException {
        StringBuilder cmd = new StringBuilder();

        if (changeToDirectory != null)
            cmd.append("cd " + changeToDirectory.toString() + "; "); //$NON-NLS-1$ //$NON-NLS-2$

        cmd.append(commandPath.toString());
        cmd.append(" "); //$NON-NLS-1$
        if (args != null)
            for (String s : args) {
                cmd.append("\"" + s + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                cmd.append(" "); //$NON-NLS-1$
            }

        try{
            ChannelExec channel = createChannelExec();

            if (env != null)
                for (String s : env) {
                    String[] tokens = s.split("=", 2); //$NON-NLS-1$
                    switch (tokens.length) {
                        case 1:
                            channel.setEnv(tokens[0], null);
                            break;
                        case 2:
                            channel.setEnv(tokens[0], tokens[1]);
                            break;
                        default:
                            Activator.log(IStatus.WARNING, Messages.SSHCommandLauncher_malformed_env_var_string + s);
                    }
                }

            channel.setCommand(cmd.toString());
            channel.connect();
            fProcess = new SSHProcess(channel);
            return fProcess;
        } catch (JSchException e) {
            throw new CoreException(Status.error(Messages.SSHCommandLauncher_execution_problem + e.getMessage(), e));
        }
    }

    @Override
    public int waitAndRead(OutputStream output, OutputStream err, IProgressMonitor monitor) {
        if (fProcess == null)
            return IRemoteCommandLauncher.ILLEGAL_COMMAND;

        return fProcess.waitAndRead(output, err, monitor);
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public Process execute(IPath commandPath, String[] args, String[] env,
            IPath changeToDirectory, IProgressMonitor monitor) throws CoreException {
        return execute(commandPath, args, env, changeToDirectory, monitor, null);
    }
}
