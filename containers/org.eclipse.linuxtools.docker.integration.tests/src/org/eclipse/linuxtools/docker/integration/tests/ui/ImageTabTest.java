/*******************************************************************************
 * Copyright (c) 2017,2022 Red Hat, Inc.
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

package org.eclipse.linuxtools.docker.integration.tests.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.reddeer.swt.api.TableItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ImageTabTest extends AbstractImageBotTest {

	@Before
	public void before() {
		clearConsole();
		deleteAllConnections();
		getConnection();
	}

	@Test
	public void testImageTab() {
		pullImage(IMAGE_HELLO_WORLD);
		DockerImagesTab imageTab = new DockerImagesTab();
		imageTab.activate();
		imageTab.refresh();
		new WaitWhile(new JobIsRunning(), TimePeriod.DEFAULT);

		String idFromTable = "";
		String repoTagsFromTable = "";
		String createdFromTable = "";
		String sizeFromTable = "";

		for (TableItem item : imageTab.getTableItems()) {
			if (item.getText(1).contains(IMAGE_HELLO_WORLD)) {
				idFromTable = item.getText();
				repoTagsFromTable = item.getText(1);
				createdFromTable = item.getText(2);
				sizeFromTable = item.getText(3).replaceAll(".", "").replaceAll(" MB", "");
				item.click();
			}
		}
		idFromTable = idFromTable.replace("sha256:", "");

		getConnection().getImage(getCompleteImageName(IMAGE_HELLO_WORLD)).select();

		PropertySheet propertiesView = new PropertySheet();
		propertiesView.open();
		propertiesView.selectTab("Info");
		String idProp = propertiesView.getProperty("Id").getPropertyValue();
		String repoTagsProp = propertiesView.getProperty("RepoTags").getPropertyValue();
		String createdProp = propertiesView.getProperty("Created").getPropertyValue();
		String sizeProp = propertiesView.getProperty("VirtualSize").getPropertyValue();

		assertTrue("Id in table and in Properties do not match!", idProp.contains(idFromTable));
		assertEquals("RepoTags in table and in Properties do not match!", repoTagsProp, repoTagsFromTable);
		assertEquals("Created in table and in Properties do not match!", createdProp, createdFromTable);
		assertTrue("Size in table and in Properties do not match!", sizeProp.startsWith(sizeFromTable));
	}

	@Test
	public void testImageTabSearch() {
		pullImage(IMAGE_HELLO_WORLD);
		DockerImagesTab imageTab = new DockerImagesTab();
		imageTab.activate();
		imageTab.refresh();
		new WaitWhile(new JobIsRunning(), TimePeriod.DEFAULT);
		imageTab.searchImage("aaa");
		assertEquals("Search result is not 0!", 0, imageTab.getTableItems().size());
		imageTab.searchImage("");
		assertTrue("Search result is 0!", imageTab.getTableItems().size() > 0);
	}

	@Override
	@After
	public void after() {
		deleteImageContainerAfter(IMAGE_HELLO_WORLD);
	}

}