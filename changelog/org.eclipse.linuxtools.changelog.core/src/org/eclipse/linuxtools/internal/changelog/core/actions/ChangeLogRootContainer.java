/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
