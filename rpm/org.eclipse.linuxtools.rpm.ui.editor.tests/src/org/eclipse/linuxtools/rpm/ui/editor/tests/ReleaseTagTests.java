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
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;

public class ReleaseTagTests extends TestCase {

	private SpecfileParser parser;
	private Specfile specfile;
	private IFile testFile;
	private Document testDocument;
	private SpecfileErrorHandler errorHandler;
	private SpecfileTestProject testProject;
	
	public ReleaseTagTests(String name) {
		super(name);
	}

	protected void newFile(String contents) throws Exception {
		testFile.setContents(new ByteArrayInputStream(contents.getBytes()), false, false, null);
		testDocument = new Document(contents);
		errorHandler = new SpecfileErrorHandler(testFile, testDocument);
		parser.setErrorHandler(errorHandler);
		specfile = parser.parse(testDocument);
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
	
	public void testResolvedSetRelease() {
		String testText = "%define blah notblah\nRelease: %{blah}";
		
		try {
			newFile(testText);
			assertEquals("notblah", specfile.getRelease());
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testReleaseTag() {
		String testText = "Release: blah";
		
		try {
			newFile(testText);
			
			assertEquals("blah", specfile.getRelease());
		} catch (Exception e) {
			fail();
		}
		
	}
	
	public void testReleaseTag2() {
		String testText = "Release:		blah";

		try {
			newFile(testText);
			
			assertEquals("blah", specfile.getRelease());
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testNullReleaseTag() {
		String testText = "Release:	";
		try {
			newFile(testText);

			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Release declaration without value.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testNullReleaseTag2() {
		String testText = "Release:		";

		try {
			newFile(testText);

			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Release declaration without value.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testMultipleReleasesTag() {
		String testText = "Release: blah bleh";
		try {
			newFile(testText);

			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Release cannot have multiple values.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testMultipleReleasesTag2() {
		String testText = "Release: 	blah bleh";

		try {
			newFile(testText);

			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Release cannot have multiple values.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}

}
