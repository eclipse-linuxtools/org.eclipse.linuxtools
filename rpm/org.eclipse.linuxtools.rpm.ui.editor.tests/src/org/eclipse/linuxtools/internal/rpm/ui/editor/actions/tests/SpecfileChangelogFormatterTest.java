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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpm.ui.editor.actions.SpecfileChangelogFormatter;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SpecfileChangelogFormatterTest extends FileTestCase {

	private static final String USER_MAIL = "someone@redhat.com";
	private static final String USER_NAME = "Alexander Kurtakov";
	private SpecfileChangelogFormatter formatter;
	private IEditorPart editor;
	public final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("EEE MMM d yyyy");

	@Override
	@BeforeEach
	public void setUp() throws CoreException {
		super.setUp();
		newFile("%changelog");
		editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), testFile,
				"org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");
		formatter = new SpecfileChangelogFormatter();
	}

	@Override
	@AfterEach
	public void tearDown() {
		closeEditor(editor);
	}

	@Test
	public void testFormatDateLine() {
		String expectedLine = MessageFormat.format("* {0} {1} <{2}> {3}{4}-{5}", SIMPLE_DATE_FORMAT.format(new Date()), //$NON-NLS-1$
				USER_NAME, USER_MAIL, "", "0", "0");
		assertEquals(expectedLine, formatter.formatDateLine(USER_NAME, USER_MAIL));
	}

	@Test
	public void testMergeChangelogStringStringStringIEditorPartStringString() {
		// TODO find how to test this
		formatter.mergeChangelog("proba", "", "", editor, "", "");
	}

}
