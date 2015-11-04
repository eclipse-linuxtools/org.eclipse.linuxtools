/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionStorageManager;
import org.eclipse.linuxtools.internal.docker.ui.BaseSWTBotTest;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerExplorerViewAssertion;
import org.eclipse.swt.widgets.Display;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Testing the {@link DockerExplorerView} {@link Viewer}
 */
public class DockerExplorerViewSWTBotTest extends BaseSWTBotTest {

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(); 
	
	@Override
	@Before
	public void setup() {
		super.setup();
		bot.views().stream()
				.filter(v -> v.getReference().getId().equals("org.eclipse.linuxtools.docker.ui.dockerContainersView")
						|| v.getReference().getId().equals("org.eclipse.linuxtools.docker.ui.dockerImagesView"))
				.forEach(v -> v.close());
	}

	
	@AfterClass
	public static void restoreDefaultConfig() {
		DockerConnectionManager.getInstance().setConnectionStorageManager(new DefaultDockerConnectionStorageManager());
	}

	private void configureConnectionManager(final IDockerConnection... connections) {
		DockerConnectionManager.getInstance()
				.setConnectionStorageManager(MockDockerConnectionStorageManagerFactory.load(connections));
		Display.getDefault().syncExec(() -> DockerConnectionManager.getInstance().reloadConnections());
	}

	@Test
	public void shouldDisplayExplanationPane() {
		// given
		configureConnectionManager();
		// when
		dockerExplorerViewBot.getToolbarButtons().get(1).click();
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isEmpty();
	}

	@Test
	public void shouldDisplayConnectionsPane() {
		// given
		configureConnectionManager(MockDockerConnectionFactory.noImageNoContainer("Empty"));
		// when
		dockerExplorerViewBot.getToolbarButtons().get(1).click();
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isNotEmpty();
	}

}
