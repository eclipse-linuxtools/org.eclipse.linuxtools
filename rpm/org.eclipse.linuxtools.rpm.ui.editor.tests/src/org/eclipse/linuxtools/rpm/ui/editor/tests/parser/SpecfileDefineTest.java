/*******************************************************************************
 * Copyright (c) 2007, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.tests.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpm.ui.editor.UiUtils;
import org.eclipse.linuxtools.rpm.core.utils.RPMQuery;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.eclipse.linuxtools.rpm.ui.editor.tests.SpecfileTestFailure;
import org.junit.Before;
import org.junit.Test;

public class SpecfileDefineTest extends FileTestCase {

	private String testSpec =
	// Characters 0 through 17
	"%define blah bleh" + "\n" +
	// Characters 18 through 36
			"%define blah2	bleh" + "\n" +
			// Characters 37 through 52
			"%define blah3	1" + "\n" +
			// Characters 53 through 69
			"%define blah4		1" + "\n" +
			// Characters 70 through 83
			"%define blah5" + "\n" +
			// Characters 84 through 95
			"%define 1 2" + "\n" +
			// Characters 96 through 109
			"%define -n -p" + "\n" +
			// Characters 110 through 144
			"%define __find_requires %{SOURCE3}" + "\n" +
			// Characters 145 through 180
			"%global version_suffix 201302130906" + "\n" +
			// Characters 181 through 195
			"Version: 2.3.0" + "\n";

	@Override
	@Before
	public void setUp() throws CoreException {
		super.setUp();
		newFile(testSpec);
		specfile = parser.parse(testDocument);
	}

	@Test
	public void testResolve() {
		SpecfileDefine define1 = new SpecfileDefine("name", "testspec",
				specfile, specfile.getPackages().getPackage(specfile.getName()));
		specfile.addDefine(define1);
		assertEquals("testspec", ((SpecfileElement) define1).resolve("%{name}"));
	}

	@Test
	public void testDefine() {
		SpecfileDefine blahDefine = specfile.getDefine("blah");
		assertEquals(SpecfileDefine.class, blahDefine.getClass());
		assertEquals("blah", blahDefine.getName());
		assertEquals("bleh", blahDefine.getStringValue());
	}

	@Test
	public void testDefine2() {
		SpecfileDefine blahDefine = specfile.getDefine("blah2");
		assertEquals(SpecfileDefine.class, blahDefine.getClass());
		assertEquals("blah2", blahDefine.getName());
		assertEquals("bleh", blahDefine.getStringValue());
	}

	@Test
	public void testDefine3() {
		SpecfileDefine blahDefine = specfile.getDefine("blah3");
		assertEquals(SpecfileDefine.class, blahDefine.getClass());
		assertEquals("blah3", blahDefine.getName());
		assertEquals(1, blahDefine.getIntValue());
	}

	@Test
	public void testDefine4() {
		SpecfileDefine blahDefine = specfile.getDefine("blah4");
		assertEquals(SpecfileDefine.class, blahDefine.getClass());
		assertEquals("blah4", blahDefine.getName());
		assertEquals(1, blahDefine.getIntValue());
	}

	@Test
	public void testNonLetterDefinitionName() {
		boolean fail = true;
		for (SpecfileTestFailure failure : getFailures()) {
			if ((failure.getPosition().getOffset() == 84)
					&& (failure.getPosition().getLength() == 11)) {
				assertEquals(SpecfileErrorHandler.ANNOTATION_ERROR, 
						failure.getAnnotation().getType());
				assertEquals(
						"Definition lvalue must begin with a letter or an underscore.",
						failure.getAnnotation().getText());
				fail = false;
			}
		}
		if (fail) {
			fail();
		}
	}
	@Test
	public void testNonLetterDefinitionName2() {
		boolean fail = true;
		for (SpecfileTestFailure failure : getFailures()) {
			if ((failure.getPosition().getOffset() == 96)
					&& (failure.getPosition().getLength() == 13)) {
				assertEquals(SpecfileErrorHandler.ANNOTATION_ERROR, 
						failure.getAnnotation().getType());
				assertEquals(
						"Definition lvalue must begin with a letter or an underscore.",
						failure.getAnnotation().getText());
				fail = false;
			}
		}
		if (fail) {
			fail();
		}
	}

	@Test
	public void testUnderscoreDefine() {
		SpecfileDefine blahDefine = specfile.getDefine("__find_requires");
		assertEquals(SpecfileDefine.class, blahDefine.getClass());
		assertEquals("__find_requires", blahDefine.getName());
		assertEquals("%{SOURCE3}", blahDefine.getStringValue());
	}

	@Test
	public void testWholeWordResolveDefine() {
		String testResolve = UiUtils.resolveDefines(specfile, "%{version}.%{version_suffix}");
		assertEquals(testResolve, "2.3.0.201302130906");
	}

	@Test
	public void testResolveSCLMacro() throws CoreException {
		String specText = "Name: %{?scl_prefix}eclipse-jgit" + "\n" + "%{name}";
		newFile(specText);
		assertEquals("eclipse-jgit", RPMQuery.eval(specfile.getName()).trim());
	}

}
