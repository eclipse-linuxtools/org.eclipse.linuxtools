/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class VersionTagTest extends FileTestCase {
	@Test
	public void testResolvedSetVersion() {
		String testText = "%define blah notblah\nVersion: %{blah}";
		newFile(testText);
		assertEquals("notblah", specfile.getVersion());
	}

	@Test
	public void testVersionTag() {
		String testText = "Version: blah";
		newFile(testText);
		assertEquals("blah", specfile.getVersion());
	}

	@Test
	public void testVersionTag2() {
		String testText = "Version:        blah";
		newFile(testText);
		assertEquals("blah", specfile.getVersion());
	}

	@Test
	public void testNullVersionTag() {
		String testText = "Version: ";
		newFile(testText);

		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(), failure.getPosition().getLength());
		assertEquals("Version declaration without value.", failure.getAnnotation().getText());
	}

	@Test
	public void testNullVersionTag2() {
		String testText = "Version:        ";

		newFile(testText);

		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(), failure.getPosition().getLength());
		assertEquals("Version declaration without value.", failure.getAnnotation().getText());
	}

	@Test
	public void testMultipleVersionsTag() {
		String testText = "Version: blah bleh";
		newFile(testText);

		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(), failure.getPosition().getLength());
		assertEquals("Version cannot have multiple values.", failure.getAnnotation().getText());
	}

	@Test
	public void testMultipleVersionsTag2() {
		String testText = "Version:     blah bleh";

		newFile(testText);

		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(), failure.getPosition().getLength());
		assertEquals("Version cannot have multiple values.", failure.getAnnotation().getText());
	}

}
