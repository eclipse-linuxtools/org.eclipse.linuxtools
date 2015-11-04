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

package org.eclipse.linuxtools.internal.docker.ui.testutils.swt;

import java.util.concurrent.TimeUnit;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.rules.ExternalResource;

/**
 * Closes the Welcome page
 */
public class CloseWelcomePageRule extends ExternalResource {

	@Override
	protected void before() {
		try {
			final SWTWorkbenchBot bot = new SWTWorkbenchBot();
			bot.views().stream().filter(v -> v.getReference().getTitle().equals("Welcome")).forEach(v -> v.close());
			bot.perspectiveById("org.eclipse.linuxtools.docker.ui.perspective").activate();
			bot.views().stream().map(v -> v.getViewReference().getId()).forEach(s -> System.err.println(s));
			Thread.sleep(TimeUnit.SECONDS.toMillis(1));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
