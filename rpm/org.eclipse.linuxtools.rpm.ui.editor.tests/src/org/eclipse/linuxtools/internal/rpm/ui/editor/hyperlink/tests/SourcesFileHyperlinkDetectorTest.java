/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat, Inc.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.SourcesFileHyperlinkDetector;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SourcesFileHyperlinkDetectorTest extends FileTestCase {
	@BeforeEach
	public void init() throws CoreException {
		super.setUp();
		String testText = "Source0: test.zip\n" + "Patch0: test.patch\n" + "Source1: www.example.com/test.zip\n"
				+ "Source2: http://www.example.com/test.zip\n";
		newFile(testText);
	}

	@Test
	public void testDetectHyperlinks() throws PartInitException {
		IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				testFile, "org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");

		editor = (SpecfileEditor) openEditor;
		editor.doRevertToSaved();
		SourcesFileHyperlinkDetector elementDetector = new SourcesFileHyperlinkDetector();
		elementDetector.setEditor(editor);
		// test source0 element
		IRegion region = new Region(10, 0);
		IHyperlink[] returned = elementDetector.detectHyperlinks(editor.getSpecfileSourceViewer(), region, false);
		// because test.zip does not exist, and is not a valid url
		// it should not have hyperlinks
		assertNull(returned);
	}

	@Test
	public void testDetectHyperlinksInvalidURL() throws PartInitException {
		IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				testFile, "org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");

		editor = (SpecfileEditor) openEditor;
		editor.doRevertToSaved();
		SourcesFileHyperlinkDetector elementDetector = new SourcesFileHyperlinkDetector();
		elementDetector.setEditor(editor);
		// test source1 element
		IRegion region = new Region(47, 0);
		IHyperlink[] returned = elementDetector.detectHyperlinks(editor.getSpecfileSourceViewer(), region, false);
		// because the protocol is missing, it should not show any hyperlinks
		assertNull(returned);
	}

	@Test
	public void testDetectHyperlinksValidURL() throws PartInitException {
		IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				testFile, "org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");

		editor = (SpecfileEditor) openEditor;
		editor.doRevertToSaved();
		SourcesFileHyperlinkDetector elementDetector = new SourcesFileHyperlinkDetector();
		elementDetector.setEditor(editor);
		// test source2 element
		IRegion region = new Region(82, 0);
		IHyperlink[] returned = elementDetector.detectHyperlinks(editor.getSpecfileSourceViewer(), region, false);
		// 1 = Download from URL (Open in browser should not show up because
		// URLHyperlinkWithMacroDetector detects that)
		assertEquals(1, returned.length);
	}

	@Test
	public void testDetectNoPatchInProject() throws PartInitException {
		IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				testFile, "org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");

		editor = (SpecfileEditor) openEditor;
		editor.doRevertToSaved();
		SourcesFileHyperlinkDetector elementDetector = new SourcesFileHyperlinkDetector();
		elementDetector.setEditor(editor);
		// test patch element
		IRegion region = new Region(27, 0);
		IHyperlink[] returned = elementDetector.detectHyperlinks(editor.getSpecfileSourceViewer(), region, false);
		// 1 = Create test.patch because test.patch doesn't exist in current project
		assertEquals(1, returned.length);
	}

	@Test
	public void testDetectHyperlinksNoRegionAndTextViewer() {
		SourcesFileHyperlinkDetector elementDetector = new SourcesFileHyperlinkDetector();
		elementDetector.setEditor(editor);
		IHyperlink[] returned = elementDetector.detectHyperlinks(null, null, false);
		assertNull(returned);
	}
}
