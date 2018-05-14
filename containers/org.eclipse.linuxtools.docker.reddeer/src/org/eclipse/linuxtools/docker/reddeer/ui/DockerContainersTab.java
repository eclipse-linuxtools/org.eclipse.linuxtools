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
import org.eclipse.reddeer.swt.api.TableItem;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.text.DefaultText;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.eclipse.reddeer.workbench.impl.view.WorkbenchView;

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