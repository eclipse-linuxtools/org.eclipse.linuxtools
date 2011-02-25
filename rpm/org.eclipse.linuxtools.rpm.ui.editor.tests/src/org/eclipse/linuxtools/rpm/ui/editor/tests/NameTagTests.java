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

public class NameTagTests extends FileTestCase {

	public void testResolvedNameTag() {

		String testText = "%define blah notblah\nName: %{blah}";

		newFile(testText);

		assertEquals("notblah", specfile.getName());
	}

	public void testResolvedSetName() {
		String testText = "%define blah notblah\nName: %{blah}";
		newFile(testText);
		assertEquals("notblah", specfile.getName());
	}

	public void testNameTag() {
		String testText = "Name: blah";
		newFile(testText);
		assertEquals("blah", specfile.getName());
	}

	public void testNameTag2() {
		String testText = "Name:	blah";
		newFile(testText);
		assertEquals("blah", specfile.getName());
	}

	public void testNullNameTag() {
		String testText = "Name:	";
		newFile(testText);
		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Name declaration without value.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}

	public void testNullNameTag2() {
		String testText = "Name: ";

		newFile(testText);
		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Name declaration without value.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}

	public void testMultipleNamesTag() {
		String testText = "Name: blah bleh";

		newFile(testText);
		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Name cannot have multiple values.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}

	public void testMultipleNamesTag2() {
		String testText = "Name: 	blah bleh";

		newFile(testText);
		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(testText.length(), marker
				.getAttribute(IMarker.CHAR_END, 0));
		assertEquals("Name cannot have multiple values.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}
}
