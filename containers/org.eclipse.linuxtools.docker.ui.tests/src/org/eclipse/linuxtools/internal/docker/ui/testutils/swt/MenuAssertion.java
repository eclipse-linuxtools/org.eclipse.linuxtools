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

import static org.hamcrest.Matchers.notNullValue;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

/**
 * Custom assertions on a given {@link SWTBotButton}.
 */
public class MenuAssertion extends AbstractSWTBotAssertion<MenuAssertion, SWTBotMenu> {

	protected MenuAssertion(final SWTBotMenu actual) {
		super(actual, MenuAssertion.class);
	}

	public static MenuAssertion assertThat(final SWTBotMenu actual) {
		return new MenuAssertion(actual);
	}

	@SuppressWarnings("unchecked")
	public MenuAssertion isVisible() {
		notNullValue();
		if (!actual.isEnabled()) {
			failWithMessage("Expected menu with text '%s' to be visible but it was not", actual.getText());
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public MenuAssertion isNotVisible() {
		notNullValue();
		if (actual.isEnabled()) {
			failWithMessage("Expected menu with text '%s' to be visible but it was not", actual.getText());
		}
		return this;
	}
}
