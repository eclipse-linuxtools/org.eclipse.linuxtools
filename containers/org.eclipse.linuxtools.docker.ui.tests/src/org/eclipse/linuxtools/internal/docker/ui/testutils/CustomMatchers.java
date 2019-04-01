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

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.stream.Stream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.hamcrest.MockitoHamcrest;

/**
 * Custom {@link Matcher}
 */
public class CustomMatchers {

	public static String[] arrayContains(final String expectation) {
		return MockitoHamcrest.argThat(new BaseMatcher<String[]>() {

			@Override
			public boolean matches(Object items) {
				return Stream.of((String[]) items).anyMatch(item -> item.equals(expectation));
			}

			@Override
			public void describeTo(Description description) {

			}

		});
	}

}
