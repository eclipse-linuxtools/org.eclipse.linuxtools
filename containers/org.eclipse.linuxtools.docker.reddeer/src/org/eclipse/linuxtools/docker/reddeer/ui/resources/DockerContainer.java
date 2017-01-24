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
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.core.condition.ShellWithTextIsAvailable;
import org.jboss.reddeer.swt.api.Menu;
import org.jboss.reddeer.swt.api.TreeItem;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.menu.ContextMenu;

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
		Menu contextMenu = new ContextMenu("Remove");
		if (!contextMenu.isEnabled()) {
			new ContextMenu("Stop").select();
			new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
			item.select();
			contextMenu = new ContextMenu("Remove");
		}
		contextMenu.select();
		new WaitUntil(new ShellWithTextIsAvailable("Confirm Remove Container"), TimePeriod.NORMAL);
		new PushButton("OK").click();
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
	}

}
