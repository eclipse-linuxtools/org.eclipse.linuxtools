/*******************************************************************************
 * Copyright (c) 2026 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.utils;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.Constants;

/**
 * A {@link ChangeLogTestProject} backed by a local git repository that is
 * shared with the workspace through EGit, so that team-provider aware actions
 * such as "Prepare ChangeLog" work on it.
 */
public class GitTestProject extends ChangeLogTestProject {

    private final Git git;

    /**
     * Create a new project with an empty git repository at its root.
     *
     * @param projectName The name of the project to be created.
     * @throws Exception
     */
    public GitTestProject(String projectName) throws Exception {
        super(projectName);
        File workTree = getTestProject().getLocation().toFile();
        git = Git.init().setDirectory(workTree).call();
    }

    /**
     * Stage everything in the working tree and commit it.
     *
     * @param message The commit message.
     */
    public void commitAll(String message) throws Exception {
        git.add().addFilepattern(".").call();
        git.commit().setMessage(message).setAuthor("Will Probe", "will@example.com")
                .setCommitter("Will Probe", "will@example.com").setSign(Boolean.FALSE).call();
    }

    /**
     * Share the project with EGit, i.e. map it to the git repository.
     */
    public void connect() throws Exception {
        File gitDir = new File(git.getRepository().getWorkTree(), Constants.DOT_GIT);
        new ConnectProviderOperation(getTestProject(), gitDir).execute(new NullProgressMonitor());
    }

    /**
     * Throw away all uncommitted changes and untracked files, so the working
     * tree matches HEAD again, and refresh the project.
     */
    public void resetHard() throws Exception {
        git.reset().setMode(ResetType.HARD).call();
        git.clean().setCleanDirectories(true).call();
        getTestProject().refreshLocal(IResource.DEPTH_INFINITE, null);
    }

    /**
     * Release the repository and delete the project.
     */
    public void dispose() throws Exception {
        git.close();
        getTestProject().delete(true, null);
    }
}
