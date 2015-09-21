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

import org.assertj.core.api.AbstractAssert;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;

/**
 * Custom assertions on a given {@link AbstractSWTBot} widget
 * @param <SWTWidget>
 */
public abstract class AbstractSWTBotAssertion<Assertion extends AbstractSWTBotAssertion<Assertion, SWTWidget>, SWTWidget extends AbstractSWTBot<?>>
		extends AbstractAssert<Assertion, SWTWidget> {
	
	protected AbstractSWTBotAssertion(final SWTWidget actual, final Class<Assertion> clazz) {
		super(actual, clazz);
	}

	@SuppressWarnings("unchecked")
	public Assertion isEnabled() {
		notNullValue();
		if(!actual.isEnabled()) {
			failWithMessage("Expected checkbox with text '%s' to be enabled but it was not", actual.getText());
		}
		return (Assertion) this;
	}

	@SuppressWarnings("unchecked")
	public Assertion isNotEnabled() {
		notNullValue();
		if(actual.isEnabled()) {
			failWithMessage("Expected checkbox with text '%s' to be disabled but it was not", actual.getText());
		}
		return (Assertion) this;
	}

}
