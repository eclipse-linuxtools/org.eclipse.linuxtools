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
package org.eclipse.linuxtools.rpm.ui.editor.tests.actions;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpm.ui.editor.actions.SpecfileChangelogParser;

public class SpecfileChangelogParserTest extends TestCase {

	private SpecfileChangelogParser parser;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		parser = new SpecfileChangelogParser();
	}

	public void testParseCurrentFunctionIEditorPart() throws CoreException {
		assertEquals("", parser.parseCurrentFunction(null));
	}

	public void testParseCurrentFunctionIEditorInputInt() throws CoreException {
		assertEquals("", parser.parseCurrentFunction(null, 0));
	}

}
