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

package org.eclipse.linuxtools.internal.docker.ui;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Base class for SWTBot Test
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class BaseSWTBotTest {

	protected static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() {
		bot = new SWTWorkbenchBot();
		bot.views().stream().filter(v -> v.getReference().getTitle().equals("Welcome")).forEach(v -> v.close());
		bot.perspectiveById("org.eclipse.linuxtools.docker.ui.perspective").activate();
	}
	
	public static void configureConnectionManager(final DockerExplorerView dockerExplorerView, final IDockerConnection... connections) throws InterruptedException {
		DockerConnectionManager.getInstance()
				.setConnectionStorageManager(MockDockerConnectionStorageManagerFactory.providing(connections));
		SWTUtils.asyncExec(() -> {
			DockerConnectionManager.getInstance().reloadConnections();
			dockerExplorerView.getCommonViewer().refresh();
		});

	}
	
}
