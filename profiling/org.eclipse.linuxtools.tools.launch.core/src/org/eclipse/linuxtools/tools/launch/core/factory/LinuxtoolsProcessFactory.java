/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Otavio Busatto Pontes <obusatto@br.ibm.com> - initial API and implementation
 *    Red Hat - remote executable methods
 *******************************************************************************/

package org.eclipse.linuxtools.tools.launch.core.factory;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.profiling.launch.RemoteEnvProxyManager;
import org.eclipse.linuxtools.tools.launch.core.properties.LinuxtoolsPathProperty;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Abstract class with useful functions for ProcessFactory classes,
 * and statically-accessible helper functions for running remote processes.
 */
public abstract class LinuxtoolsProcessFactory {
    private static final int DEFAULT_PORT = 22;

    private static final String PATH = "PATH"; //$NON-NLS-1$
    private static final String PATH_EQUAL = "PATH="; //$NON-NLS-1$
    private static final String SEPARATOR = ":"; //$NON-NLS-1$

    private String getEnvpPath(String[] envp) {
        if (envp == null) {
            return null;
        }
        for (String env : envp) {
            if (env.startsWith(PATH_EQUAL)) {
                return env.substring(PATH_EQUAL.length());
            }
        }
        return null;
    }

    /**
     * Update the environment variables list with system environment variables
     * and the Linux tools path project property (if applicable), and prepend
     * the latter to the PATH env in the list. Call this function if the
     * command to be run may be in the path selected in 'Linux tools path'
     * project property page.
     * @param envp The list of new environment variables to use.
     * @param project If not <code>null</code>, only the environment of this project
     * will be updated.
     */
    protected String[] updateEnvironment(String[] envp, IProject project) {
        String ltPath = LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project);
        String envpPath = getEnvpPath(envp);

        Map<String, String> envMap = new HashMap<>();
        if (project != null) {
            try {
                envMap.putAll(RemoteEnvProxyManager.class.newInstance().getEnv(project));
            } catch (InstantiationException|IllegalAccessException|CoreException e) {
                e.printStackTrace();
            }
        }
        String systemPath;
        if (!envMap.isEmpty()) {
            systemPath = envMap.get(PATH);
            if (systemPath == null) {
                systemPath = System.getenv(PATH);
                if (systemPath != null) {
                    envMap.put(PATH, systemPath);
                }
            }
        } else {
            envMap.putAll(System.getenv());
            systemPath = envMap.get(PATH);
        }

        StringBuffer newPath = new StringBuffer();
        newPath.append(PATH_EQUAL);

        if (ltPath != null && !ltPath.isEmpty()) {
            newPath.append(ltPath);
            newPath.append(SEPARATOR);
        }
        if (envpPath != null && !envpPath.isEmpty()) {
            newPath.append(envpPath);
            newPath.append(SEPARATOR);
        }
        if (systemPath != null && !systemPath.isEmpty()) {
            newPath.append(systemPath);
        }

        // Overwrite/update map of system environment variables with ones from the provided array
        if (newPath.length() > PATH_EQUAL.length()) {
            envMap.put(PATH, newPath.substring(PATH_EQUAL.length()));
        }
        if (envp != null) {
            for (String var : envp) {
                if (!var.startsWith(PATH_EQUAL)) {
                    int splitIndex = var.indexOf('=');
                    envMap.put(var.substring(0, splitIndex), var.substring(splitIndex + 1));
                }
            }
        }
        Set<String> keySet = envMap.keySet();
        String[] newEnvp = new String[keySet.size()];
        int i = 0;
        for (String key : keySet) {
            if (!key.startsWith(PATH_EQUAL)) {
                newEnvp[i] = key + "=" + envMap.get(key); //$NON-NLS-1$
            }
            i++;
        }

        return newEnvp;
    }

    /**
     * Runs a command on the given host using the given credientials.
     *
     * @param args The command to run, followed by a list of optional arguments.
     * @param out A stream for the command's standard output.
     * @param err A stream for the command's standard error output.
     * @param user The user name to use on the remote machine.
     * @param host The host where the command will be run.
     * @param password The password for authenticating with the given host.
     * @return A {@link Channel} connected to the remotely running process.
     * @throws JSchException thrown if there are problems connecting to the remote machine.
     * @since 3.1
     */
    public static Channel execRemote(String[] args,
            OutputStream out, OutputStream err, String user, String host,
            String password) throws JSchException {
        return execRemote(args, out, err, user, host, password, DEFAULT_PORT, null);
    }

    /**
     * Runs a command on the given host using the given credentials.
     *
     * @param args The command to run, followed by a list of optional arguments.
     * @param out A stream for the command's standard output.
     * @param err A stream for the command's standard error output.
     * @param user the user name to use on the remote machine.
     * @param host the host where the command will be run.
     * @param password password for authenticating with the given host.
     * @param port The port to use during remote communication.
     * @param envp an array with extra enviroment variables to be used when running
     * the command. Set to <code>null</code> if none are needed.
     * @return a {@link Channel} connected to the remotely running process.
     * @throws JSchException thrown if there are problems connecting to the remote machine.
     * @since 3.1
     */
    public static Channel execRemote(String[] args, OutputStream out,
            OutputStream err, String user, String host, String password, int port, String[] envp)
                    throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(password);
        Properties config = new Properties();
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
     * Runs a command on the given host using the given
     * credentials and waits for the process to finish executing, or until
     * the executing thread is interrupted.
     *
     * @param args The command to run, followed by a list of optional arguments.
     * @param out A stream for the command's standard output.
     * @param err A stream for the command's standard error output.
     * @param user the user name to use on the remote machine.
     * @param host the host where the command will be run.
     * @param password password for authenticating with the given host.
     * @return a {@link Channel} connected to the remotely running process.
     * @throws JSchException thrown if there are problems connecting to the remote machine.
     * @since 3.1
     */
    public static Channel execRemoteAndWait(String[] args,
            OutputStream out, OutputStream err, String user, String host,
            String password) throws JSchException {
        return execRemoteAndWait(args, out, err, user, host, password, DEFAULT_PORT, null);
    }

    /**
     * Runs a command on the given host using the given
     * credentials and waits for the process to finish executing, or until
     * the executing thread is interrupted.
     *
     * @param args The command to run, followed by a list of optional arguments.
     * @param out A stream for the command's standard output.
     * @param err A stream for the command's standard error output.
     * @param user the user name to use on the remote machine.
     * @param host the host where the command will be run.
     * @param password password for authenticating with the given host.
     * @param port The port to use during remote communication.
     * @param envp an array with extra enviroment variables to be used when running
     * the command. Set to <code>null</code> if none are needed.
     * @return a {@link Channel} connected to the remotely running process.
     * @throws JSchException thrown if there are problems connecting to the remote machine.
     * @since 3.1
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
