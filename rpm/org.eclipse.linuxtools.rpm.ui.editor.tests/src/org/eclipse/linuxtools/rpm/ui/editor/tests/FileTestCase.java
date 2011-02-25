/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;

/**
 * Test case providing all the objects needed for the rpm editor tests.
 * 
 */
public abstract class FileTestCase extends TestCase {

	protected SpecfileParser parser;
	protected Specfile specfile;
	protected IFile testFile;
	protected Document testDocument;
	SpecfileErrorHandler errorHandler;
	SpecfileTestProject testProject;

	@Override
	protected void setUp() throws CoreException {
		testProject = new SpecfileTestProject();
		testFile = testProject.createFile("test.spec");
		parser = new SpecfileParser();
		specfile = new Specfile();
	}

	@Override
	protected void tearDown() throws CoreException {
		testProject.dispose();
	}

	protected IMarker[] getFailureMarkers() {
		try {
			return testProject.getFailureMarkers();
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		return null;
	}

	protected void newFile(String contents) {
		try {
			testFile.setContents(new ByteArrayInputStream(contents.getBytes()),
					false, false, null);
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		testDocument = new Document(contents);
		errorHandler = new SpecfileErrorHandler(testFile, testDocument);
		parser.setErrorHandler(errorHandler);
		specfile = parser.parse(testDocument);
	}
}
