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

import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.core.condition.ShellWithTextIsAvailable;
import org.jboss.reddeer.eclipse.exception.EclipseLayerException;
import org.jboss.reddeer.swt.api.Combo;
import org.jboss.reddeer.swt.api.TableItem;
import org.jboss.reddeer.swt.impl.button.CheckBox;
import org.jboss.reddeer.swt.impl.button.FinishButton;
import org.jboss.reddeer.swt.impl.combo.DefaultCombo;
import org.jboss.reddeer.swt.impl.menu.ContextMenu;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.table.DefaultTable;
import org.jboss.reddeer.swt.impl.text.DefaultText;
import org.jboss.reddeer.swt.impl.text.LabeledText;
import org.jboss.reddeer.swt.impl.toolbar.DefaultToolItem;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

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
		new WaitUntil(new ShellWithTextIsAvailable("Build a Docker Image"));
		new LabeledText("Image Name:").setText(name);
		new LabeledText("Directory:").setText(directory);
		new FinishButton().click();
	}

	public void runImage(String imageName) {
		activate();
		selectImage(imageName);
		new ContextMenu("Run...").select();
	}

	public String getImageTags(String imageName) {
		activate();
		TableItem image = getDockerImage(imageName);
		return image.getText(1);
	}

	public void addTagToImage(String imageName, String newTag) {
		selectImage(imageName);
		new ContextMenu("Add Tag").select();
		new DefaultShell("Tag Image");
		new LabeledText("New Tag:").setText(newTag);
		new FinishButton().click();
	}

	public void removeTagFromImage(String imageName, String tagToBeRemoved) {
		selectImage(imageName);
		new ContextMenu("Remove Tag").select();
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

	public void pushImage(String imageName, String registryAccount, boolean forceTagging, boolean keepTaggedImage) {
		selectImage(imageName);
		new ContextMenu("Push...").select();
		Combo combo = new DefaultCombo();
		combo.setSelection(registryAccount);
		new CheckBox("Force tagging image with selected registry").toggle(forceTagging);
		new CheckBox("Keep tagged image upon completion").toggle(keepTaggedImage);
		new FinishButton().click();
	}

	public void searchImage(String searchText) {
		this.activate();
		new DefaultText().setText(searchText);
	}

}