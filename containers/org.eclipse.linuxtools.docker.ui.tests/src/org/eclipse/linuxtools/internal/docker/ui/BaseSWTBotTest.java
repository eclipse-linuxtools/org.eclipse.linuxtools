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

import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Base class for SWTBot Test
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class BaseSWTBotTest {

	protected SWTWorkbenchBot bot;
	protected SWTBotView dockerExplorerViewBot;
	protected DockerExplorerView dockerExplorerView;

	@Before
	public void setup() {
		this.bot = new SWTWorkbenchBot();
		this.dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		this.dockerExplorerViewBot.show();
		this.dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(false));
	}


}
