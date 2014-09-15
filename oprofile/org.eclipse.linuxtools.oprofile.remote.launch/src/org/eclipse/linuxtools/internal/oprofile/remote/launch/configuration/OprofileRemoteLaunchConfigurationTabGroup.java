/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
