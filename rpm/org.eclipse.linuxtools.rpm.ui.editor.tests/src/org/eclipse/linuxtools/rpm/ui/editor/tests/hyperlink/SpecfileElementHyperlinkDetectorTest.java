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
package org.eclipse.linuxtools.rpm.ui.editor.tests.hyperlink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.SpecfileElementHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.SpecfileElementHyperlinkDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.Test;

public class SpecfileElementHyperlinkDetectorTest extends FileTestCase {

	@Test
	public void testDetectHyperlinks() throws PartInitException {
		String testText = "%define smth other\nSource0: test.zip\nPatch0: first.patch\n"
				+ "%build\n %{SOURCE0}\n%patch0\n%{smth}\n";
		newFile(testText);
		SpecfileElementHyperlinkDetector elementDetector = new SpecfileElementHyperlinkDetector();
		elementDetector.setSpecfile(specfile);

		IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage(), testFile,
				"org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");

		editor = (SpecfileEditor) openEditor;
		editor.doRevertToSaved();
		// test source element
		IRegion region = new Region(74, 0);
		IHyperlink[] returned = elementDetector.detectHyperlinks(
				editor.getSpecfileSourceViewer(), region, false);
		SpecfileElementHyperlink element = (SpecfileElementHyperlink) returned[0];
		assertTrue(element.getSource() instanceof SpecfileSource);
		SpecfileSource source = (SpecfileSource) element.getSource();
		assertEquals(source.getSourceType(), SpecfileSource.SourceType.SOURCE);
		assertEquals(source.getFileName(), "test.zip");

		// test patch element
		region = new Region(83, 0);
		returned = elementDetector.detectHyperlinks(
				editor.getSpecfileSourceViewer(), region, false);
		element = (SpecfileElementHyperlink) returned[0];
		assertTrue(element.getSource() instanceof SpecfileSource);
		source = (SpecfileSource) element.getSource();
		assertEquals(source.getSourceType(), SpecfileSource.SourceType.PATCH);
		assertEquals(source.getFileName(), "first.patch");

		// test define
		region = new Region(89, 0);
		returned = elementDetector.detectHyperlinks(
				editor.getSpecfileSourceViewer(), region, false);
		element = (SpecfileElementHyperlink) returned[0];
		assertTrue(element.getSource() instanceof SpecfileDefine);
		SpecfileDefine define = (SpecfileDefine) element.getSource();
		assertEquals(define.getName(), "smth");
		assertEquals(define.getStringValue(), "other");
	}

	@Test
	public void testDetectHyperlinksNoRegionAndTextViewer() {
		SpecfileElementHyperlinkDetector elementDetector = new SpecfileElementHyperlinkDetector();
		elementDetector.setSpecfile(specfile);
		IHyperlink[] returned = elementDetector.detectHyperlinks(null, null,
				false);
		assertNull(returned);
	}
}
