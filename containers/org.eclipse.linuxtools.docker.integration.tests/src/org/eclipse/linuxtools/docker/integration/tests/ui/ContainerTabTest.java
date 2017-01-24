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

package org.eclipse.linuxtools.docker.integration.tests.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerContainersTab;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.eclipse.ui.views.properties.PropertiesView;
import org.jboss.reddeer.swt.api.TableItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class ContainerTabTest extends AbstractImageBotTest {

	private static final String IMAGE_NAME = IMAGE_BUSYBOX;
	private static final String CONTAINER_NAME = "test_run_busybox";

	@Before
	public void before() {
		deleteAllConnections();
		getConnection();
		pullImage(IMAGE_NAME);
	}

	@Test
	public void testContainerTab() {
		runContainer(DEFAULT_CONNECTION_NAME, IMAGE_NAME, IMAGE_TAG_LATEST, CONTAINER_NAME);
		DockerContainersTab containerTab = new DockerContainersTab();
		containerTab.activate();
		containerTab.refresh();

		new WaitWhile(new JobIsRunning(), TimePeriod.NORMAL);

		// get values from Container Tab
		String nameFromTable = "";
		String imageFromTable = "";
		String createdFromTable = "";
		String commandFromTable = "";
		String portsFromTable = "";
		String statusFromTable = "";

		containerTab.refresh();
		TableItem item = getContainerItem(CONTAINER_NAME, containerTab);
		assertNotNull("Container tab item " + CONTAINER_NAME + " was not found.", item);
		nameFromTable = item.getText();
		imageFromTable = item.getText(1);
		createdFromTable = item.getText(2);
		commandFromTable = item.getText(3);
		portsFromTable = item.getText(4);
		statusFromTable = item.getText(5);

		// get values from Properties view
		PropertiesView propertiesView = new PropertiesView();
		propertiesView.open();
		getConnection().getContainer(CONTAINER_NAME).select();
		propertiesView.selectTab("Info");
		String nameProp = propertiesView.getProperty("Names").getPropertyValue();
		String imageProp = propertiesView.getProperty("Image").getPropertyValue();
		String createdProp = propertiesView.getProperty("Created").getPropertyValue();
		String commandProp = propertiesView.getProperty("Command").getPropertyValue();
		String portsProp = propertiesView.getProperty("Ports").getPropertyValue();
		String statusProp = propertiesView.getProperty("Status").getPropertyValue();

		// compare values
		assertTrue("Name in table and in Properties do not match!(" + nameProp + "-" + nameFromTable + ")",
				nameFromTable.contains(nameProp));
		assertTrue("Image in table and in Properties do not match!(" + imageProp + "-" + imageFromTable + ")",
				imageProp.equals(imageFromTable));
		assertTrue("Created in table and in Properties do not match!(" + createdProp + "-" + createdFromTable + ")",
				createdProp.equals(createdFromTable));
		assertTrue("Command in table and in Properties do not match!(" + commandProp + "-" + commandFromTable + ")",
				commandProp.startsWith(commandFromTable));
		assertTrue("Ports in table and in Properties do not match!(" + portsProp + "-" + portsFromTable + ")",
				portsProp.startsWith(portsFromTable));
		assertTrue("Status in table and in Properties do not match!(" + statusProp + "-" + statusFromTable + ")",
				statusProp.startsWith(statusFromTable));
	}

	@Test
	public void testContainerTabSearch() {
		runContainer(DEFAULT_CONNECTION_NAME, IMAGE_NAME, IMAGE_TAG_LATEST, CONTAINER_NAME);
		DockerContainersTab containerTab = new DockerContainersTab();
		containerTab.activate();
		containerTab.refresh();
		containerTab.searchContainer("aaa");
		assertTrue("Search result is not 0!", containerTab.getTableItems().size() == 0);
		containerTab.searchContainer("");
		assertTrue("Search result is 0!", containerTab.getTableItems().size() > 0);
	}
	
	private void runContainer(String connectionName, String imageName, String imageTag, String containerName){
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.runImage(imageName);
		ImageRunSelectionPage firstPage = new ImageRunSelectionPage();
		firstPage.setContainerName(containerName);
		firstPage.finish();
		if (mockitoIsUsed()) {
			MockUtils.runContainer(connectionName, imageName, imageTag, containerName);
		}
		getConnection().refresh();
		new WaitWhile(new JobIsRunning());
	}

	private TableItem getContainerItem(String containerName, DockerContainersTab containersTab) {
		for (TableItem item : containersTab.getTableItems()) {
			if (item.getText(0).contains(containerName)) {
				return item;
			}
		}
		return null;
	}

	@After
	public void after() {
		deleteContainerIfExists(CONTAINER_NAME);
	}

}