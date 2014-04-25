/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class ChangeLogRootContainer {

    private IProject proj;

    public ChangeLogRootContainer(IProject project) {
        proj = project;
    }

    public IResource[] members() {
        return new IResource[]{proj};
    }
}
