/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.callgraph.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.callgraph.core.LaunchConfigurationConstants;

public class ConfigurationOptionsSetter {

	
	/**
	 * Returns a SystemTap command string containing the options specified 
	 * in the configuration.
	 * @param config
	 * @return
	 */
	public static String setOptions(ILaunchConfiguration config) {
		String options = ""; //$NON-NLS-1$
		try {
			if (config.getAttribute(LaunchConfigurationConstants.COMMAND_VERBOSE,
					LaunchConfigurationConstants.DEFAULT_COMMAND_VERBOSE)) {
				options += "-v "; //$NON-NLS-1$
			}


		if (config.getAttribute(LaunchConfigurationConstants.COMMAND_PASS,
				LaunchConfigurationConstants.DEFAULT_COMMAND_PASS) > 0) {
			options += "-p" + config.getAttribute(LaunchConfigurationConstants.COMMAND_PASS, LaunchConfigurationConstants.DEFAULT_COMMAND_PASS) + " "; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_KEEP_TEMPORARY,
				LaunchConfigurationConstants.DEFAULT_COMMAND_KEEP_TEMPORARY)) {
			options += "-k "; //$NON-NLS-1$
		}

		if (config.getAttribute(LaunchConfigurationConstants.COMMAND_GURU,
				LaunchConfigurationConstants.DEFAULT_COMMAND_GURU)) {
			options += "-g "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_PROLOGUE_SEARCH,
				LaunchConfigurationConstants.DEFAULT_COMMAND_PROLOGUE_SEARCH)) {
			options += "-P "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_NO_CODE_ELISION,
				LaunchConfigurationConstants.DEFAULT_COMMAND_NO_CODE_ELISION)) {
			options += "-u "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_DISABLE_WARNINGS,
				LaunchConfigurationConstants.DEFAULT_COMMAND_DISABLE_WARNINGS)) {
			options += "-w "; //$NON-NLS-1$
		}

		if (config.getAttribute(LaunchConfigurationConstants.COMMAND_BULK_MODE,
				LaunchConfigurationConstants.DEFAULT_COMMAND_BULK_MODE)) {
			options += "-b "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_TIMING_INFO,
				LaunchConfigurationConstants.DEFAULT_COMMAND_TIMING_INFO)) {
			options += "-t "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_BUFFER_BYTES,
				LaunchConfigurationConstants.DEFAULT_COMMAND_BUFFER_BYTES) > 0) {
			options += "-s" + config.getAttribute(LaunchConfigurationConstants.COMMAND_BUFFER_BYTES, LaunchConfigurationConstants.DEFAULT_COMMAND_BUFFER_BYTES) + " "; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_TARGET_PID,
				LaunchConfigurationConstants.DEFAULT_COMMAND_TARGET_PID) > 0) {
			options += "-x" + config.getAttribute(LaunchConfigurationConstants.COMMAND_TARGET_PID, LaunchConfigurationConstants.DEFAULT_COMMAND_TARGET_PID) + " "; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_C_DIRECTIVES,
				LaunchConfigurationConstants.DEFAULT_COMMAND_C_DIRECTIVES) != LaunchConfigurationConstants.DEFAULT_COMMAND_C_DIRECTIVES) {
			options += config.getAttribute(
					LaunchConfigurationConstants.COMMAND_C_DIRECTIVES,
					LaunchConfigurationConstants.DEFAULT_COMMAND_C_DIRECTIVES)
					+ " "; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_LEAVE_RUNNING,
				LaunchConfigurationConstants.DEFAULT_COMMAND_LEAVE_RUNNING)) {
			options += "-F "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_SKIP_BADVARS,
				LaunchConfigurationConstants.DEFAULT_COMMAND_SKIP_BADVARS)) {
			options += "--skip-badvars "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_IGNORE_DWARF,
				LaunchConfigurationConstants.DEFAULT_COMMAND_IGNORE_DWARF)) {
			options += "--ignore-dwarf "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_TAPSET_COVERAGE,
				LaunchConfigurationConstants.DEFAULT_COMMAND_TAPSET_COVERAGE)) {
			options += "-q "; //$NON-NLS-1$
		}
		return options;
		
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
