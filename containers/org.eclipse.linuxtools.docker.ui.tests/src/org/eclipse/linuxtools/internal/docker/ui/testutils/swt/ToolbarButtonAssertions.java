/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

/**
 * Custom assertions on a given {@link SWTBotButton}.
 */
public class ToolbarButtonAssertions extends AbstractSWTBotAssertions<ToolbarButtonAssertions, SWTBotToolbarButton> {

	protected ToolbarButtonAssertions(final SWTBotToolbarButton actual) {
		super(actual, ToolbarButtonAssertions.class);
	}

	public static ToolbarButtonAssertions assertThat(final SWTBotToolbarButton actual) {
		return new ToolbarButtonAssertions(actual);
	}

}
