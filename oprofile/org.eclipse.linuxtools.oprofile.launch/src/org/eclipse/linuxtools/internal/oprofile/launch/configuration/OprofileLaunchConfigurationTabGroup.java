/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.launch.configuration;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;

public class OprofileLaunchConfigurationTabGroup extends ProfileLaunchConfigurationTabGroup {
    @Override
    public AbstractLaunchConfigurationTab[] getProfileTabs() {
        return new AbstractLaunchConfigurationTab[] { new OprofileSetupTab(), new OprofileEventConfigTab() };
    }
}
