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

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

/**
 * Custom assertions on a given {@link SWTBotButton}.
 */
public class ToolbarButtonAssertion extends AbstractSWTBotAssertion<ToolbarButtonAssertion, SWTBotToolbarButton> {

	protected ToolbarButtonAssertion(final SWTBotToolbarButton actual) {
		super(actual, ToolbarButtonAssertion.class);
	}

	public static ToolbarButtonAssertion assertThat(final SWTBotToolbarButton actual) {
		return new ToolbarButtonAssertion(actual);
	}

}
