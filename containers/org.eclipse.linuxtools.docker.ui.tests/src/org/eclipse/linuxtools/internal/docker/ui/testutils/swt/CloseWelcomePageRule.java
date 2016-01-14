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
 * Closes the Welcome page
 */
public class CloseWelcomePageRule extends ExternalResource {

	@Override
	protected void before() {
		Display.getDefault().syncExec(() -> {
				if (PlatformUI.getWorkbench().getIntroManager().getIntro() != null) {
					PlatformUI.getWorkbench().getIntroManager()
							.closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());
				}
		});
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		bot.perspectiveById("org.eclipse.linuxtools.docker.ui.perspective").activate(); //$NON-NLS-1$
	}
}
