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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.MailHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.MailHyperlinkDetector;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.jupiter.api.Test;

public class MailHyperlinkDetectorTest extends FileTestCase {

	@Test
	public void testDetectHyperlinks() throws PartInitException {
		String testText = """
				Version: 0.0
				Release: 0
				%changelog
				* Fri Feb 27 2009 Test <someone@smth.com> 3.3.2.4-6
				-
				* Fri Feb 27 2009 Test someone@smth.com 3.3.2.4-6
				-
				""";
		newFile(testText);

		IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				testFile, "org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");

		editor = (SpecfileEditor) openEditor;
		editor.doRevertToSaved();
		MailHyperlinkDetector elementDetector = new MailHyperlinkDetector();
		elementDetector.setEditor(editor);
		// test mail
		IRegion region = new Region(38, 0);
		IHyperlink[] returned = elementDetector.detectHyperlinks(editor.getSpecfileSourceViewer(), region, false);
		assertEquals(1, returned.length);
		assertTrue(returned[0] instanceof MailHyperlink);

		region = new Region(124, 0);
		returned = elementDetector.detectHyperlinks(editor.getSpecfileSourceViewer(), region, false);
		assertEquals(1, returned.length);
		assertTrue(returned[0] instanceof MailHyperlink);
	}

	@Test
	public void testDetectHyperlinksNoRegionAndTextViewer() {
		MailHyperlinkDetector elementDetector = new MailHyperlinkDetector();
		elementDetector.setEditor(editor);
		IHyperlink[] returned = elementDetector.detectHyperlinks(null, null, false);
		assertNull(returned);
	}

}
