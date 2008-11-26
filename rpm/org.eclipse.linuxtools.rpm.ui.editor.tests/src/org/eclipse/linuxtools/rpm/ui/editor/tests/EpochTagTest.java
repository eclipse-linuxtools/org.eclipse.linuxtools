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

public class EpochTagTest extends TestCase {

	private SpecfileTestProject testProject;
	private SpecfileParser parser;
	private Specfile specfile;
	private SpecfileErrorHandler errorHandler;
	private IFile testFile;
	private Document testDocument;

	public EpochTagTest(String name) {
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
	
	public void testEpochTag() {
		try {
			String testText = "Epoch: 1";
			newFile(testText);
			assertEquals(1, specfile.getEpoch());
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testEpochTag2() {
		String testText = "Epoch:	1";
		try {
			newFile(testText);
			assertEquals(1, specfile.getEpoch());
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testNullEpochTag() {
		String testText = "Epoch:";
		try {
			newFile(testText);
			IMarker marker= testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(6, marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(IMarker.SEVERITY, -1));
			assertEquals("Epoch declaration without value.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testNullEpochTag2() {
		String testText = "Epoch:	";
		try {
			newFile(testText);
			IMarker marker= testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(7, marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(IMarker.SEVERITY, -1));
			assertEquals("Epoch declaration without value.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testMultipleEpochsTag() {
		String testText = "Epoch: 1 2";
		try {
			newFile(testText);
			IMarker marker= testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(10, marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(IMarker.SEVERITY, -1));
			assertEquals("Epoch cannot have multiple values.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testMultipleEpochsTag2() {
		String testText = "Epoch: 	1 2";
		try {
			newFile(testText);
			IMarker marker= testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(11, marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(IMarker.SEVERITY, -1));
			assertEquals("Epoch cannot have multiple values.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testNonIntegerEpoch() {
		String testText = "Epoch: blah";
		try {
			newFile(testText);
			IMarker marker= testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(11, marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(IMarker.SEVERITY, -1));
			assertEquals("Epoch cannot have non-integer value.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testNonIntegerEpoch2() {
		String testText = "Epoch:	blah";
		try {
			newFile(testText);
			IMarker marker= testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(11, marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(IMarker.SEVERITY, -1));
			assertEquals("Epoch cannot have non-integer value.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}

}
