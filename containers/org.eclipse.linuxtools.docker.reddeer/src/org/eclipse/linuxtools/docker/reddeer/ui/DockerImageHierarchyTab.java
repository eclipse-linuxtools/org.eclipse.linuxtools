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

import java.util.List;

import org.eclipse.reddeer.eclipse.exception.EclipseLayerException;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.impl.tree.DefaultTree;
import org.eclipse.reddeer.workbench.impl.view.WorkbenchView;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class DockerImageHierarchyTab extends WorkbenchView {

	public DockerImageHierarchyTab() {
		super("Docker Image Hierarchy");
	}

	public TreeItem getDockerImage(String dockerImageName) {
		activate();
		for (TreeItem item : getTreeItems()) {
			if (item.getText().contains(dockerImageName)) {
				return item;
			}
		}
		throw new EclipseLayerException("There is no Docker image with name " + dockerImageName);
	}

	public List<TreeItem> getTreeItems() {
		activate();
		return new DefaultTree().getItems();
	}

}
