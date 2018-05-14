/*******************************************************************************
 * Copyright (c) 2008, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *    Thavidu Ranatunga (IBM) - derived from
 *        org.eclipse.linuxtools.oprofile.launch.configuration.OprofileLaunchConfigurationTabGroup
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.remote.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.perf.launch.PerfEventsTab;
import org.eclipse.linuxtools.internal.perf.launch.PerfOptionsTab;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyProfileLaunchConfigurationTabGroup;

public class PerfLaunchConfigurationTabGroup extends RemoteProxyProfileLaunchConfigurationTabGroup  {
    @Override
    public AbstractLaunchConfigurationTab[] getProfileTabs() {
        return new AbstractLaunchConfigurationTab[] { new PerfOptionsTab(), new PerfEventsTab() };
    }
}
