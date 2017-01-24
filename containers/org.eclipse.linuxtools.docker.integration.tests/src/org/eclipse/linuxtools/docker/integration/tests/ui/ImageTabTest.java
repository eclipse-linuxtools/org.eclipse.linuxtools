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

import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
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
		new WaitWhile(new JobIsRunning(), TimePeriod.NORMAL);

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

		PropertiesView propertiesView = new PropertiesView();
		propertiesView.open();
		propertiesView.selectTab("Info");
		String idProp = propertiesView.getProperty("Id").getPropertyValue();
		String repoTagsProp = propertiesView.getProperty("RepoTags").getPropertyValue();
		String createdProp = propertiesView.getProperty("Created").getPropertyValue();
		String sizeProp = propertiesView.getProperty("VirtualSize").getPropertyValue();

		assertTrue("Id in table and in Properties do not match!", idProp.contains(idFromTable));
		assertTrue("RepoTags in table and in Properties do not match!", repoTagsProp.equals(repoTagsFromTable));
		assertTrue("Created in table and in Properties do not match!", createdProp.equals(createdFromTable));
		assertTrue("Size in table and in Properties do not match!", sizeProp.startsWith(sizeFromTable));
	}

	@Test
	public void testImageTabSearch() {
		pullImage(IMAGE_HELLO_WORLD);
		DockerImagesTab imageTab = new DockerImagesTab();
		imageTab.activate();
		imageTab.refresh();
		new WaitWhile(new JobIsRunning(), TimePeriod.NORMAL);
		imageTab.searchImage("aaa");
		assertTrue("Search result is not 0!", imageTab.getTableItems().size() == 0);
		imageTab.searchImage("");
		assertTrue("Search result is 0!", imageTab.getTableItems().size() > 0);
	}

	@After
	public void after() {
		deleteImageContainerAfter(IMAGE_HELLO_WORLD);
	}

}