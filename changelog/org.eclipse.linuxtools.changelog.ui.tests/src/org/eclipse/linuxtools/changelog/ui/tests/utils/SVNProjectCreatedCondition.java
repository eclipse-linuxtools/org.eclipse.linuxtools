/*******************************************************************************
 * Copyright (c) 2010, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

/**
 * Condition returns true if and only if a project with
 * name <code>projectName</code> exists in the current
 * workspace.
 *
 */
public class SVNProjectCreatedCondition implements ICondition {

    private String projectName;

    public SVNProjectCreatedCondition(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public boolean test() {
        IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = (IProject)wsRoot.findMember(new Path(projectName));
        if (project == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void init(SWTBot bot) {
        // no initialization; don't need bot
    }

    @Override
    public String getFailureMessage() {
        return null;
    }

}
