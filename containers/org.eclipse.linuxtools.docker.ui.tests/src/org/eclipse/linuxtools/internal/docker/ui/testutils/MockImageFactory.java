/*******************************************************************************
 * Copyright (c) 2015, 2021 Red Hat.
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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.mandas.docker.client.messages.Image;

/**
 * A factory for mock {@link Image}s.
 */
public class MockImageFactory {

	public static final char[] HEXA = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	public static Image of(final String id) {
		return new Image(Long.toString(new Date().getTime()), id, "", List.of(), List.of(), 0L, 0L, Map.of());
	}

	public static Image of(final String id, final List<String> names) {
		return new Image(Long.toString(new Date().getTime()), id, "", names, List.of(), 0L, 0L, Map.of());
	}

	public static Image of(final String id, String parentId, String... names) {
		return new Image(Long.toString(new Date().getTime()), id, parentId, List.of(names), List.of(), 0L, 0L,
				Map.of());
	}
}
