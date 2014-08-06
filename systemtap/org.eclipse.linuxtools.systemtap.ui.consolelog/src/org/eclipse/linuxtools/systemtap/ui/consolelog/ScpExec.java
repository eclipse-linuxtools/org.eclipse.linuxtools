/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.structures.runnable.Command;
import org.eclipse.linuxtools.systemtap.structures.runnable.StreamGobbler;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.RemoteScriptOptions;
import org.eclipse.linuxtools.tools.launch.core.factory.LinuxtoolsProcessFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

public class ScpExec extends Command {

    public static final int INPUT_STREAM = 1;
    private static final int WAIT_DELAY = 1000;

    private Channel channel;
    private RemoteScriptOptions remoteOptions;

    /**
     * @since 3.0
     */
    public ScpExec(String[] cmd, RemoteScriptOptions remoteOptions, String[] envVars) {
        super(cmd, envVars, null);
        this.remoteOptions = remoteOptions;
    }

    @Override
    protected IStatus init() {
        try {
            channel = LinuxtoolsProcessFactory.execRemote(
                    cmd, System.out, System.err,
                    remoteOptions.userName, remoteOptions.hostName, remoteOptions.password, remoteOptions.port,
                    envVars);

            errorGobbler = new StreamGobbler(channel.getExtInputStream());
            inputGobbler = new StreamGobbler(channel.getInputStream());

            transferListeners();
            return Status.OK_STATUS;
        } catch (final JSchException|IOException e) {
            final IStatus status = new Status(IStatus.ERROR, ConsoleLogPlugin.PLUGIN_ID, Messages.ScpExec_FileTransferFailed, e);
            return status;
        }
    }

    @Override
    public void run() {
        try {
            errorGobbler.start();
            inputGobbler.start();

            synchronized (this) {
                while (!channel.isClosed()) {
                    wait(WAIT_DELAY);
                }
                cleanUpAfterStop();
            }
        } catch (InterruptedException e) {
            // This thread was interrupted while waiting for
            // the process to exit. Destroy the process just
            // to make sure it exits.
            stop();
        }
    }

    @Override
    public synchronized void stop() {
        if (!stopped) {
            try {
                if (channel != null) {
                    channel.getSession().disconnect();
                }
                cleanUpAfterStop();
            } catch (JSchException e) {
                ExceptionErrorDialog.openError(Messages.ScpExec_Error,
                        e.getMessage(), e);
            }
        }
    }
}
