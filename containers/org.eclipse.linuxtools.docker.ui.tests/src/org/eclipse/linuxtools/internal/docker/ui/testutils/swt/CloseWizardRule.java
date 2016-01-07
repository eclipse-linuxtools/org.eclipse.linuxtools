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
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.junit.rules.ExternalResource;

/**
 * Closes the wizard(s) after each test, if the "Cancel" button is available
 */
public class CloseWizardRule extends ExternalResource {

	@Override
	protected void after() {
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		try {
			while (bot.button(IDialogConstants.CANCEL_LABEL) != null) {
				bot.button(IDialogConstants.CANCEL_LABEL).click();
			}
		} catch (WidgetNotFoundException e) {
			// ignoring
		}
	}
}
