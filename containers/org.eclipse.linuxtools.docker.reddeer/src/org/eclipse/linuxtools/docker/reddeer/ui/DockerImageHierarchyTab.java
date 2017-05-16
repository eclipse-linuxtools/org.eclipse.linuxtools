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

import java.util.List;

import org.jboss.reddeer.eclipse.exception.EclipseLayerException;
import org.jboss.reddeer.swt.api.TreeItem;
import org.jboss.reddeer.swt.impl.tree.DefaultTree;

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
