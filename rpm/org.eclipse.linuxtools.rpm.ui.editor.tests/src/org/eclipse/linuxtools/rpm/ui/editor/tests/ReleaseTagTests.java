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

import org.eclipse.core.resources.IMarker;

public class ReleaseTagTests extends FileTestCase {

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

		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Release declaration without value.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}

	public void testNullReleaseTag2() {
		String testText = "Release:		";

		newFile(testText);

		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Release declaration without value.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}

	public void testMultipleReleasesTag() {
		String testText = "Release: blah bleh";
		newFile(testText);

		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Release cannot have multiple values.", marker
				.getAttribute(IMarker.MESSAGE, ""));
	}

	public void testMultipleReleasesTag2() {
		String testText = "Release: 	blah bleh";
		newFile(testText);

		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Release cannot have multiple values.", marker
				.getAttribute(IMarker.MESSAGE, ""));
	}
}
