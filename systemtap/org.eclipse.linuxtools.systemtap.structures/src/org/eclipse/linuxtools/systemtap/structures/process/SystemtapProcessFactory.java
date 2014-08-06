/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation.
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures.process;

import java.io.OutputStream;

import org.eclipse.linuxtools.tools.launch.core.factory.LinuxtoolsProcessFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

/**
 * @deprecated Use {@link LinuxtoolsProcessFactory} instead.
 */
@Deprecated
public class SystemtapProcessFactory {

    /**
     * Runs stap with the given arguments on the given host using the given
     * credentials.
     *
     * @param user the user name to use on the remote machine.
     * @param host the host where the systemtap process will be run.
     * @param password password for authenticating with the given host.
     * @return a {@link Channel} connected to the remotely running process.
     * @throws JSchException thrown if there are problems connecting to the remote machine.
     */
    public static Channel execRemote(String[] args,
            OutputStream out, OutputStream err, String user, String host,
            String password) throws JSchException {
        return LinuxtoolsProcessFactory.execRemote(args, out, err, user, host, password);
    }

    /**
     * Runs stap with the given arguments on the given host using the given
     * credentials.
     *
     * @param user the user name to use on the remote machine.
     * @param host the host where the systemtap process will be run.
     * @param password password for authenticating with the given host.
     * @param envp an array with extra enviroment variables to be used when running
     * the command. Set to <code>null</code> if none are needed.
     * @return a {@link Channel} connected to the remotely running process.
     * @throws JSchException thrown if there are problems connecting to the remote machine.
     * @since 3.0
     */
    public static Channel execRemote(String[] args, OutputStream out,
            OutputStream err, String user, String host, String password, int port, String[] envp)
                    throws JSchException {
        return LinuxtoolsProcessFactory.execRemote(args, out, err, user, host, password, port, envp);
    }

    /**
     * Runs stap with the given arguments on the given host using the given
     * credentials and waits for the process to finish executing, or until
     * the executing thread is interrupted.
     *
     * @param user the user name to use on the remote machine.
     * @param host the host where the systemtap process will be run.
     * @param password password for authenticating with the given host.
     * @return a {@link Channel} connected to the remotely running process.
     * @throws JSchException thrown if there are problems connecting to the remote machine.
     */
    public static Channel execRemoteAndWait(String[] args,
            OutputStream out, OutputStream err, String user, String host,
            String password) throws JSchException {
        return LinuxtoolsProcessFactory.execRemoteAndWait(args, out, err, user, host, password);
    }

    /**
     * Runs stap with the given arguments on the given host using the given
     * credentials and waits for the process to finish executing, or until
     * the executing thread is interrupted.
     *
     * @param user the user name to use on the remote machine.
     * @param host the host where the systemtap process will be run.
     * @param password password for authenticating with the given host.
     * @param envp an array with extra enviroment variables to be used when running
     * the command. Set to <code>null</code> if none are needed.
     * @return a {@link Channel} connected to the remotely running process.
     * @throws JSchException thrown if there are problems connecting to the remote machine.
     * @since 3.0
     */
    public static Channel execRemoteAndWait(String[] args, OutputStream out,
            OutputStream err, String user, String host, String password, int port, String[] envp)
                    throws JSchException {
        return LinuxtoolsProcessFactory.execRemoteAndWait(args, out, err, user, host, password, port, envp);
    }
}
