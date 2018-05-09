/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.helgrind;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public class HelgrindLaunchShortcut extends ProfileLaunchShortcut {


    @Override
    protected void setDefaultProfileAttributes(
            ILaunchConfigurationWorkingCopy wc) throws CoreException {
        ValgrindOptionsTab tab = new ValgrindOptionsTab();
        tab.setDefaults(wc);
        ILaunchConfigurationTab defaultTab = ValgrindLaunchPlugin.getDefault().getToolPage(HelgrindPlugin.TOOL_ID);
        defaultTab.setDefaults(wc);
    }

    /**
     * Method getValgrindLaunchConfigType.
     * @return ILaunchConfigurationType
     */
    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(ValgrindLaunchPlugin.LAUNCH_ID);
    }

}
