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

package org.eclipse.linuxtools.docker.integration.tests.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerConnectionManager;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageSearchPage;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageTagSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerExplorerView;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageSearchResultFactory;
import org.eclipse.reddeer.common.wait.AbstractWait;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.swt.api.TableItem;
import org.eclipse.reddeer.swt.impl.button.CancelButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.junit.Before;
import org.junit.Test;

import org.mandas.docker.client.DockerClient;

public class SearchDialogTest extends AbstractImageBotTest {

	private static final String IMAGE_NAME = IMAGE_BUSYBOX;
	private static final String IMAGE_TAG = "latest";
	private static final String EXPECTED_IMAGE_NAME = "busybox";

	@Before
	public void before() {
		deleteAllConnections();
		getConnection();
		deleteImageIfExists(IMAGE_NAME, IMAGE_TAG);
		if (mockitoIsUsed()) {
			mockResults();
		}
	}

	@Test
	public void testSearchDialog() {
		DockerExplorerView explorer = new DockerExplorerView();
		explorer.open();
		AbstractWait.sleep(TimePeriod.getCustom(5));
		getConnection().openImageSearchDialog(IMAGE_NAME, null, REGISTRY_URL);
		ImageSearchPage pageOne = new ImageSearchPage(explorer);
		pageOne.searchImage();
		assertFalse("Search result is empty!", pageOne.getSearchResults().isEmpty());
		assertTrue("Search result do not contains image:" + EXPECTED_IMAGE_NAME + "!",
				pageOne.searchResultsContains(EXPECTED_IMAGE_NAME));
		pageOne.next();
		AbstractWait.sleep(TimePeriod.getCustom(5));
		ImageTagSelectionPage pageTwo = new ImageTagSelectionPage(pageOne);
		if (!mockitoIsUsed()) {
			List<TableItem> tags = pageTwo.getTags();
			assertFalse("Search tags are empty!", tags.isEmpty());
			new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
			if (!pageTwo.tagsContains(IMAGE_TAG)) {
				pageTwo.cancel();
				new CancelButton().click();
				fail("Search results do not contain tag: " + IMAGE_TAG + "! Page contains tags: " + tags.toString());
			}
			pageTwo.selectTag(IMAGE_TAG);
			pageTwo.cancel();
		} else {
			pageTwo.cancel();
		}
		new PushButton("Cancel").click();
		new WaitWhile(new JobIsRunning());
	}

	private void mockResults() {
		final DockerClient client = MockDockerClientFactory
				.onSearch(IMAGE_NAME, MockImageSearchResultFactory.name(IMAGE_NAME + ":" + IMAGE_TAG).build(),
						MockImageSearchResultFactory.name(IMAGE_NAME).build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from(DEFAULT_CONNECTION_NAME, client)
				.withDefaultTCPConnectionSettings();
		MockDockerImageFactory.name(IMAGE_NAME + ":" + IMAGE_TAG).connection(dockerConnection).build();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}

}