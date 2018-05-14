/*******************************************************************************
 * Copyright (c) 2012, 2018 IBM Corporation
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Rafael Medeiros Teixeira <rafaelmt@linux.vnet.ibm.com> - initial API and implementation
*******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.remote.launch.configuration;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileEventConfigTab;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileSetupTab;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyProfileLaunchConfigurationTabGroup;

/**
 * @since 1.1
 */
public class OprofileRemoteLaunchConfigurationTabGroup extends RemoteProxyProfileLaunchConfigurationTabGroup {
    @Override
    public AbstractLaunchConfigurationTab[] getProfileTabs() {
        return new AbstractLaunchConfigurationTab[] { new OprofileSetupTab(), new OprofileEventConfigTab() };
    }
}
