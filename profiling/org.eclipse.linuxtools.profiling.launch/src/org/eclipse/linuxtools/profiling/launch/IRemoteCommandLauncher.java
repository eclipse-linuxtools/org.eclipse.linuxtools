/*******************************************************************************
 * Copyright (c) 2011, 2014 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.launch;

import java.io.OutputStream;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IRemoteCommandLauncher {
	
	int OK = 0;
	int COMMAND_CANCELED = 1;
	int ILLEGAL_COMMAND = -1;
	
	Process execute(IPath commandPath, String[] args, String[] env, IPath changeToDirectory, IProgressMonitor monitor) throws CoreException;
	/**
	 * @since 3.0
	 */
	Process execute(IPath commandPath, String[] args, String[] env, IPath changeToDirectory, IProgressMonitor monitor, PTY pty) throws CoreException;
	int waitAndRead(OutputStream output, OutputStream err, IProgressMonitor monitor);
	String getErrorMessage();
	
}