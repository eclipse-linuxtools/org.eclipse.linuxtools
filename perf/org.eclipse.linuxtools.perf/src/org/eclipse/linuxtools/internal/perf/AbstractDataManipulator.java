/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteConnectionException;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.swt.widgets.Display;

/**
 * This class represents the general flow of a perf command being
 * set up, executed, and having its data collected.
 */
public abstract class AbstractDataManipulator extends BaseDataManipulator
        implements IPerfData {

    private String text;
    private String title;
    private ILaunch launch;
    private IPath pathWorkDir;
    private List<Thread> threads;
    protected IProject project;

    AbstractDataManipulator (String title, IPath pathWorkDir, IProject project) {
        this.title = title;
        this.pathWorkDir=pathWorkDir;
        threads = new ArrayList<>();
        this.project = project;
    }

    @Override
    public String getPerfData() {
        return text;
    }

    protected IPath getWorkDir(){
        return pathWorkDir;
    }

    @Override
    public String getTitle () {
        return title;
    }

    public void setLaunch (ILaunch launch) {
        this.launch = launch;
    }

    public void performCommand(String[] cmd, int fd) {
        BufferedReader buffData = null;
        BufferedReader buffTemp = null;
        try {

            Process proc = null;
            IFileStore workDirStore = getWorkingDirStore();
            proc = RuntimeProcessFactory.getFactory().exec(cmd, null, workDirStore, project);
            StringBuilder data = new StringBuilder();
            StringBuilder temp = new StringBuilder();

            switch (fd) {
            case 2:
                buffData = proc.errorReader();
                buffTemp = proc.inputReader();
                readStream(buffTemp, temp);
                readStream(buffData, data);
                break;
            case 1:
                // fall through to default case
            default:
                buffData = proc.inputReader();
                buffTemp = proc.errorReader();
                readStream(buffData, data);
                readStream(buffTemp, temp);
                break;
            }
            joinAll();
            text = data.toString();
        } catch (IOException|InterruptedException e) {
            text = ""; //$NON-NLS-1$
        }finally {
            try {
                if (buffData != null) {
                    buffData.close();
                }
                if (buffTemp != null) {
                    buffTemp.close();
                }
            } catch (IOException e) {
                // continue
            }
        }
    }

    public void performCommand(String[] cmd, String file) {
        Process proc = null;
        IRemoteFileProxy fileProxy;
        try {
            try {
                fileProxy = RemoteProxyManager.getInstance().getFileProxy(project);
            } catch (RemoteConnectionException e) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.MsgProxyError, Messages.MsgProxyError);
                return;
            }
            IFileStore workDirStore = getWorkingDirStore();
            proc = RuntimeProcessFactory.getFactory().exec(cmd, null, workDirStore, project, new PTY());
            DebugPlugin.newProcess(launch, proc, ""); //$NON-NLS-1$
            proc.waitFor();

            StringBuilder data = new StringBuilder();
            try (BufferedReader buffData = new BufferedReader(
                    new InputStreamReader(
                            fileProxy.getResource(file).openInputStream(EFS.NONE, null)))) {
                readStream(buffData, data);
                joinAll();
            }
            text = data.toString();
        } catch (IOException|CoreException e) {
            text = ""; //$NON-NLS-1$
        } catch (InterruptedException e){
            text = ""; //$NON-NLS-1$
        }
    }

    /**
     * Write entire contents of BufferedReader into given StringBuilder.
     *
     * @param buff BufferedReader to read from.
     * @param strBuff StringBuilder to write to.
     */
    private void readStream(final BufferedReader buff,
            final StringBuilder strBuff) {
        Thread readThread = new Thread(() -> strBuff.append(getBufferContents(buff)));
        readThread.start();
        threads.add(readThread);
    }

    /**
     * Wait for all working threads to finish.
     *
     * @throws InterruptedException
     */
    private void joinAll() throws InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * A combination of setting up the command to run and executing it.
     * This often calls performCommand(String [] cmd).
     */
    public abstract void parse();

    private IFileStore getWorkingDirStore() {
        IRemoteFileProxy fileProxy;
        try {
            fileProxy = RemoteProxyManager.getInstance().getFileProxy(project);
            if(fileProxy == null) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.MsgProxyError, Messages.MsgProxyError);
            }
        } catch (CoreException e) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.MsgProxyError, Messages.MsgProxyError);
	        return null;
        }
        return fileProxy.getResource(pathWorkDir.toOSString());
    }
}
