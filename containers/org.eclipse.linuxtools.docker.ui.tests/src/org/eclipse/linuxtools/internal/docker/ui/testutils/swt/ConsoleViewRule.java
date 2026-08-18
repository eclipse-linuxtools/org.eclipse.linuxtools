/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.ui.console.IConsoleConstants;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * An extension to close the Console view.
 */
public class ConsoleViewRule implements BeforeEachCallback {

	@Override
	public void beforeEach(final ExtensionContext context) {
		Display.getDefault().syncExec(() -> SWTUtils.closeView(new SWTWorkbenchBot(), IConsoleConstants.ID_CONSOLE_VIEW));
	}

}
