/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
