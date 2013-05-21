/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.launch;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

/**
 * Helper class for launching command line tools. Contains methods for creating a process and
 * one method for fetching from the console.
 *
 *
 * @author chwang
 *
 */
public abstract class ProfileLaunchConfigurationDelegate extends AbstractCLaunchDelegate{


	/**
	 * Executes a command array using pty
	 *
	 * @param commandArray -- Split a command string on the ' ' character
	 * @param env -- Use <code>getEnvironment(ILaunchConfiguration)</code> in the AbstractCLaunchDelegate.
	 * @param wd -- Working directory
	 * @param usePty -- A value of 'true' usually suffices
	 * @return A properly formed process, or null
	 */
	public Process execute(String[] commandArray, String[] env, File wd,
			boolean usePty) {
		Process process = null;
		try {
			if (wd == null) {
				process = ProcessFactory.getFactory().exec(commandArray, env);
			} else {
				if (PTY.isSupported() && usePty) {
					process = ProcessFactory.getFactory().exec(commandArray,
							env, wd, new PTY());
				} else {
					process = ProcessFactory.getFactory().exec(commandArray,
							env, wd);
				}
			}
		} catch (IOException e) {
			return null;
		}
		return process;
	}



	/**
	 * Spawn a new IProcess using the Debug Plugin.
	 *
	 * @param launch
	 * @param systemProcess
	 * @param programName
	 * @return The newly created process.
	 */
	protected IProcess createNewProcess(ILaunch launch, Process systemProcess,
			String programName) {
		return DebugPlugin.newProcess(launch, systemProcess,
				renderProcessLabel(programName));
	}

}
