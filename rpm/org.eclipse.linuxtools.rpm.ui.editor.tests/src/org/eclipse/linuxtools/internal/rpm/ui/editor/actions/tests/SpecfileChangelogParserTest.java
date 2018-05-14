/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.actions.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.linuxtools.internal.rpm.ui.editor.actions.SpecfileChangelogParser;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpecfileChangelogParserTest {

	private static SpecfileChangelogParser parser;

	@BeforeClass
	public static void setUp() {
		parser = new SpecfileChangelogParser();
	}

	@Test
	public void testParseCurrentFunctionIEditorPart() {
		assertEquals("", parser.parseCurrentFunction(null));
	}

	@Test
	public void testParseCurrentFunctionIEditorInputInt() {
		assertEquals("", parser.parseCurrentFunction(null, 0));
	}

}
