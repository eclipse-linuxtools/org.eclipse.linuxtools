/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;

/**
 * Project nature for createrepo plugin.
 */
public class CreaterepoProjectNature implements IProjectNature {

    public static final String CREATEREPO_NATURE_ID = "org.eclipse.linuxtools.rpm.createrepo.createreponature"; //$NON-NLS-1$

    private IProject project;

    @Override
    public void configure(){/* not implemented */}

    @Override
    public void deconfigure() {/* not implemented */}

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

}
