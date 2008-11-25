/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.launch;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;

/**
 * Interface for declaring a tool-specific delegate for a Valgrind
 * <code>LaunchConfiguration</code>.
 */
public interface IValgrindLaunchDelegate {

	/**
	 * To be called after Valgrind has been called for a given launch.
	 * This method is responsible for parsing Valgrind's output as needed
	 * by this tool
	 * @param command - The Valgrind instance that just ran
	 * @param config - the configuration to launch
	 * @param launch - the launch object to contribute processes and debug
	 *  targets to
	 * @param monitor - to report progress
	 * @throws CoreException - if this method fails
	 */
	public void launch(ValgrindCommand command, ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Parses attributes of an <code>ILaunchConfiguration</code> into an array
	 * of arguments to be passed to Valgrind
	 * @param command - The Valgrind instance about to run
	 * @param config - the <code>ILaunchConfiguration</code>
	 * @return an array of arguments that can appended to a <code>valgrind</code> command
	 * @throws CoreException - retrieving attributes from config failed
	 */
	public String[] getCommandArray(ValgrindCommand command, ILaunchConfiguration config) throws CoreException;
	
	/**
	 * This method is to be called to reparse the output of recent launches where the
	 * output still remains in the local filesystem. This method should perform the
	 * same output parsing as if a Valgrind instance just ran and display the appropriate
	 * UI.
	 * @param datadir - the directory containing the Valgrind output files to be parsed
	 * @throws CoreException - if parsing the files fails
	 */
	public void reparseOutput(File datadir) throws CoreException;
}
