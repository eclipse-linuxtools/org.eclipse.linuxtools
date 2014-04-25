/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;

public class GcovLaunchConfigurationTabGroup extends
        ProfileLaunchConfigurationTabGroup {

    @Override
    public AbstractLaunchConfigurationTab[] getProfileTabs() {
        return new AbstractLaunchConfigurationTab[0];
    }

}
