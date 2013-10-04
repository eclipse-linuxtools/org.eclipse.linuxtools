/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.actions.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
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
	public void testParseCurrentFunctionIEditorPart() throws CoreException {
		assertEquals("", parser.parseCurrentFunction(null));
	}

	@Test
	public void testParseCurrentFunctionIEditorInputInt() throws CoreException {
		assertEquals("", parser.parseCurrentFunction(null, 0));
	}

}
