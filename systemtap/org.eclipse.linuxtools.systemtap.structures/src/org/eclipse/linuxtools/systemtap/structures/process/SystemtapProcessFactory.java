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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * The {@link SystemtapProcessFactory} is responsible for creating process
 * objects.
 * When executing systemtap operations you must always use this factory so that
 * mock objects can be provided during testing.
 */
public class SystemtapProcessFactory {

    private static final int DEFAULT_PORT = 22;

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
        return execRemote(args, out, err, user, host, password, DEFAULT_PORT, null);
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
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no"); //$NON-NLS-1$//$NON-NLS-2$
        session.setConfig(config);
        session.connect();

        StringBuilder command = new StringBuilder();
        if (envp != null) {
            for (String var : envp) {
                command.append(String.format("export %s; ", var)); //$NON-NLS-1$
            }
        }
        for (int i = 0; i < args.length; i++) {
            command.append(args[i] + ' ');
        }

        ChannelExec channel = (ChannelExec) session.openChannel("exec"); //$NON-NLS-1$
        channel.setCommand(command.toString());
        channel.setInputStream(null, true);
        channel.setOutputStream(out, true);
        channel.setExtOutputStream(err, true);
        channel.connect();

        return channel;
    }

    /**
     * Runs stap with the given arguments on the given host using the given
     * credentials and waits for the process to finish executing.
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
        return execRemote(args, out, err, user, host, password, DEFAULT_PORT, null);
    }

    /**
     * Runs stap with the given arguments on the given host using the given
     * credentials and waits for the process to finish executing.
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
        Channel channel = execRemote(args, out, err, user, host, password, port, envp);

        while (!channel.isClosed()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // Thread was interrupted just return.
                return channel;
            }
        }

        return channel;
    }
}
