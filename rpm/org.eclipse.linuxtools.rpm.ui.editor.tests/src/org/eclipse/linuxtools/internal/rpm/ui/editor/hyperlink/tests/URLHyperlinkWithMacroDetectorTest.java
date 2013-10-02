/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.URLHyperlinkWithMacroDetector;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.Test;

public class URLHyperlinkWithMacroDetectorTest extends FileTestCase {

	@Test
	public void testDetectHyperlinks() throws PartInitException {
		String testText = "Name: eclipse\nURL: http://www.%{name}.org/";
		newFile(testText);
		URLHyperlinkWithMacroDetector macroDetector = new URLHyperlinkWithMacroDetector();
		macroDetector.setSpecfile(specfile);
		IRegion region = new Region(20, 0);
		IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage(), testFile,
				"org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");

		editor = (SpecfileEditor) openEditor;
		editor.doRevertToSaved();
		IHyperlink[] returned = macroDetector.detectHyperlinks(
				editor.getSpecfileSourceViewer(), region, false);
		URLHyperlink url = (URLHyperlink) returned[0];
		assertEquals("http://www.eclipse.org/", url.getURLString());
	}
}