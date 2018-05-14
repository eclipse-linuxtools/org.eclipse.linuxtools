/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *    Severin Gehwolf <sgehwolf@redhat.com> - moved to separate class
 *******************************************************************************/

package org.eclipse.linuxtools.oprofile.launch.tests.utils;

import java.net.URI;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileCounter;
import org.eclipse.linuxtools.internal.oprofile.launch.launching.OprofileLaunchConfigurationDelegate;

/**
 * Helper delegate class
 */
public final class TestingOprofileLaunchConfigurationDelegate extends
        OprofileLaunchConfigurationDelegate {
    public boolean eventsIsNull;
    public OprofileDaemonOptions _options;

    @Override
    protected boolean oprofileStatus() {
        return false;
    }

    @Override
    protected void postExec(LaunchOptions options,
            OprofileDaemonEvent[] daemonEvents, Process process) {
        super.postExec(options, daemonEvents, process);

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected OprofileCounter[] oprofileCounters(ILaunchConfiguration config) {
        return new OprofileCounter[0];

    }

    @Override
    protected URI oprofileWorkingDirURI(ILaunchConfiguration config){
        return oprofileProject().getLocationURI();
    }
}
