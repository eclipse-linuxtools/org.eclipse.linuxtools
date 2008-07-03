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
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSource;

public class LinePositionTests extends TestCase {

	private SpecfileParser parser;
	private Specfile specfile;
	private IFile testFile;
	private Document testDocument;
	private SpecfileErrorHandler errorHandler;
	private SpecfileTestProject testProject;

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
	public LinePositionTests(String name) {
		super(name);
	}
	
	public void testLineNumber() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" +
		"Patch2: someotherfile.patch";
		
		try {
			newFile(specText);
			Collection<SpecfileSource> patches = specfile.getPatchesAsList();
			for (SpecfileSource patch: patches) {
				if (patch.getNumber() == 2)
					assertEquals(1, patch.getLineNumber());
				else if (patch.getNumber() == 3)
					assertEquals(0, patch.getLineNumber());
				else
					fail();
			}
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testLineNumber2() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" +
		"%patch3";
		
		try {
			newFile(specText);
			Collection<SpecfileSource> patches = specfile.getPatchesAsList();
			for (SpecfileSource patch: patches) {
				if (patch.getNumber() == 3) {
					assertEquals(0, patch.getLineNumber());
					assertEquals(1, patch.getLinesUsed().get(0).intValue()); 
				} else
					fail();
			}
		} catch (Exception e) {
			fail();
		}
	}

}
