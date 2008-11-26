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
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSource;

public class RefactoringTests extends TestCase {

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
	public RefactoringTests(String name) {
		super(name);
	}
	
	public void testGetLine() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" +
		"%patch3";
		
		try {
			newFile(specText);
			assertEquals("%patch3", specfile.getLine(1));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testChangeLine() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" +
		"%patch3";
		
		try {
			newFile(specText);
			assertEquals("%patch3", specfile.getLine(1));
			specfile.changeLine(1, "%patch4");
			assertEquals("%patch4", specfile.getLine(1));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testChangeLine2() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" +
		"%patch3";
		
		try {
			newFile(specText);
			assertEquals("Patch3: somefilesomewhere.patch", specfile.getLine(0));
			specfile.changeLine(0, "Patch4: somefilesomewhere.patch");
			assertEquals("Patch4: somefilesomewhere.patch", specfile.getLine(0));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testChangeLine3() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" +
		"%patch3";
		
		try {
			newFile(specText);
			assertEquals("Patch3: somefilesomewhere.patch", specfile.getLine(0));
			specfile.changeLine(0, "andrew");
			assertEquals("andrew", specfile.getLine(0));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testOrganizePatches() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" +
		"%patch3";
		
		try {
			newFile(specText);
			assertEquals("Patch3: somefilesomewhere.patch", specfile.getLine(0));
			assertEquals("%patch3", specfile.getLine(1));
			assertEquals(0, specfile.getPatch(3).getLineNumber());
			SpecfileSource patch = specfile.getPatch(3);
			List<Integer> linesUsed = patch.getLinesUsed();
			assertEquals(1, linesUsed.size());
			Integer lineUsedNumber = linesUsed.get(0);
			assertEquals(1, lineUsedNumber.intValue());
			specfile.organizePatches();
			assertEquals("Patch0: somefilesomewhere.patch", specfile.getLine(0));
			assertEquals("%patch0", specfile.getLine(1));
			assertEquals(0, specfile.getPatch(0).getLineNumber());
			patch = specfile.getPatch(3);
			assertNull(patch);
			patch = specfile.getPatch(0);
			assertEquals(0, patch.getNumber());
			linesUsed = patch.getLinesUsed();
			assertEquals(1, linesUsed.size());
			lineUsedNumber = linesUsed.get(0);
			assertEquals(1, lineUsedNumber.intValue());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
