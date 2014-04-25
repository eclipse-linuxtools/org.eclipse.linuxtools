/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;

public class LocalLauncher implements IRemoteCommandLauncher {

    private CommandLauncher launcher;

    public LocalLauncher() {
        launcher = new CommandLauncher();
    }

    @Override
    public Process execute(IPath commandPath, String[] args, String[] env,
            IPath changeToDirectory, IProgressMonitor monitor)
            throws CoreException {
        launcher.showCommand(true);
        return launcher.execute(commandPath, args, env, changeToDirectory, monitor);
    }

    @Override
    public Process execute(IPath commandPath, String[] args, String[] env,
            IPath changeToDirectory, IProgressMonitor monitor, PTY pty) {
        String [] mergedCommand = new String [args.length + 1];
        System.arraycopy(args, 0, mergedCommand, 1, args.length);
        mergedCommand[0] = commandPath.toOSString();
        Process p = null;
        try {
            p = ProcessFactory.getFactory().exec(mergedCommand, env, changeToDirectory.toFile(), pty);
        } catch (IOException e) {
            CCorePlugin.log(e);
        }
        return p;
    }

    @Override
    public int waitAndRead(OutputStream output, OutputStream err,
            IProgressMonitor monitor) {
        return launcher.waitAndRead(output, err, monitor);
    }

    @Override
    public String getErrorMessage() {
        return launcher.getErrorMessage();
    }

}
