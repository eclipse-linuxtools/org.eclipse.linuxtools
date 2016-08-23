/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.stream.Stream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.Matchers;

/**
 * Custom {@link Matcher}
 */
public class CustomMatchers {

	public static String[] arrayContains(final String expectation) {
		return Matchers.argThat(new BaseMatcher<String[]>() {
	
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
