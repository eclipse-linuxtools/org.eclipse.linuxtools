/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.reddeer.ui;

import org.eclipse.reddeer.jface.handler.TreeViewerHandler;
import org.eclipse.reddeer.swt.api.TreeItem;

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
