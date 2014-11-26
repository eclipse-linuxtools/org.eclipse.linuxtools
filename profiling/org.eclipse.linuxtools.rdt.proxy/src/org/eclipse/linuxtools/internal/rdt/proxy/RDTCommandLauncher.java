/*******************************************************************************
 * Copyright (c) 2005, 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - modify to use with RDT
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rdt.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteResource;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteProcessAdapter;
import org.eclipse.remote.core.RemoteServices;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RDTCommandLauncher implements IRemoteCommandLauncher {

    private IRemoteProcess fProcess;
    private boolean fShowCommand;
    private String[] fCommandArgs;

    private String fErrorMessage = ""; //$NON-NLS-1$

    private String lineSeparator;
    private URI uri;

    /**
     * The number of milliseconds to pause between polling.
     */
    private static final long DELAY = 50L;

    /**
     * Creates a new launcher Fills in stderr and stdout output to the given
     * streams. Streams can be set to <code>null</code>, if output not
     * required
     */
    public RDTCommandLauncher(IProject project) {
        fProcess = null;
        fShowCommand = false;
        try {
            if (project.hasNature(RDTProxyManager.SYNC_NATURE)) {
                IRemoteResource remoteRes = (IRemoteResource)project.getAdapter(IRemoteResource.class);
                uri = remoteRes.getActiveLocationURI();
            } else{
                uri = project.getLocationURI();
            }
        } catch (CoreException e) {
            uri = project.getLocationURI();
        }
        lineSeparator = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Creates a new launcher Fills in stderr and stdout output to the given
     * streams. Streams can be set to <code>null</code>, if output not
     * required
     */
    public RDTCommandLauncher(URI uri) {
        fProcess = null;
        fShowCommand = false;
        this.uri = uri;
        lineSeparator = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.ICommandLauncher#getErrorMessage()
     */
    @Override
    public String getErrorMessage() {
        return fErrorMessage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.ICommandLauncher#getCommandArgs()
     */
    private String[] getCommandArgs() {
        return fCommandArgs;
    }

    /**
     * Constructs a command array that will be passed to the process
     */
    private static String[] constructCommandArray(String command, String[] commandArgs) {
        String[] args = new String[1 + commandArgs.length];
        args[0] = command;
        System.arraycopy(commandArgs, 0, args, 1, commandArgs.length);
        return args;
    }


    /**
     * @see org.eclipse.cdt.core.IRemoteCommandLauncher#execute(IPath, String[], String[], IPath, IProgressMonitor)
     */
    @Override
    public Process execute(IPath commandPath, String[] args, String[] env, IPath changeToDirectory, IProgressMonitor monitor, PTY pty) {
        try {
            // add platform specific arguments (shell invocation)
            fCommandArgs = constructCommandArray(commandPath.toOSString(), args);
            fShowCommand = true;
            IRemoteServices services = RemoteServices.getRemoteServices(uri);
            IRemoteConnection connection = services.getConnectionManager().getConnection(uri);
            IRemoteFileManager fm = connection.getFileManager();
            IRemoteProcessBuilder builder = connection.getProcessBuilder(Arrays.asList(fCommandArgs));

            if (changeToDirectory != null)
                builder.directory(fm.getResource(changeToDirectory.toString()));

            Map<String,String> envMap = builder.environment();

            for (int i = 0; i < env.length; ++i) {
                String s = env[i];
                String[] tokens = s.split("=", 2); //$NON-NLS-1$
                switch (tokens.length) {
                    case 1:
                        envMap.put(tokens[0], null);
                        break;
                    case 2:
                        envMap.put(tokens[0], tokens[1]);
                        break;
                    default:
                        Activator.log(IStatus.WARNING, Messages.RDTCommandLauncher_malformed_env_var_string + s);
                }
            }

            fProcess = builder.start();
            fErrorMessage = ""; //$NON-NLS-1$
        } catch (IOException e) {
            fErrorMessage = e.getMessage();
            return null;
        }
        return new RemoteProcessAdapter(fProcess);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.IRemoteCommandLauncher#waitAndRead(java.io.OutputStream, java.io.OutputStream, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public int waitAndRead(OutputStream output, OutputStream err, IProgressMonitor monitor) {
        if (fShowCommand) {
            printCommandLine(output);
        }

        if (fProcess == null) {
            return ILLEGAL_COMMAND;
        }

        RemoteProcessClosure closure = new RemoteProcessClosure(fProcess, output, err);
        closure.runNonBlocking();
        while (!monitor.isCanceled() && closure.isAlive()) {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException ie) {
                // ignore
            }
        }

        int state = OK;

        // Operation canceled by the user, terminate abnormally.
        if (monitor.isCanceled()) {
            closure.terminate();
            state = COMMAND_CANCELED;
            fErrorMessage = Activator.getResourceString("CommandLauncher.error.commandCanceled"); //$NON-NLS-1$
        }

        try {
            fProcess.waitFor();
        } catch (InterruptedException e) {
            // ignore
        }
        return state;
    }

    private void printCommandLine(OutputStream os) {
        if (os != null) {
            String cmd = getCommandLine(getCommandArgs());
            try {
                os.write(cmd.getBytes());
                os.flush();
            } catch (IOException e) {
                // ignore;
            }
        }
    }

    private String getCommandLine(String[] commandArgs) {
        StringBuffer buf = new StringBuffer();
        if (fCommandArgs != null) {
            for (String commandArg : commandArgs) {
                buf.append(commandArg);
                buf.append(' ');
            }
            buf.append(lineSeparator);
        }
        return buf.toString();
    }

    @Override
    public Process execute(IPath commandPath, String[] args, String[] env,
            IPath changeToDirectory, IProgressMonitor monitor) {
        return execute(commandPath, args, env, changeToDirectory, monitor, null);
    }

}
