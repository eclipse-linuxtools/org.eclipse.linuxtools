/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.launch.tests.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;

public class LaunchTestingOptions extends LaunchOptions {
    private IProject project;

    public void setOprofileProject(IProject proj) {
        project = proj;
    }

    @Override
    protected IProject getOprofileProject() {
        return project;
    }
}
