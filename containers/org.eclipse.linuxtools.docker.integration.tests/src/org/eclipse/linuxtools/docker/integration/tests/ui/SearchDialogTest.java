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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerConnectionManager;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageSearchPage;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageTagSelectionPage;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageSearchResultFactory;
import org.jboss.reddeer.common.wait.AbstractWait;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.core.condition.ProgressInformationShellIsActive;
import org.jboss.reddeer.swt.impl.button.CancelButton;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 * 
 * @author jkopriva@redhat.com
 * @contributor adietish@redhat.com
 *
 */

public class SearchDialogTest extends AbstractImageBotTest {

	private static final String IMAGE_NAME = IMAGE_BUSYBOX;
	private static final String IMAGE_TAG = "1.24";
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
		getConnection().openImageSearchDialog(IMAGE_NAME, null, REGISTRY_URL);
		ImageSearchPage pageOne = new ImageSearchPage();
		pageOne.searchImage();
		assertFalse("Search result is empty!", pageOne.getSearchResults().isEmpty());
		assertTrue("Search result do not contains image:" + EXPECTED_IMAGE_NAME + "!",
				pageOne.searchResultsContains(EXPECTED_IMAGE_NAME));
		pageOne.next();

		new WaitWhile(new ProgressInformationShellIsActive(), TimePeriod.NORMAL);
		AbstractWait.sleep(TimePeriod.getCustom(5));
		ImageTagSelectionPage pageTwo = new ImageTagSelectionPage();
		assertFalse("Search tags are empty!", pageTwo.getTags().isEmpty());
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		if (!pageTwo.tagsContains(IMAGE_TAG)) {
			pageTwo.cancel();
			new CancelButton().click();
			fail("Search results do not contain tag: " + IMAGE_TAG + "!");
		}
		pageTwo.selectTag(IMAGE_TAG);
		pageTwo.finish();
		new PushButton("Finish").click();
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}

	private void mockResults() {
		final DockerClient client = MockDockerClientFactory
				.onSearch(IMAGE_NAME, MockImageSearchResultFactory.name(IMAGE_NAME).build(),
						MockImageSearchResultFactory.name("other/bar").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from(DEFAULT_CONNECTION_NAME, client)
				.withDefaultTCPConnectionSettings();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}

}