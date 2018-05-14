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
package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.tests;

import static org.junit.Assert.assertNotNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.SourcesFileCreateHyperlink;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.junit.Before;
import org.junit.Test;

public class SourcesFileCreateHyperlinkTest extends FileTestCase {
	@Before
	public void init() throws CoreException {
		super.setUp();
		String testText = "Patch0: test\n";
		newFile(testText);
	}

	@Test
	public void testCreatePatch() {
		SourcesFileCreateHyperlink patchTest = new SourcesFileCreateHyperlink(testFile,
				specfile.getPatch(0).getFileName(), null);
		assertNotNull(patchTest);
		patchTest.open();
		assertNotNull(testFile.getProject().findMember("test"));
	}
}
