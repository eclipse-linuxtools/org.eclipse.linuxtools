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
import org.jboss.reddeer.swt.api.TableItem;
import org.jboss.reddeer.swt.impl.table.DefaultTable;
import org.jboss.reddeer.swt.impl.text.DefaultText;
import org.jboss.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.jboss.reddeer.workbench.impl.view.WorkbenchView;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class DockerContainersTab extends WorkbenchView {

	public DockerContainersTab() {
		super("Docker Containers");
	}

	public TableItem getDockerContainer(String dockerContainerName) {
		this.activate();
		for (TableItem item : getTableItems()) {
			if (item.getText().equals(dockerContainerName)) {
				return item;
			}
		}
		throw new EclipseLayerException("There is no container with name " + dockerContainerName);
	}

	public void refresh() {
		this.activate();
		new DefaultToolItem("Refresh (F5)").click();

	}

	public List<TableItem> getTableItems() {
		return new DefaultTable().getItems();

	}

	public void select(String containerName) {
		getDockerContainer(containerName).select();
	}

	public void searchContainer(String containerName) {
		this.activate();
		new DefaultText().setText(containerName);
	}

}