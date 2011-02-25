/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.utils;

import static org.junit.Assert.assertNotNull;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.changelog.ui.tests.swtbot.PrepareChangelogSWTBotTest;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * Subversion project abstraction for SWTBot tests. See
 * {@link PrepareChangelogSWTBotTest#setUp()} for a usage example.
 *
 */
public class SVNProject {

	private SWTWorkbenchBot bot;
	private String repoURL;
	private String projectName;
	private IProject project; // available after checkout
	
	public SVNProject(SWTWorkbenchBot bot) {
		this.bot = bot;
	}
	
	/**
	 * @return the repoURL
	 */
	public String getRepoURL() {
		return repoURL;
	}

	/**
	 * @param repoURL the repoURL to set
	 */
	public SVNProject setRepoURL(String repoURL) {
		this.repoURL = repoURL;
		return this;
	}

	/**
	 * @return the projectName
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * @param projectName the projectName to set
	 */
	public SVNProject setProjectName(String projectName) {
		this.projectName = projectName;
		return this;
	}

	/**
	 * @return the project
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @param project the project to set
	 */
	public SVNProject setProject(IProject project) {
		this.project = project;
		return this;
	}

	/**
	 * Use File => Import => SVN to create a svn-backed project.
	 */
	public IProject checkoutProject() throws IllegalStateException {
		if (repoURL == null || projectName == null) {
			// need to have url and project set
			throw new IllegalStateException();
		}
		bot.menu("File").menu("Import...").click();
		 
		SWTBotShell shell = bot.shell("Import");
		shell.activate();
		bot.tree().expandNode("SVN").select("Checkout Projects from SVN");
		bot.button("Next >").click();
		
		// create new repo
		shell = bot.shell("Checkout from SVN");
		shell.activate();
		bot.button("Next >").click();
		
		shell = bot.shell("Checkout from SVN");
		shell.activate();
		// Enter url
		bot.comboBoxWithLabelInGroup("Url:", "Location").setText(repoURL);
		bot.button("Next >").click();
		
		// the next few operation can take quite a while, adjust
		// timout accordingly.
		long oldTimeout = SWTBotPreferences.TIMEOUT;
		SWTBotPreferences.TIMEOUT = 3 * 5000;
		
		bot.waitUntil(Conditions.shellIsActive("Progress Information"));
		shell = bot.shell("Progress Information");
		bot.waitUntil(Conditions.shellCloses(shell));		
		bot.waitUntil(Conditions.shellIsActive("Checkout from SVN"));
		shell = bot.shell("Checkout from SVN");
		bot.waitUntil(new TreeItemAppearsCondition(repoURL, projectName));
		SWTBotTreeItem projectTree = bot.tree().expandNode(repoURL);
		projectTree.expandNode(projectName).select();
		bot.button("Finish").click();
		// Wait for import operation to finish
		bot.waitUntil(Conditions.shellCloses(shell));
		bot.waitUntil(Conditions.shellIsActive("SVN Checkout"));
		SWTBotShell svnCheckoutPopup = bot.shell("SVN Checkout");
		bot.waitUntil(Conditions.shellCloses(svnCheckoutPopup));
		// need a little delay
		bot.waitUntil(new SVNProjectCreatedCondition(projectName));
		
		// Set timout back what it was.
		SWTBotPreferences.TIMEOUT = oldTimeout;
		
		// A quick sanity check
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject changelogTestsProject = (IProject)wsRoot.findMember(new Path(projectName));
		assertNotNull(changelogTestsProject);
		try {
			changelogTestsProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IResource manifest = changelogTestsProject.findMember(new Path("/META-INF/MANIFEST.MF"));
		assertNotNull(manifest);
		return changelogTestsProject;
	}
	
	/**
	 *  Discard the automatically created SVN repo URL from the list.
	 */
	public void discardRepositoryLocation() throws Exception {
		if (repoURL == null) { // need to have repoURL set
			throw new IllegalStateException();
		}
		new SVNReporsitoriesView(bot).open().discardRepository(repoURL);
	}
}
