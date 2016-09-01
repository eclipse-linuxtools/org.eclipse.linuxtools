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
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;

/**
 * Custom assertions on a given {@link ITabDescriptor}.
 */
public class TabDescriptorAssertion extends AbstractAssert<TabDescriptorAssertion, ITabDescriptor> {

	protected TabDescriptorAssertion(final ITabDescriptor actual) {
		super(actual, TabDescriptorAssertion.class);
	}

	public static TabDescriptorAssertion assertThat(final ITabDescriptor actual) {
		return new TabDescriptorAssertion(actual);
	}

	public TabDescriptorAssertion hasId(final String id) {
		notNullValue();
		if (!actual.getId().equals(id)) {
			failWithMessage("Expected tab section with id '%s' to be selected but it was '%s'", id, actual.getId());
		}
		return this;
	}

}
