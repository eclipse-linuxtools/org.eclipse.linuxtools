/*******************************************************************************
 * Copyright (c) 2008, 2018 Red Hat, Inc.
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
 *        org.eclipse.linuxtools.oprofile.launch.launching.OprofileLaunchShortcut
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.launch;

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public class PerfLaunchShortcut extends ProfileLaunchShortcut {

    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(PerfPlugin.LAUNCHCONF_ID);
    }

    @Override
    protected void setDefaultProfileAttributes(
            ILaunchConfigurationWorkingCopy wc) {
        //These settings make it appear smoother.
        wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
        wc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
    }

}
