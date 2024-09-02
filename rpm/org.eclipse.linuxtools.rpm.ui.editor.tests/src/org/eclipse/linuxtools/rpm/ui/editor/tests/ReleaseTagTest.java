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

public class ReleaseTagTest extends FileTestCase {
	@Test
	public void testResolvedSetRelease() {
		String testText = "%define blah notblah\nRelease: %{blah}";
		newFile(testText);
		assertEquals("notblah", specfile.getRelease());
	}

	@Test
	public void testReleaseTag() {
		String testText = "Release: blah";
		newFile(testText);
		assertEquals("blah", specfile.getRelease());
	}

	@Test
	public void testReleaseTag2() {
		String testText = "Release:        blah";
		newFile(testText);
		assertEquals("blah", specfile.getRelease());
	}

	@Test
	public void testNullReleaseTag() {
		String testText = "Release:    ";
		newFile(testText);
		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(), failure.getPosition().getLength());
		assertEquals("Release declaration without value.", failure.getAnnotation().getText());
	}

	@Test
	public void testNullReleaseTag2() {
		String testText = "Release:        ";
		newFile(testText);
		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(), failure.getPosition().getLength());
		assertEquals("Release declaration without value.", failure.getAnnotation().getText());
	}

	@Test
	public void testMultipleReleasesTag() {
		String testText = "Release: blah bleh";
		newFile(testText);
		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(), failure.getPosition().getLength());
		assertEquals("Release cannot have multiple values.", failure.getAnnotation().getText());
	}

	@Test
	public void testMultipleReleasesTag2() {
		String testText = "Release:     blah bleh";
		newFile(testText);
		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(), failure.getPosition().getLength());
		assertEquals("Release cannot have multiple values.", failure.getAnnotation().getText());
	}
}
