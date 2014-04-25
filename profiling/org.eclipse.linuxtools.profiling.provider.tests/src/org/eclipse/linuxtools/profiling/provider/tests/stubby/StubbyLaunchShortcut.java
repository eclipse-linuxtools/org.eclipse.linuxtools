/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
