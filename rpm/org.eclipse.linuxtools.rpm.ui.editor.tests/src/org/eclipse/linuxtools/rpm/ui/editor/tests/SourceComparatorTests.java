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
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSource;

public class SourceComparatorTests extends TestCase {
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
	
	protected void setUp() throws Exception {
		testProject = new SpecfileTestProject();
		testFile = testProject.createFile("test.spec");
		parser = new SpecfileParser();
	}

	protected void tearDown() throws Exception {
		testProject.dispose();
	}
	public SourceComparatorTests(String name) {
		super(name);
	}

	public void testPatchComparator() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" +
		"Patch2: someotherfile.patch";
		
		try {
			newFile(specText);
			Collection patches = specfile.getPatchesAsList();
			int i = 1;
			for (Iterator iter = patches.iterator(); iter.hasNext();) {
				i++;
				SpecfileSource patch = (SpecfileSource) iter.next();
				if (i == 2)
					assertEquals(2, patch.getNumber());
				else if (i == 3)
					assertEquals(3, patch.getNumber());
				else
					fail();
			}
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testPatchComparator2() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" +
		"Patch2: someotherfile.patch";
		
		try {
			newFile(specText);
			SpecfileSource[] patches = specfile.getPatchesAsArray();
			assertEquals(2, patches[0].getNumber());
			assertEquals(3, patches[1].getNumber());
		} catch (Exception e) {
			fail();
		}
	}
}
