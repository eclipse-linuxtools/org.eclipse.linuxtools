/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.launch;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;


/**
 * @since 3.2
 */
public class ProfileCategoryEnablementTester extends PropertyTester {

    public ProfileCategoryEnablementTester() {
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        ProfileLaunchShortcut x = null;

        if (args.length == 0)
            return true;

        // See if there is a profile provider for the given category
        x = ProviderFramework.getProfilingProvider((String)args[0]);

        if (x == null)
            return false;
        return true;
    }

}
