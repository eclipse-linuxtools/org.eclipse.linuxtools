/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.tests;


public class ReleaseTagTest extends FileTestCase {

	public void testResolvedSetRelease() {
		String testText = "%define blah notblah\nRelease: %{blah}";

		newFile(testText);
		assertEquals("notblah", specfile.getRelease());
	}

	public void testReleaseTag() {
		String testText = "Release: blah";
		newFile(testText);
		assertEquals("blah", specfile.getRelease());
	}

	public void testReleaseTag2() {
		String testText = "Release:		blah";
		newFile(testText);
		assertEquals("blah", specfile.getRelease());
	}

	public void testNullReleaseTag() {
		String testText = "Release:	";
		newFile(testText);

		SpecfileTestFailure failure = getFailures()[0];
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(),
				failure.getPosition().getLength());
		assertEquals("Release declaration without value.",
				failure.getAnnotation().getText());
	}

	public void testNullReleaseTag2() {
		String testText = "Release:		";

		newFile(testText);

		SpecfileTestFailure failure = getFailures()[0];
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(),
				failure.getPosition().getLength());
		assertEquals("Release declaration without value.",
				failure.getAnnotation().getText());
	}

	public void testMultipleReleasesTag() {
		String testText = "Release: blah bleh";
		newFile(testText);

		SpecfileTestFailure failure = getFailures()[0];
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(),
				failure.getPosition().getLength());
		assertEquals("Release cannot have multiple values.",
				failure.getAnnotation().getText());
	}

	public void testMultipleReleasesTag2() {
		String testText = "Release: 	blah bleh";
		newFile(testText);

		SpecfileTestFailure failure = getFailures()[0];
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(testText.length(),
				failure.getPosition().getLength());
		assertEquals("Release cannot have multiple values.",
				failure.getAnnotation().getText());
	}
}
