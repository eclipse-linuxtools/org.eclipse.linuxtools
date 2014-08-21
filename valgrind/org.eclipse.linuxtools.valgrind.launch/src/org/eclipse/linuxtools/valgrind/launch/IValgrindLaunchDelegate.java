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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.osgi.framework.Version;

/**
 * Interface for declaring a tool-specific delegate for a Valgrind
 * <code>LaunchConfiguration</code>.
 */
public interface IValgrindLaunchDelegate {

    /**
     * To be called after Valgrind has been called for a given launch.
     * This method is responsible for parsing Valgrind's output as needed
     * by this tool
     * @param config - the configuration to launch
     * @param launch - the launch object to contribute processes and debug
     *  targets to
     * @param logDir - directory to store Valgrind log output files
     * @param monitor - to report progress
     * @throws CoreException - if this method fails
     * @since 3.0
     */
    void handleLaunch(ILaunchConfiguration config, ILaunch launch, IPath logDir, IProgressMonitor monitor) throws CoreException;

    /**
     * Called after handleLaunch returns control to the main Valgrind launch
     * delegate, and initializes the Valgrind view. This method is responsible
     * for initializing the tool-specific portion of the Valgrind view with tool-specific
     * output from the launch.
     * @param view - the tool-specific part of the Valgrind view contributed via extension point
     * @param contentDescription - String describing the launch that populated the view
     * @param monitor - to report progress
     * @throws CoreException - if this method fails
     * @since 3.0
     */
    void initializeView(IValgrindToolView view, String contentDescription, IProgressMonitor monitor) throws CoreException;

    /**
     * Parses attributes of an <code>ILaunchConfiguration</code> into an array
     * of arguments to be passed to Valgrind
     * @param config - the <code>ILaunchConfiguration</code>
     * @param ver - the version of Valgrind, or null if version checking should not be performed
     * @param logDir - directory to store Valgrind log output files
     * @return an array of arguments that can appended to a <code>valgrind</code> command
     * @throws CoreException - retrieving attributes from config failed
     * @since 3.0
     */
    String[] getCommandArray(ILaunchConfiguration config, Version ver, IPath logDir) throws CoreException;

}
