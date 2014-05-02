/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;

/**
 * Interface for oprofile core to utilize opcontrol program. Platform plugins should define/register
 * an OpcontrolProvider for the core to use.
 */
public interface IOpcontrolProvider {

    /**
     * Initialize the Oprofile kernel module
     * @throws OpcontrolException
     */
    void initModule() throws OpcontrolException;

    /**
     * De-initialize (unload) the kernel module
     * @throws OpcontrolException
     */
    void deinitModule() throws OpcontrolException;

    /**
     * Clears out data from the current session
     * @throws OpcontrolException
     */
    void reset() throws OpcontrolException;

    /**
     * Flush the current oprofiled sample buffers to disk
     * @throws OpcontrolException
     */
    void dumpSamples() throws OpcontrolException;

    /**
     * Setup oprofiled collection parameters
     * @param options a list of command-line arguments for opcontrol
     * @param events list of events to collect
     * @throws OpcontrolException
     */
    void setupDaemon(OprofileDaemonOptions options, OprofileDaemonEvent[] events) throws OpcontrolException;

    /**
     * Start data collection by oprofiled (will start oprofiled if necessary)
     * @throws OpcontrolException
     */
    void startCollection() throws OpcontrolException;

    /**
     * Stop data collection (does NOT stop daemon)
     * @throws OpcontrolException
     */
    void stopCollection() throws OpcontrolException;

    /**
     * Stop data collection and shutdown oprofiled
     * @throws OpcontrolException
     */
    void shutdownDaemon() throws OpcontrolException;

    /**
     * Start oprofiled (does NOT start data collection)
     * @throws OpcontrolException
     */
    void startDaemon() throws OpcontrolException;

    /**
     * Save the current session
     * @throws OpcontrolException
     */
    void saveSession(String name) throws OpcontrolException;

    /**
     * Delete the session with the specified name for the specified event
     * @param sessionName The name of the session to delete
     * @param sessionEvent The name of the event containing the session
     * @throws OpcontrolException
     */
    void deleteSession (String sessionName, String sessionEvent) throws OpcontrolException;

    /**
     * Check if the user has permission to run opcontrol
     * @param project The project to be run
     * @return true if the user has sudo permission to run opcontrol, otherwise false
     * @throws OpcontrolException
     * @since 2.0
     */
    boolean hasPermissions(IProject project) throws OpcontrolException;

    /**
     * Check status and return true if any status was returned
     * @return true if any status was returned, otherwise false
     * @throws OpcontrolException
     */
    boolean status() throws OpcontrolException;
}
