/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
