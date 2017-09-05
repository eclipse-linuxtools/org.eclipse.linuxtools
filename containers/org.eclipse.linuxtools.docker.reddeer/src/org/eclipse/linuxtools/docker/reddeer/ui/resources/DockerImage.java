/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.docker.reddeer.ui.resources;

import org.eclipse.linuxtools.docker.reddeer.ui.AbstractDockerExplorerItem;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.swt.api.Combo;
import org.eclipse.reddeer.swt.api.Shell;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

/**
 * 
 * @author jkopriva@redhat.com, mlabuda@redhat.com
 *
 */

public class DockerImage extends AbstractDockerExplorerItem {

	public DockerImage(TreeItem treeItem) {
		super(treeItem);
	}

	/**
	 * * Removes docker image.
	 */
	public void remove() {
		select();
		new ContextMenu().getItem("Remove").select();

		Shell confirm = new DefaultShell("Confirm Remove Image");
		new OkButton().click();

		new WaitWhile(new ShellIsAvailable(confirm));
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
	}

	public void openImageHierarchy() {
		select();
		new ContextMenu().getItem("Open Image Hierarchy").select();
	}

	public void pushImage(String registryAccount, boolean forceTagging, boolean keepTaggedImage) {
		select();
		new ContextMenu().getItem("Push...").select();
		new DefaultShell("Push Image");
		Combo combo = new DefaultCombo();
		combo.setSelection(registryAccount);
		new CheckBox("Force tagging image with selected registry").toggle(forceTagging);
		new CheckBox("Keep tagged image upon completion").toggle(keepTaggedImage);
		new FinishButton().click();
	}

	public void addTagToImage(String newTag) {
		select();
		new ContextMenu().getItem("Add Tag").select();
		new DefaultShell("Tag Image");
		new LabeledText("New Tag:").setText(newTag);
		new FinishButton().click();
	}

	public void run() {
		select();
		new ContextMenu().getItem("Run...").select();
	}

}