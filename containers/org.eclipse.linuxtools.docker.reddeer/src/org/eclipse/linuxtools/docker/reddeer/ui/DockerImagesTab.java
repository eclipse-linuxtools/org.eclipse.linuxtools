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

import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.eclipse.exception.EclipseLayerException;
import org.eclipse.reddeer.swt.api.Combo;
import org.eclipse.reddeer.swt.api.TableItem;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.text.DefaultText;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.eclipse.reddeer.workbench.impl.view.WorkbenchView;

public class DockerImagesTab extends WorkbenchView {

	public DockerImagesTab() {
		super("Docker Images");
	}

	public TableItem getDockerImage(String dockerImageName) {
		activate();
		for (TableItem item : getTableItems()) {
			if (item.getText(1).contains(dockerImageName)) {
				return item;
			}
		}
		throw new EclipseLayerException("There is no Docker image with name " + dockerImageName);
	}

	public void refresh() {
		activate();
		new DefaultToolItem("Refresh (F5)").click();

	}

	public List<TableItem> getTableItems() {
		activate();
		return new DefaultTable().getItems();

	}

	public void buildImage(String name, String directory) {
		activate();
		new DefaultToolItem("Build Image").click();
		new WaitUntil(new ShellIsAvailable("Build a Docker Image"));
		new LabeledText("Image Name:").setText(name);
		new LabeledText("Directory:").setText(directory);
		new FinishButton().click();
	}

	public void runImage(String imageName) {
		activate();
		selectImage(imageName);
		new ContextMenu().getItem("Run...").select();
	}

	public String getImageTags(String imageName) {
		activate();
		TableItem image = getDockerImage(imageName);
		return image.getText(1);
	}

	public void addTagToImage(String imageName, String newTag) {
		selectImage(imageName);
		new ContextMenu().getItem("Add Tag").select();
		new DefaultShell("Tag Image");
		new LabeledText("New Tag:").setText(newTag);
		new FinishButton().click();
	}

	public void removeTagFromImage(String imageName, String tagToBeRemoved) {
		selectImage(imageName);
		new ContextMenu().getItem("Remove Tag").select();
		String fullTag = "";
		Combo combo = new DefaultCombo();
		List<String> comboItems = combo.getItems();
		for (String item : comboItems) {
			if (item.contains(tagToBeRemoved)) {
				fullTag = item;
				break;
			}
		}
		combo.setSelection(fullTag);
		new FinishButton().click();
	}

	public void selectImage(String imageName) {
		activate();
		TableItem image = getDockerImage(imageName);
		image.select();
	}

	public void searchImage(String searchText) {
		this.activate();
		new DefaultText().setText(searchText);
	}

}