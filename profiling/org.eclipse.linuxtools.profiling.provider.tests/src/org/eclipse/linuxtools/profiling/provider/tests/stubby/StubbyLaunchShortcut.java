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
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.provider.tests.stubby;

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public class StubbyLaunchShortcut extends ProfileLaunchShortcut {

    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType("org.eclipse.linuxtools.profiling.stubby.launchConfigurationType"); //$NON-NLS-1$
    }

    @Override
    protected void setDefaultProfileAttributes(ILaunchConfigurationWorkingCopy wc)  {
        // Set this for testing purposes
        wc.setAttribute("foo", "bar"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
