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

package org.eclipse.linuxtools.docker.reddeer.ui;

import org.eclipse.reddeer.jface.handler.TreeViewerHandler;
import org.eclipse.reddeer.swt.api.TreeItem;

/**
 * 
 * @author jkopriva@redhat.com, mlabuda@redhat.com
 *
 */

public abstract class AbstractDockerExplorerItem {

	protected TreeViewerHandler treeViewerHandler = TreeViewerHandler.getInstance();
	protected TreeItem item;

	public AbstractDockerExplorerItem(TreeItem treeItem) {
		this.item = treeItem;
	}

	/**
	 * Activates docker explorer.
	 */
	protected void activateDockerExplorerView() {
		new DockerExplorerView().activate();
	}

	/**
	 * Selects abstract docker explorer item.
	 */
	public void select() {
		activateDockerExplorerView();
		item.select();
	}

	/**
	 * Gets tree item encapsulated in abstract docker explorer item.
	 * 
	 * @return encapsulated tree item
	 */
	public TreeItem getTreeItem() {
		return item;
	}

}
