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
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.callgraph.launch;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;

/**
 * Expansion of the SystemTapLCTG, which was a stripped down version of the ProfileLaunchConfigurationTabGroup
 */
public class SystemTapLaunchConfigurationTabGroup extends ProfileLaunchConfigurationTabGroup {

    @Override
    public AbstractLaunchConfigurationTab[] getProfileTabs() {
        return new AbstractLaunchConfigurationTab[] {
                new SystemTapOptionsTab()
            };
    }

    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<>();

        tabs.addAll(Arrays.asList(getProfileTabs()));
        setTabs(tabs.toArray(new AbstractLaunchConfigurationTab[tabs.size()]));
    }
}
