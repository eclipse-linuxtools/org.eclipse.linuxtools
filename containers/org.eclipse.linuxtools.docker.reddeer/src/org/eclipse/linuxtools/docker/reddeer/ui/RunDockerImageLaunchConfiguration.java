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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageSearchPage;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageTagSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.perspective.DockerPerspective;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.debug.ui.launchConfigurations.LaunchConfiguration;
import org.eclipse.reddeer.eclipse.exception.EclipseLayerException;
import org.eclipse.reddeer.swt.api.Table;
import org.eclipse.reddeer.swt.condition.ShellIsActive;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.button.RadioButton;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.combo.LabeledCombo;
import org.eclipse.reddeer.swt.impl.ctab.DefaultCTabItem;
import org.eclipse.reddeer.swt.impl.menu.ShellMenu;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class RunDockerImageLaunchConfiguration extends LaunchConfiguration {

	private static final String MAIN_TAB_LABEL = "Main";
	private static final String VOLUMES_TAB_LABEL = "Volumes";
	private static final String PORTS_TAB_LABEL = "Ports";
	private static final String LINKS_TAB_LABEL = "Links";
	private static final String ENVIRONMENT_TAB_LABEL = "Environment";
	private static final String LABELS_TAB_LABEL = "Labels";
	private static final String RESOURCES_TAB_LABEL = "Resources";
	private static final String DELETE_LAUNCH_CONFIGURATION_LABEL = "Delete selected launch configuration(s)";

	public RunDockerImageLaunchConfiguration() {
		super("Run Docker Image");
	}

	/**
	 * Opens Run configuration dialog
	 */
	public void open() {
		DockerPerspective p = new DockerPerspective();
		p.open();
		new ShellMenu().getItem("Run", "Run Configurations...").select();
		new WaitUntil(new ShellIsAvailable("Run Configurations"));
	}

	/**
	 * Creates new Docker launch configuration
	 */
	public void createNewConfiguration(String configurationName) {
		new DefaultTreeItem("Run Docker Image").select();
		new DefaultToolItem("New launch configuration").click();
		setName(configurationName);
	}

	/**
	 * Select configuration name
	 * 
	 * @param confName
	 *            configuration name
	 */
	public void selectConfiguration(String confName) {
		new DefaultTreeItem("Run Docker Image", confName).select();
	}

	private void addValueInTable(String tabName, String shellName, String newValueName, String newValue) {
		selectTab(tabName);
		new PushButton("Add...").click();
		new WaitUntil(new ShellIsAvailable(shellName));
		new LabeledText("Name:").setText(newValueName);
		new LabeledText("Value:").setText(newValue);
		new OkButton().click();
	}

	private void editValueInTable(String tabName, String shellName, String oldValueName, String newValueName,
			String newValue) {
		selectTab(tabName);
		if (selectItemInTable(oldValueName)) {
			new PushButton("Edit...");
			new WaitWhile(new ShellIsAvailable(shellName));
			new LabeledText("Name:").setText(newValueName);
			new LabeledText("Value:").setText(newValue);
			new OkButton().click();
		} else {
			throw new EclipseLayerException("There is no " + oldValueName + " in table on tab " + tabName);
		}
	}

	private void removeValueInTable(String tabName, String valueName) {
		selectTab(LABELS_TAB_LABEL);
		if (selectItemInTable(valueName)) {
			new PushButton("Remove").click();
		} else {
			throw new EclipseLayerException("There is no " + valueName + " in table on tab " + tabName);
		}
	}

	public void setConfigurationName(String configurationName) {
		selectTab(MAIN_TAB_LABEL);
		new LabeledText("Name:").setText(configurationName);
	}

	// Main tab
	public void selectConnection(String connectionName) {
		selectTab(MAIN_TAB_LABEL);
		new LabeledCombo("Connection:").setSelection(connectionName);
	}

	public void selectImage(String imageName) {
		selectTab(MAIN_TAB_LABEL);
		new LabeledCombo("Image:").setSelection(imageName);
	}

	public void searchImage(String imageName, String tag) {
		selectTab(MAIN_TAB_LABEL);
		new PushButton("Search...");
		ImageSearchPage pageOne = new ImageSearchPage(new DefaultCTabItem(MAIN_TAB_LABEL));
		pageOne.searchImage(imageName);
		pageOne.next();
		new WaitWhile(new ShellIsActive("Progress Information"), TimePeriod.DEFAULT);
		ImageTagSelectionPage pageTwo = new ImageTagSelectionPage(pageOne);
		assertFalse("Search tags are empty!", pageTwo.getTags().isEmpty());
		new WaitWhile(new JobIsRunning(), TimePeriod.DEFAULT);
		assertTrue("Search results do not contains tag:" + tag + "!", pageTwo.tagsContains(tag));
		pageTwo.selectTag(tag);
		pageTwo.finish();
		new DefaultShell("Pull Image");
		new PushButton("Finish").click();
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}

	public void setContainerName(String containerName) {
		selectTab(MAIN_TAB_LABEL);
		new LabeledText("Container Name:").setText(containerName);
	}

	public void setEntrypoint(String entrypoint) {
		selectTab(MAIN_TAB_LABEL);
		new LabeledText("Entrypoint:").setText(entrypoint);
	}

	public void setCommand(String command) {
		selectTab(MAIN_TAB_LABEL);
		new LabeledText("Command:").setText(command);
	}

	public void setKeepSTDINopentoConsoleevenifnotattachedi(boolean checked) {
		selectTab(MAIN_TAB_LABEL);
		new CheckBox("Keep STDIN open to Console even if not attached (-i)").toggle(checked);
	}

	public void setAllocatepseudoTTYfromConsolet(boolean checked) {
		selectTab(MAIN_TAB_LABEL);
		new CheckBox("Allocate pseudo-TTY from Console (-t)").toggle(checked);
	}

	public void setRemoveContainerOnExit(boolean checked) {
		selectTab(MAIN_TAB_LABEL);
		new CheckBox("Automatically remove the container when it exits (--rm)").toggle(checked);
	}

	public void setPrivilegedMode(boolean checked) {
		selectTab(MAIN_TAB_LABEL);
		new CheckBox("Give extended privileges to this container (--privileged)").toggle(checked);
	}

	// volumes tab
	public void removeVolume(String valueName) {
		removeValueInTable(VOLUMES_TAB_LABEL, valueName);
	}

	public void addDataVolumeNoExternalMount(String containerPath) {
		selectTab(VOLUMES_TAB_LABEL);
		new PushButton("Add..").click();
		new LabeledText("Container path:").setText(containerPath);
		new RadioButton("No external mount").click();
		new OkButton().click();
	}

	public void addDataVolumeToHost(String containerPath, String path) {
		addDataVolumeToHost(containerPath, path, false);
	}

	public void addDataVolumeToHost(String containerPath, String path, boolean readOnly) {
		selectTab(VOLUMES_TAB_LABEL);
		new PushButton("Add...").click();
		new LabeledText("Container path:").setText(containerPath);
		new RadioButton("Mount a host directory or host file").click();
		new LabeledText("Path:").setText(path);
		new CheckBox("Read-only access").toggle(readOnly);
		new OkButton().click();
	}

	public void addDataVolumeToContainer(String containerPath, String containerName) {
		selectTab(VOLUMES_TAB_LABEL);
		new PushButton("Add...").click();
		new LabeledText("Container path:").setText(containerPath);
		new RadioButton("Mount a data volume container").click();
		new DefaultCombo("Container").setText(containerName);
		new OkButton().click();
	}

	// ports tab
	public void setPublishAllExposedPortsToRandomPorts(boolean checked) {
		new CheckBox("Publish all exposed ports to random ports on the host interfaces").toggle(checked);
	}

	public void addPort(String containerPort, String hostAddress, String hostPort) {
		selectTab(PORTS_TAB_LABEL);
		setPublishAllExposedPortsToRandomPorts(false);
		new PushButton("Add...").click();
		new WaitUntil(new ShellIsAvailable("Exposing a Container Port"));
		new LabeledText("Container port:").setText(containerPort);
		new LabeledText("Host address:").setText(hostAddress);
		new LabeledText("Host port:").setText(hostPort);
		new OkButton().click();
	}

	public void editPort(String oldContainerPort, String newContainerPort, String newHostAddress, String newHostPort) {
		selectTab(PORTS_TAB_LABEL);
		setPublishAllExposedPortsToRandomPorts(false);
		if (selectItemInTable(oldContainerPort)) {
			new PushButton("Edit...");
			new WaitUntil(new ShellIsAvailable("Exposing a Container Port"));
			new LabeledText("Container port:").setText(newContainerPort);
			new LabeledText("Host address:").setText(newHostAddress);
			new LabeledText("Host port:").setText(newHostPort);
			new OkButton().click();
		} else {
			throw new EclipseLayerException("There is no " + oldContainerPort + " in table on tab " + PORTS_TAB_LABEL);
		}
	}

	public void removePort(String valueName) {
		setPublishAllExposedPortsToRandomPorts(false);
		removeValueInTable(PORTS_TAB_LABEL, valueName);
	}

	// links tab
	public void addLink(String containerName, String alias) {
		selectTab(LINKS_TAB_LABEL);
		new PushButton("Add...").click();
		new WaitWhile(new ShellIsAvailable("Container Linking"));
		new LabeledText("Container:").setText(containerName);
		new LabeledText("Value:").setText(alias);
		new OkButton().click();
	}

	public void editLink(String oldContainer, String newContainer, String newAlias) {
		selectTab(LINKS_TAB_LABEL);
		if (selectItemInTable(oldContainer)) {
			new PushButton("Edit...").click();
			new WaitWhile(new ShellIsAvailable("Container Linking"));
			new LabeledText("Container:").setText(newContainer);
			new LabeledText("Value:").setText(newAlias);
			new OkButton().click();
		} else {
			throw new EclipseLayerException("There is no " + oldContainer + " in table on tab " + LINKS_TAB_LABEL);
		}
	}

	public void removeLink(String linkName) {
		removeValueInTable(LINKS_TAB_LABEL, linkName);
	}

	// environment tab
	public void addEnvironmentVariable(String variableName, String variableValue) {
		addValueInTable(ENVIRONMENT_TAB_LABEL, "Environment Variable", variableName, variableValue);
	}

	public void editEnvironmentVariable(String oldVariable, String newVariable, String newValue) {
		editValueInTable(ENVIRONMENT_TAB_LABEL, "Environment Variable", oldVariable, newVariable, newValue);
	}

	public void removeEnvironmentVariable(String variableName) {
		removeValueInTable(ENVIRONMENT_TAB_LABEL, variableName);
	}

	// labels tab
	public void addLabel(String name, String value) {
		addValueInTable(LABELS_TAB_LABEL, "Label", name, value);
	}

	public void removeLabel(String label) {
		removeValueInTable(LABELS_TAB_LABEL, label);
	}

	public void editLabel(String label, String newName, String newValue) {
		editValueInTable(LABELS_TAB_LABEL, "Label", label, newName, newValue);
	}

	// resources tab
	public void setHigh() {
		setEnableResourceLimitations(true);
		new RadioButton("High").click();
	}

	public void setMedium() {
		setEnableResourceLimitations(true);
		new RadioButton("Medium").click();
	}

	public void setLow() {
		setEnableResourceLimitations(true);
		new RadioButton("Low").click();
	}

	public void setEnableResourceLimitations(boolean check) {
		selectTab(RESOURCES_TAB_LABEL);
		new CheckBox("Enable resource limitations").toggle(check);
	}

	public void setMemoryLimit(String memoryLimit) {
		selectTab(RESOURCES_TAB_LABEL);
		new CheckBox("Enable resource limitations").toggle(true);
		new LabeledText("Memory limit:").setText(memoryLimit);
	}

	public String getMemoryLimit() {
		selectTab(RESOURCES_TAB_LABEL);
		new CheckBox("Enable resource limitations").toggle(true);
		return new LabeledText("Memory limit:").getText();
	}

	/**
	 * Selects tab with a given label.
	 * 
	 * @param label
	 *            Label
	 * @return
	 */
	public DefaultCTabItem selectTab(String label) {
		DefaultCTabItem tab = new DefaultCTabItem(label);
		tab.activate();
		return tab;
	}

	private boolean selectItemInTable(String itemName) {
		Table table = new DefaultTable();
		if (table.containsItem(itemName)) {
			table.select(itemName);
			return true;
		} else {
			return false;
		}
	}

	public void deleteRunConfiguration(String configuratioName) {
		selectConfiguration(configuratioName);
		new DefaultToolItem(DELETE_LAUNCH_CONFIGURATION_LABEL).click();
		new WaitUntil(new ShellIsAvailable("Confirm Launch Configuration Deletion"));
		new PushButton("Yes").click();
		new WaitUntil(new ShellIsAvailable("Run Configurations"));
	}

	public void runConfiguration(String configurationName) {
		selectConfiguration(configurationName);
		new PushButton("Run").click();
	}

	public void close() {
		new PushButton("Close").click();
	}

}
