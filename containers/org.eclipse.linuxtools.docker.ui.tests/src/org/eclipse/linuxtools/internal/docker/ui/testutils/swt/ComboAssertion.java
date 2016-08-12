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

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;

/**
 * Custom assertions on a given {@link SWTBotCheckBox}.
 */
public class ComboAssertion extends AbstractSWTBotAssertion<ComboAssertion, SWTBotCombo> {

	protected ComboAssertion(final SWTBotCombo actual) {
		super(actual, ComboAssertion.class);
	}

	public static ComboAssertion assertThat(final SWTBotCombo actual) {
		return new ComboAssertion(actual);
	}

	public ComboAssertion itemSelected(final String expectedItem) {
		notNullValue();
		if (actual.selectionIndex() < 0) {
			failWithMessage("Expected combo to have selection to '%s' but it had none", expectedItem);
		} else if (!actual.selection().equals(expectedItem)) {
			failWithMessage("Expected combo to have selection to '%s' but it was '%s'", expectedItem,
					actual.selection());
		}
		return this;
	}

	public ComboAssertion indexItemSelected(final int expectedItemIndex) {
		notNullValue();
		if (actual.selectionIndex() < 0) {
			failWithMessage("Expected combo to have selection index to '%s' but it had none", expectedItemIndex);
		} else if (actual.selectionIndex() != expectedItemIndex) {
			failWithMessage("Expected combo to have selection index to '%s' but it was '%s'", expectedItemIndex,
					actual.selectionIndex());
		}
		return this;
	}

}
