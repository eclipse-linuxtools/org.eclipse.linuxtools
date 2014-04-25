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
package org.eclipse.linuxtools.internal.callgraph.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.callgraph.core.LaunchConfigurationConstants;

public class ConfigurationOptionsSetter {

    /**
     * Returns a SystemTap command string containing the options specified in
     * the configuration.
     *
     * @param config
     * @return
     */
    public static String setOptions(ILaunchConfiguration config) {
        StringBuilder options = new StringBuilder();
        try {

            int verboseLevel = config.getAttribute(
                    LaunchConfigurationConstants.COMMAND_VERBOSE,
                    LaunchConfigurationConstants.DEFAULT_COMMAND_VERBOSE);
            if (verboseLevel > 0) {
                StringBuilder builder = new StringBuilder();
                builder.append("-"); //$NON-NLS-1$
                for (int i = 0; i < verboseLevel; i++)
                    builder.append("v"); //$NON-NLS-1$
                builder.append(" "); //$NON-NLS-1$
                options.append(builder.toString());
            }

            if (config.getAttribute(LaunchConfigurationConstants.COMMAND_PASS,
                    LaunchConfigurationConstants.DEFAULT_COMMAND_PASS) > 0) {
                options.append("-p" + config.getAttribute(LaunchConfigurationConstants.COMMAND_PASS, LaunchConfigurationConstants.DEFAULT_COMMAND_PASS) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (config
                    .getAttribute(
                            LaunchConfigurationConstants.COMMAND_KEEP_TEMPORARY,
                            LaunchConfigurationConstants.DEFAULT_COMMAND_KEEP_TEMPORARY)) {
                options.append("-k "); //$NON-NLS-1$
            }

            if (config.getAttribute(LaunchConfigurationConstants.COMMAND_GURU,
                    LaunchConfigurationConstants.DEFAULT_COMMAND_GURU)) {
                options.append("-g "); //$NON-NLS-1$
            }

            if (config
                    .getAttribute(
                            LaunchConfigurationConstants.COMMAND_PROLOGUE_SEARCH,
                            LaunchConfigurationConstants.DEFAULT_COMMAND_PROLOGUE_SEARCH)) {
                options.append("-P "); //$NON-NLS-1$
            }

            if (config
                    .getAttribute(
                            LaunchConfigurationConstants.COMMAND_NO_CODE_ELISION,
                            LaunchConfigurationConstants.DEFAULT_COMMAND_NO_CODE_ELISION)) {
                options.append("-u "); //$NON-NLS-1$
            }

            if (config
                    .getAttribute(
                            LaunchConfigurationConstants.COMMAND_DISABLE_WARNINGS,
                            LaunchConfigurationConstants.DEFAULT_COMMAND_DISABLE_WARNINGS)) {
                options.append("-w "); //$NON-NLS-1$
            }

            if (config.getAttribute(
                    LaunchConfigurationConstants.COMMAND_BULK_MODE,
                    LaunchConfigurationConstants.DEFAULT_COMMAND_BULK_MODE)) {
                options.append("-b "); //$NON-NLS-1$
            }

            if (config.getAttribute(
                    LaunchConfigurationConstants.COMMAND_TIMING_INFO,
                    LaunchConfigurationConstants.DEFAULT_COMMAND_TIMING_INFO)) {
                options.append("-t "); //$NON-NLS-1$
            }

            if (config.getAttribute(
                    LaunchConfigurationConstants.COMMAND_BUFFER_BYTES,
                    LaunchConfigurationConstants.DEFAULT_COMMAND_BUFFER_BYTES) > 0) {
                options.append("-s" + config.getAttribute(LaunchConfigurationConstants.COMMAND_BUFFER_BYTES, LaunchConfigurationConstants.DEFAULT_COMMAND_BUFFER_BYTES) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (config.getAttribute(
                    LaunchConfigurationConstants.COMMAND_TARGET_PID,
                    LaunchConfigurationConstants.DEFAULT_COMMAND_TARGET_PID) > 0) {
                options.append("-x" + config.getAttribute(LaunchConfigurationConstants.COMMAND_TARGET_PID, LaunchConfigurationConstants.DEFAULT_COMMAND_TARGET_PID) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (config.getAttribute(
                    LaunchConfigurationConstants.COMMAND_C_DIRECTIVES,
                    LaunchConfigurationConstants.DEFAULT_COMMAND_C_DIRECTIVES) != LaunchConfigurationConstants.DEFAULT_COMMAND_C_DIRECTIVES) {
                options.append(config
                        .getAttribute(
                                LaunchConfigurationConstants.COMMAND_C_DIRECTIVES,
                                LaunchConfigurationConstants.DEFAULT_COMMAND_C_DIRECTIVES)
                        + " "); //$NON-NLS-1$
            }

            if (config.getAttribute(
                    LaunchConfigurationConstants.COMMAND_LEAVE_RUNNING,
                    LaunchConfigurationConstants.DEFAULT_COMMAND_LEAVE_RUNNING)) {
                options.append("-F "); //$NON-NLS-1$
            }

            if (config.getAttribute(
                    LaunchConfigurationConstants.COMMAND_SKIP_BADVARS,
                    LaunchConfigurationConstants.DEFAULT_COMMAND_SKIP_BADVARS)) {
                options.append("--skip-badvars "); //$NON-NLS-1$
            }

            if (config.getAttribute(
                    LaunchConfigurationConstants.COMMAND_IGNORE_DWARF,
                    LaunchConfigurationConstants.DEFAULT_COMMAND_IGNORE_DWARF)) {
                options.append("--ignore-dwarf "); //$NON-NLS-1$
            }

            if (config
                    .getAttribute(
                            LaunchConfigurationConstants.COMMAND_TAPSET_COVERAGE,
                            LaunchConfigurationConstants.DEFAULT_COMMAND_TAPSET_COVERAGE)) {
                options.append("-q "); //$NON-NLS-1$
            }
            return options.toString();

        } catch (CoreException e) {
            e.printStackTrace();
        }

        return null;
    }
}
