/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
