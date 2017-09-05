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
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class DockerContainer extends AbstractDockerExplorerItem {

	public DockerContainer(TreeItem treeItem) {
		super(treeItem);
	}

	public void remove() {
		select();
		boolean removeEnabled = new ContextMenu().getItem("Remove").isEnabled();
		if (!removeEnabled) {
			new ContextMenu().getItem("Stop").select();
			new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
			item.select();
		}
		new ContextMenu().getItem("Remove").select();
		new WaitUntil(new ShellIsAvailable("Confirm Remove Container"), TimePeriod.DEFAULT);
		new PushButton("OK").click();
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
	}

}
