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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.ui.PlatformUI;
import org.junit.rules.ExternalResource;

/**
 * Closes the Welcome page and optionally opens a given perspective
 */
public class CloseWelcomePageRule extends ExternalResource {

	/** the Id of the perspective to open. */
	private final String defaultPerspectiveId;
	
	/**
	 * Default constructor when the "Docker tooling" perspective is going to be opened once the welcome page was closed.
	 */
	public CloseWelcomePageRule() {
		this.defaultPerspectiveId = "org.eclipse.linuxtools.docker.ui.perspective";
	}
	
	/**
	 * Custom constructor with the id of the perspective to open once the welcome page was closed.
	 * @param perspectiveId the id of the perspective to open.
	 */
	public CloseWelcomePageRule(final String perspectiveId) {
		this.defaultPerspectiveId = perspectiveId;
	}
	
	@Override
	protected void before() {
		Display.getDefault().syncExec(() -> {
				if (PlatformUI.getWorkbench().getIntroManager().getIntro() != null) {
					PlatformUI.getWorkbench().getIntroManager()
							.closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());
				}
		});
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		bot.perspectiveById(defaultPerspectiveId).activate();
	}
}
