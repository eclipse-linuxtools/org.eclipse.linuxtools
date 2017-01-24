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
package org.eclipse.linuxtools.docker.integration.tests.image;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.linuxtools.docker.integration.tests.AbstractDockerBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.condition.ContainerIsDeployedCondition;
import org.eclipse.linuxtools.docker.reddeer.preferences.RegistryAccountsPreferencePage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.linuxtools.docker.reddeer.ui.resources.DockerImage;
import org.jboss.reddeer.common.exception.RedDeerException;
import org.jboss.reddeer.common.exception.WaitTimeoutExpiredException;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.eclipse.condition.ConsoleHasNoChange;
import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
import org.jboss.reddeer.eclipse.ui.views.properties.PropertiesView;
import org.jboss.reddeer.jface.preference.PreferenceDialog;
import org.jboss.reddeer.swt.exception.SWTLayerException;
import org.jboss.reddeer.swt.impl.button.OkButton;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.junit.After;

/**
 * A base class tests that build docker images
 * 
 * @author adietish@redhat.com
 */
public class AbstractImageBotTest extends AbstractDockerBotTest {

	protected static final String NAME_TAG_SEPARATOR = ":";
	protected static final String IMAGE_TAG_LATEST = "latest";

	protected static final String IMAGE_TEST_BUILD = "test_build";
	protected static final String IMAGE_BUSYBOX = "busybox";
	protected static final String IMAGE_BUSYBOX_LATEST = IMAGE_BUSYBOX + NAME_TAG_SEPARATOR + IMAGE_TAG_LATEST;
	protected static final String IMAGE_ALPINE = "alpine";
	protected static final String IMAGE_ALPINE_TAG = "3.3";
	protected static final String IMAGE_ALPINE_33 = IMAGE_ALPINE + NAME_TAG_SEPARATOR + IMAGE_ALPINE_TAG;

	protected static final String IMAGE_CIRROS = "cirros";
	protected static final String IMAGE_CIRROS_TAG = "0.3.4";

	protected static final String IMAGE_UHTTPD = "fnichol/uhttpd";

	protected static final String IMAGE_HELLO_WORLD = "hello-world";

	protected static final String REGISTRY_SERVER_ADDRESS = "registry.access.redhat.com";

	protected static final String IMAGE_RHEL = "rhel7.2";

	protected static final String DOCKERFILE_FOLDER = "resources/test-build";

	protected static final String REGISTRY_URL = "https://index.docker.io";

	private static final String CONSOLE_SUCCESS_MSG = "Successfully built";

	protected static final String MOCKITO = System.getProperty(SYSPROP_MOCKITO);

	@After
	public void after() {
		cleanUpWorkspace();
	}

	protected DockerImagesTab openDockerImagesTab() {
		DockerImagesTab imageTab = new DockerImagesTab();
		imageTab.activate();
		imageTab.refresh();

		new WaitWhile(new JobIsRunning(), TimePeriod.NORMAL);

		return imageTab;
	}

	protected void buildImage(String imageName, String dockerFileFolder, DockerImagesTab imageTab) {
		try {
			String dockerFilePath = new File(dockerFileFolder).getCanonicalPath();
			getConnection();
			imageTab.buildImage(imageName, dockerFilePath);
			new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		} catch (IOException ex) {
			fail("Resource file not found!");
		}
	}

	protected void assertConsoleSuccess() {
		assertConsoleContains(CONSOLE_SUCCESS_MSG);
	}

	protected void assertConsoleContains(String text) {
		new WaitWhile(new ConsoleHasNoChange());
		ConsoleView consoleView = new ConsoleView();
		consoleView.open();
		if (mockitoIsUsed()) {
			consoleView = MockUtils.getConsoleViewText(text);
		}
		assertFalse("Console has no output!", consoleView.getConsoleText().isEmpty());
		assertTrue("Build has not been successful", consoleView.getConsoleText().contains(text));
	}

	protected void setUpRegister(String serverAddress, String email, String userName, String password) {
		PreferenceDialog dialog = new WorkbenchPreferenceDialog();
		RegistryAccountsPreferencePage page = new RegistryAccountsPreferencePage();
		dialog.open();
		dialog.select(page);
		page.removeRegistry(serverAddress);
		page.addRegistry(serverAddress, email, userName, password);
		try {
			new DefaultShell("New Registry Account").setFocus();
		} catch (SWTLayerException e) {
			new DefaultShell("Preferences").setFocus();
		}
		new OkButton().click();
	}

	protected void deleteRegister(String serverAddress) {
		PreferenceDialog dialog = new WorkbenchPreferenceDialog();
		RegistryAccountsPreferencePage page = new RegistryAccountsPreferencePage();
		dialog.open();
		dialog.select(page);
		page.removeRegistry(serverAddress);
		new WaitWhile(new JobIsRunning());
		new OkButton().click();
	}

	protected void deleteRegisterIfExists(String serverAddress) {
		try {
			deleteRegister(serverAddress);
		} catch (RedDeerException e) {
			// swallow intentionally
		}
	}

	protected void pullImage(String imageName) {
		pullImage(imageName, null, null);
	}

