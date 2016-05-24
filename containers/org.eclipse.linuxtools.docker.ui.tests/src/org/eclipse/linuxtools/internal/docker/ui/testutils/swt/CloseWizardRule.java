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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionSettingsFinder;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.PlatformUI;
import org.junit.rules.ExternalResource;

/**
 * Closes the wizard(s) after each test, if the "Cancel" button is available
 */
public class CloseWizardRule extends ExternalResource {

	@Override
	protected void after() {
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		try {
			while (isInDialog(bot) && getCancelButton(bot) != null) {
				getCancelButton(bot).click();
			}
			DockerConnectionManager.getInstance()
					.setConnectionSettingsFinder(new DefaultDockerConnectionSettingsFinder());
		} catch (WidgetNotFoundException e) {
			// ignoring
		}
	}

	private static boolean isInDialog(final SWTWorkbenchBot bot) {
		final SWTBotShell activeShell = bot.activeShell();
		final String text = SWTUtils.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getText());
		final String shellText = activeShell.getText();
		return text != null && !text.equals(shellText);
	}

	private static SWTBotButton getCancelButton(final SWTWorkbenchBot bot) {
		return bot.button(IDialogConstants.CANCEL_LABEL);
	}
}
