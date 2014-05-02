/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
