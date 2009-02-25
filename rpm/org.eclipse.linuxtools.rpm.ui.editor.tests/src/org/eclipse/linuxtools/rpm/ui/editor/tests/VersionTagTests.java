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

public class VersionTagTests extends FileTestCase {

	public void testResolvedSetVersion() {
		String testText = "%define blah notblah\nVersion: %{blah}";
		newFile(testText);
		assertEquals("notblah", specfile.getVersion());
	}

	public void testVersionTag() {
		String testText = "Version: blah";
		newFile(testText);
		assertEquals("blah", specfile.getVersion());
	}

	public void testVersionTag2() {
		String testText = "Version:		blah";
		newFile(testText);
		assertEquals("blah", specfile.getVersion());
	}

	public void testNullVersionTag() {
		String testText = "Version: ";
		newFile(testText);

		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Version declaration without value.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}

	public void testNullVersionTag2() {
		String testText = "Version:		";

		newFile(testText);

		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Version declaration without value.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}

	public void testMultipleVersionsTag() {
		String testText = "Version: blah bleh";
		newFile(testText);

		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Version cannot have multiple values.", marker
				.getAttribute(IMarker.MESSAGE, ""));
	}

	public void testMultipleVersionsTag2() {
		String testText = "Version: 	blah bleh";

		newFile(testText);

		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Version cannot have multiple values.", marker
				.getAttribute(IMarker.MESSAGE, ""));
	}

}
