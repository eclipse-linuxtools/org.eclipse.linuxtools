/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.ssh.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

import com.jcraft.jsch.ChannelExec;

public class SSHProcess extends Process {

    private ChannelExec channel;
    private static final long DELAY = 100;

    public SSHProcess(ChannelExec channel) {
        this.channel = channel;
    }

    @Override
    public OutputStream getOutputStream() {
        try {
            return channel.getOutputStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public InputStream getInputStream() {
        try {
            return channel.getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public InputStream getErrorStream() {
        try {
            return channel.getErrStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public int waitFor() throws InterruptedException {
        while (!channel.isClosed()) {
            Thread.sleep(DELAY);
        }
        return channel.getExitStatus();
    }

    @Override
    public int exitValue() {
        if (!channel.isClosed()) {
            throw new IllegalThreadStateException();
        }
        return channel.getExitStatus();
    }

    @Override
    public void destroy() {
        channel.disconnect();
    }

    protected int waitAndRead(OutputStream output, OutputStream err, IProgressMonitor monitor) {
        channel.setOutputStream(output);
        channel.setErrStream(err);
        while (!channel.isClosed() && !monitor.isCanceled()) {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
            }
        }

        channel.setOutputStream(null);
        channel.setErrStream(null);
        return channel.getExitStatus();
    }
}
