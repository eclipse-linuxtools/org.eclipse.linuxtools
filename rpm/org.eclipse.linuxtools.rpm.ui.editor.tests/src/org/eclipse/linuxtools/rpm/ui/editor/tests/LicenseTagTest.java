/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.tests;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;

public class LicenseTagTest extends TestCase {
	private SpecfileTestProject testProject;
	private SpecfileParser parser;
	private Specfile specfile;
	private SpecfileErrorHandler errorHandler;
	private IFile testFile;
	private Document testDocument;

	public LicenseTagTest(String name) {
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
		testFile.setContents(new ByteArrayInputStream(contents.getBytes()), false, false, null);
		testDocument = new Document(contents);
		errorHandler = new SpecfileErrorHandler(testFile, testDocument);
		parser.setErrorHandler(errorHandler);
		specfile = parser.parse(testDocument);
	}
	
	public void testSingleLicenseTag() {
		try {
			String testText = "License: EPL";
			newFile(testText);
			assertEquals("EPL", specfile.getLicense());
		} catch (Exception e) {
			fail();
		}
	}
// FIXME:  investigate whether we should keep this or not
// See SpecfileParser at line 450
//	public void testMultipleLicenseTag() {
//		String testText = "License: Eclipse Public License";
//		try {
//			newFile(testText);
//			IMarker marker= testProject.getFailureMarkers()[0];
//			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
//			assertEquals(31, marker.getAttribute(IMarker.CHAR_END, 0));
//			assertEquals(IMarker.SEVERITY_WARNING, marker.getAttribute(IMarker.SEVERITY, -1));
//			assertEquals("License should be an acronym.", marker.getAttribute(IMarker.MESSAGE, ""));
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail();
//		}
//	}
}
