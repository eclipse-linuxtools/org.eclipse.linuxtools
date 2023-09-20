/*******************************************************************************
 * Copyright (c) 2017,2020 Red Hat, Inc.
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
package org.eclipse.linuxtools.docker.reddeer.ui.resources;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.linuxtools.docker.reddeer.ui.AbstractDockerExplorerItem;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.jface.exception.JFaceLayerException;
import org.eclipse.reddeer.swt.api.Combo;
import org.eclipse.reddeer.swt.api.Shell;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.ControlIsEnabled;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

public class DockerConnection extends AbstractDockerExplorerItem {

	// image name label in dialog
	private static final String IMAGE_NAME_LABEL_DIALOG = "Image name:";

	public DockerConnection(TreeItem treeItem) {
		super(treeItem);
	}

	public void enableConnection() {
		select();
//		new DefaultToolItem("Enable Connection").click();
		new ContextMenu().getItem("Refresh").select();
		new WaitWhile(new JobIsRunning());
	}

	/**
	 * Gets docker image with specified image name and latest tag.
	 * 
	 * @param imageName
	 *            docker image name
	 * @return tree item of docker image or null if does not exist
	 */
	public DockerImage getImage(String imageName) {
		return getImage(imageName, "latest");
	}

	/**
	 * Gets docker image with specified image name and specified tag.
	 * 
	 * @param imageName
	 *            docker image name
	 * @param tag
	 *            image tag
	 * @return tree item of docker image or null if does not exist
	 */
	public DockerImage getImage(String imageName, String tag) {
		try {
			List<TreeItem> images = treeViewerHandler.getTreeItems(item, "Images", imageName + ":");

			for (TreeItem item : images) {
				if (hasTag(tag, item)) {
					return new DockerImage(item);
				}
			}
		} catch (JFaceLayerException ex) {
		}
		return null;
	}

	private boolean hasTag(String tag, TreeItem item) {
		String[] styledTexts = treeViewerHandler.getStyledTexts(item);
		if (styledTexts == null || styledTexts.length == 0) {
			return false;
		}
		return StringUtils.contains(styledTexts[0].trim(), tag);
	}

	/**
	 * Refresh images.
	 */
	public void refreshImages() {
		treeViewerHandler.getTreeItem(item, "Images").select();
		new ContextMenu().getItem("Refresh").select();
		new WaitWhile(new JobIsRunning());
	}

	/**
	 * Refresh containers.
	 */
	public void refreshContainers() {
		treeViewerHandler.getTreeItem(item, "Containers").select();
		new ContextMenu().getItem("Refresh").select();
		new WaitWhile(new JobIsRunning());
	}

	/**
	 * Refresh images and containers.
	 */
	public void refresh() {
		refreshImages();
		refreshContainers();
	}

	public void pullImage(String imageName, String imageTag, String dockerRegister) {
		if (getImage(imageName, imageTag) == null) {
			refreshImages();

			treeViewerHandler.getTreeItem(item, "Images").select();
			new ContextMenu().getItem("Pull...").select();

			new WaitUntil(new ShellIsAvailable("Pull Image"), TimePeriod.DEFAULT);
			Shell pullShell = new DefaultShell("Pull Image");

			// select register
			if (dockerRegister != null) {
				Combo combo = new DefaultCombo();
				combo.setSelection(dockerRegister);
			}

			new LabeledText(IMAGE_NAME_LABEL_DIALOG).setFocus();
			new LabeledText(IMAGE_NAME_LABEL_DIALOG).setText(imageTag == null ? imageName : imageName + ":" + imageTag);

			new WaitUntil(new ControlIsEnabled(new FinishButton()));
			new FinishButton(pullShell).click();

			new WaitWhile(new ShellIsAvailable(pullShell));
			new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		}
	}

	public void openImageSearchDialog(String imageName, String imageTag, String dockerRegister) {
		refreshImages();

		treeViewerHandler.getTreeItem(item, "Images").select();
		new ContextMenu().getItem("Pull...").select();

		new WaitUntil(new ShellIsAvailable("Pull Image"), TimePeriod.DEFAULT);

		// select register
		if (dockerRegister != null) {
			Combo combo = new DefaultCombo();
			combo.setSelection(dockerRegister);
		}

		new LabeledText(IMAGE_NAME_LABEL_DIALOG).setFocus();
		new LabeledText(IMAGE_NAME_LABEL_DIALOG).setText(imageTag == null ? imageName : imageName + ":" + imageTag);
		new PushButton("Search...").click();
	}

	public boolean imageIsDeployed(String imageName) {
		return deployedImagesCount(imageName) >= 1;
	}

	public int deployedImagesCount(String imageName) {
		int count = 0;
		select();
		List<String> imagesNames = getImagesNames(true);
		for (String imageNameFromList : imagesNames) {
			if (imageNameFromList.contains(imageName)) {
				count++;
			}
		}
		return count;
	}

	public boolean containerIsDeployed(String containerName) {
		return getContainer(containerName) != null;
	}

	public void removeConnection() {
		select();
		// Use context menu instead of Tab toolbar button as this is not working with 4.17 I-build
//		new DefaultToolItem("Remove Connection").click();
		new ContextMenu().getItem("Remove").select();
		new WaitWhile(new JobIsRunning());
	}

	public DockerContainer getContainer(String containerName) {
		try {
			List<TreeItem> containers = treeViewerHandler.getTreeItems(item, "Containers", containerName);
			return new DockerContainer(containers.get(0));
		} catch (JFaceLayerException ex) {
			// Container does not exist.
			return null;
		}
	}

	/**
	 * Returns all the names of all image, without the tag.
	 * 
	 * @return
	 */
	public List<String> getImagesNames() {
		return getImagesNames(false);
	}

	public List<String> getImagesNames(boolean withTag) {
		select();
		List<String> imagesNames = new ArrayList<String>();
		List<TreeItem> images = treeViewerHandler.getTreeItem(item, "Images").getItems();
		for (TreeItem item : images) {
			String imageName = treeViewerHandler.getNonStyledText(item);
			if (withTag) {
				String imageTag = getImageTag(item);
				imagesNames.add(imageName + imageTag);
			} else {
				imagesNames.add(imageName);
			}

		}
		return imagesNames;
	}

	private String getImageTag(TreeItem item) {
		String[] styledTexts = treeViewerHandler.getStyledTexts(item);
		if (styledTexts == null || styledTexts.length == 0) {
			return null;
		}
		return styledTexts[0];
	}

	public List<String> getContainersNames() {
		select();
		List<String> containersNames = new ArrayList<String>();
		List<TreeItem> containers = treeViewerHandler.getTreeItem(item, "Containers").getItems();
		for (TreeItem item : containers) {
			containersNames.add(treeViewerHandler.getNonStyledText(item));
		}
		return containersNames;
	}

	public String getName() {
		return treeViewerHandler.getNonStyledText(this.item);
	}

}