	protected void pullImage(String imageName, String imageTag) {
		pullImage(imageName, imageTag, null);
	}

	protected void pullImage(String imageName, String imageTag, String dockerRegister) {
		if (mockitoIsUsed()) {
			MockUtils.pullImage(DEFAULT_CONNECTION_NAME, imageName, imageTag == null ? "latest" : imageTag);
		} else {
			try {
				getConnection().pullImage(imageName, imageTag, dockerRegister);
			} catch (WaitTimeoutExpiredException ex) {
				killRunningImageJobs();
				fail("Timeout expired when pulling image:" + imageName + (imageTag == null ? "" : ":" + imageTag)
						+ "!");
			}
		}
	}

	protected String getCompleteImageName(String imageName) {
		for (String image : getConnection().getImagesNames()) {
			if (image.contains(imageName)) {
				imageName = image.replace(":", "");
			}
		}
		return imageName;
	}

	protected void deleteImageIfExists(String imageName) {
		deleteImageIfExists(imageName, IMAGE_TAG_LATEST);
	}

	protected void deleteImageIfExists(String imageName, String imageTag) {
		String name = getCompleteImageName(imageName);
		if (imageIsDeployed(name + NAME_TAG_SEPARATOR + imageTag)) {
			if (mockitoIsUsed()) {
				MockUtils.removeImage(DEFAULT_CONNECTION_NAME, imageName, imageTag);
			} else {
				getConnection().getImage(name, imageTag).remove();
			}
		}
	}

	protected void deleteImage(String imageName) {
		deleteImage(imageName, IMAGE_TAG_LATEST);
	}

	protected void deleteImage(String imageName, String imageTag) {
		if (mockitoIsUsed()) {
			MockUtils.removeImage(DEFAULT_CONNECTION_NAME, imageName, imageTag);
			return;
		}
		String completeImageName = getCompleteImageName(imageName);
		DockerImage image = getConnection().getImage(completeImageName, imageTag);
		if (image == null) {
			fail("Image " + imageName + ":" + imageTag + "(" + completeImageName + ":" + imageTag + ")"
					+ " does not exists!");
		}
		image.remove();
	}

	protected void deleteImages(List<String> images) {
		for (String image : images) {
			deleteImage(image);
		}
	}

	protected boolean imageIsDeployed(String imageName) {
		return getConnection().imageIsDeployed(imageName);
	}

	protected int deployedImagesCount(String imageName) {
		return getConnection().deployedImagesCount(imageName);
	}

	protected boolean containerIsDeployed(String containerName) {
		return getConnection().containerIsDeployed(containerName);
	}

	protected void deleteContainerIfExists(String containerName) {
		if (containerIsDeployed(containerName)) {
			getConnection().getContainer(containerName).remove();
			if (!mockitoIsUsed()) {
				new WaitWhile(new ContainerIsDeployedCondition(containerName, getConnection()));
			}
		}
	}

	protected void deleteContainer(String containerName) {
		if (!containerIsDeployed(containerName)) {
			fail("Container " + containerName + " does not exists!");
		}
		getConnection().getContainer(containerName).remove();
		if (!mockitoIsUsed()) {
			new WaitWhile(new ContainerIsDeployedCondition(containerName, getConnection()));
		}
	}

	/**
	 * Deletes the given images. Image names may be provided with tag (ex.
	 * "alpine:3.3"). Also kills all jobs that are still running.
	 * 
	 * @param the
	 *            names of the image that will be deleted
	 */
	protected void deleteImageContainerAfter(String... imageContainerNames) {
		killRunningImageJobs();
		deleteImageContainer(imageContainerNames);
	}

	/**
	 * Deletes the given images. Image names may be provided with tag (ex.
	 * "alpine:3.3").
	 * 
	 * @param the
	 *            names of the image that will be deleted
	 */
	protected void deleteImageContainer(String... imageContainerNames) {
		for (String imageContainerName : imageContainerNames) {
			String[] nameAndTag = imageContainerName.split(":");
			if (imageIsDeployed(imageContainerName)) {
				if (nameAndTag.length == 1) {
					deleteImage(imageContainerName);
				} else {
					deleteImage(nameAndTag[0], nameAndTag[1]);
				}
			}
			if (containerIsDeployed(imageContainerName)) {
				deleteContainer(imageContainerName);
			}
		}
	}

	protected String getContainerIP(String containerName) {
		PropertiesView propertiesView = openPropertiesTabForContainer("Inspect", containerName);
		return propertiesView.getProperty("NetworkSettings", "IPAddress").getPropertyValue();
	}
	
	protected void addTagToImage(String imageName, String imageTag){
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.activate();
		if (mockitoIsUsed()) {
			MockUtils.pullImage(DEFAULT_CONNECTION_NAME, imageName, imageTag);
		} else {
			imagesTab.addTagToImage(imageName, imageTag);
		}
	}
	
	protected void removeTagFromImage(String imageName, String imageTagToRemove){
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.activate();
		if (mockitoIsUsed()) {
			MockUtils.pullImage(DEFAULT_CONNECTION_NAME, imageName, IMAGE_TAG_LATEST);
		} else {
			imagesTab.removeTagFromImage(imageName, imageTagToRemove);
		}
	}
}