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
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.hyperlink.URLHyperlinkWithMacroDetector;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.ui.IEditorPart;

public class HyperlinkWithMacroTests extends TestCase {

	private SpecfileTestProject testProject;
	private SpecfileParser parser;
	private Specfile specfile;
	private SpecfileErrorHandler errorHandler;
	private SpecfileEditor editor;
	private IFile testFile;
	private Document testDocument;
	private URLHyperlinkWithMacroDetector macroDetector;

	public HyperlinkWithMacroTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		testProject = new SpecfileTestProject();
		testFile = testProject.createFile("test.spec");
		parser = new SpecfileParser();
	}

	@Override
	protected void tearDown() throws Exception {
		testProject.dispose();
	}

	protected void newFile(String contents) throws Exception {
		testFile.setContents(new ByteArrayInputStream(contents.getBytes()), true, false, null);
		testDocument = new Document(contents);
		errorHandler = new SpecfileErrorHandler(testFile, testDocument);
		parser.setErrorHandler(errorHandler);
		specfile = parser.parse(testDocument);
	}

	public void testMacroResolutionInUrl() {
		try {
			String testText = "Name: eclipse\nURL: http://www.%{name}.org/";
			newFile(testText);
			macroDetector = new URLHyperlinkWithMacroDetector(specfile);
			IRegion region = new Region(20, 0);
			IEditorPart openEditor = org.eclipse.ui.ide.IDE.openEditor(
					Activator.getDefault().getWorkbench()
							.getActiveWorkbenchWindow().getActivePage(),
					testFile,
					"org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");

			editor = (SpecfileEditor) openEditor;
			editor.doRevertToSaved();
			IHyperlink[] returned = macroDetector.detectHyperlinks(editor
					.getSpecfileSourceViewer(), region, false);
			URLHyperlink url = (URLHyperlink) returned[0];
			assertEquals("http://www.eclipse.org/", url.getURLString());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}