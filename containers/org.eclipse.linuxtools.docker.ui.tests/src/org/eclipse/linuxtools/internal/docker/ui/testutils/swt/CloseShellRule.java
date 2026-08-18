/*******************************************************************************
 * Copyright (c) 2015, 2026 Red Hat.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.testutils.swt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionSettingsFinder;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Closes the wizard(s) and dialog(s) left open after each test, using their
 * "Cancel" button when available.
 */
public class CloseShellRule implements AfterEachCallback {

	private final String buttonLabel;

	public CloseShellRule(final String buttonLabel) {
		this.buttonLabel = buttonLabel;
	}

	@Override
	public void afterEach(final ExtensionContext context) {
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		// wizards can be stacked (eg: the image search wizard opens on top of
		// the image pull wizard) so close them all, from the topmost one down,
		// rather than relying on the active shell: after a shell is closed, the
		// activation of the one underneath is not immediate, and not even
		// guaranteed without a window manager.
		for (SWTBotShell shell : dialogShells(bot)) {
			closeShell(shell);
		}
		DockerConnectionManager.getInstance()
				.setConnectionSettingsFinder(new DefaultDockerConnectionSettingsFinder());
	}

	/**
	 * @return the open shells other than the workbench window, topmost (ie,
	 *         most recently opened) first.
	 */
	private static List<SWTBotShell> dialogShells(final SWTWorkbenchBot bot) {
		final Shell workbenchShell = SWTUtils
				.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		final List<SWTBotShell> dialogShells = new ArrayList<>();
		for (SWTBotShell shell : bot.shells()) {
			if (shell.widget != workbenchShell && shell.isOpen()) {
				dialogShells.add(shell);
			}
		}
		Collections.reverse(dialogShells);
		return dialogShells;
	}

	private void closeShell(final SWTBotShell shell) {
		if (!shell.isOpen()) {
			return;
		}
		try {
			shell.activate();
			shell.bot().button(this.buttonLabel).click();
		} catch (WidgetNotFoundException e) {
			// no such button in this shell: just close it
			if (shell.isOpen()) {
				shell.close();
			}
		}
		new SWTWorkbenchBot().waitUntil(Conditions.shellCloses(shell));
	}
}
