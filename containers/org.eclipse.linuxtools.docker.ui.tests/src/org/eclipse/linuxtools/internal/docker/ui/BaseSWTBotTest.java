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

import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Base class for SWTBot Test
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class BaseSWTBotTest {

	protected static SWTWorkbenchBot bot;
	protected SWTBotView dockerExplorerViewBot;
	protected DockerExplorerView dockerExplorerView;

	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		//System.err.println("Closing Welcome page");
		bot = new SWTWorkbenchBot();
		//bot.views().stream().map(v -> v.getViewReference().getId()).forEach(s -> System.err.println(s));
		bot.views().stream().filter(v -> v.getReference().getTitle().equals("Welcome")).forEach(v -> v.close());
		bot.perspectiveById("org.eclipse.linuxtools.docker.ui.perspective").activate();
		//System.err.println("In Docker perspective");
		bot.views().stream().map(v -> v.getViewReference().getId()).forEach(s -> System.err.println(s));
		Thread.sleep(TimeUnit.SECONDS.toMillis(1));
	}
	
	@Before
	public void setup() {
		dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		dockerExplorerViewBot.show();
		dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(false));
	}


}
