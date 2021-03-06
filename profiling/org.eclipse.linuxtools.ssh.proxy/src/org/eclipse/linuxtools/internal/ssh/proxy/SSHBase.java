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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHBase {
    private URI uri;
    private JSch jsch;
    private Session session;
    private ChannelSftp channelSftp;
    private static Map<String, String> passwords = new HashMap<>();

    public SSHBase(URI uri) {
        this.uri = uri;
        jsch=new JSch();
    }

    private Session loadSession() throws CoreException {
        if (session == null || !session.isConnected()) {
            try {
                session=jsch.getSession(uri.getUserInfo(), uri.getHost());
            } catch (JSchException e) {
                throw new CoreException(Status.error(Messages.SSHBase_CreateSessionFailed + e.getMessage()));
            }

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");  //$NON-NLS-1$,  //$NON-NLS-2$
            session.setConfig(config);

            String password = passwords.get(uri.getAuthority());
            if (password != null) {
                session.setPassword(password);
                try {
                    session.connect();
                    return session;
                } catch (JSchException e) {
                    //Nothing to do. It will try again in next command
                }
            }

            password = askPassword(uri.getUserInfo(), uri.getHost());
            session.setPassword(password);
            try {
                session.connect();
            } catch (JSchException e) {
                throw new CoreException(Status.error(Messages.SSHBase_CreateSessionFailed + e.getMessage()));
            }
            passwords.put(uri.getAuthority(), password);
        }
        return session;
    }

    private String askPassword(String user, String host) throws CoreException {
        IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (w == null) {
            return ""; //$NON-NLS-1$
        }
        SSHPasswordDialog d = new SSHPasswordDialog(w.getShell(), user, host);
        if (d.open() == Window.OK) {
            return d.getPassword();
        } else {
            throw new CoreException(Status.error(Messages.SSHBase_CreateSessionCancelled));
        }
    }

    protected ChannelSftp getChannelSftp() throws CoreException {
        loadSession();
        if (channelSftp == null || !channelSftp.isConnected()) {
            try {
                channelSftp = (ChannelSftp)session.openChannel("sftp"); //$NON-NLS-1$
                channelSftp.connect();
            } catch (JSchException e) {
				throw new CoreException(Status.error(Messages.SSHBase_CreateSessionFailed + e.getMessage(), e));
            }
        }
        return channelSftp;
    }

    protected ChannelExec createChannelExec() throws CoreException {
        loadSession();
        try {
            return (ChannelExec)session.openChannel("exec"); //$NON-NLS-1$
        } catch (JSchException e) {
            throw new CoreException(Status.error(Messages.SSHBase_CreateSessionFailed + e.getMessage(), e));
        }
    }
}
