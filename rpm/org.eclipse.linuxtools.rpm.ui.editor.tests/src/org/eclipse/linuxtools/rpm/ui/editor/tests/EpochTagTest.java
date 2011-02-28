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

public class EpochTagTest extends FileTestCase {

	public void testEpochTag() {
		String testText = "Epoch: 1";
		newFile(testText);
		assertEquals(1, specfile.getEpoch());
	}

	public void testEpochTag2() {
		String testText = "Epoch:	1";
		newFile(testText);
		assertEquals(1, specfile.getEpoch());
	}

	public void testNullEpochTag() {
		String testText = "Epoch:";
		newFile(testText);
		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(6, marker.getAttribute(IMarker.CHAR_END, 0));
		assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(
				IMarker.SEVERITY, -1));
		assertEquals("Epoch declaration without value.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}

	public void testNullEpochTag2() {
		String testText = "Epoch:	";
		newFile(testText);
		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(7, marker.getAttribute(IMarker.CHAR_END, 0));
		assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(
				IMarker.SEVERITY, -1));
		assertEquals("Epoch declaration without value.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}

	public void testMultipleEpochsTag() {
		String testText = "Epoch: 1 2";
		newFile(testText);
		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(10, marker.getAttribute(IMarker.CHAR_END, 0));
		assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(
				IMarker.SEVERITY, -1));
		assertEquals("Epoch cannot have multiple values.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}

	public void testMultipleEpochsTag2() {
		String testText = "Epoch: 	1 2";
		newFile(testText);
		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(11, marker.getAttribute(IMarker.CHAR_END, 0));
		assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(
				IMarker.SEVERITY, -1));
		assertEquals("Epoch cannot have multiple values.", marker.getAttribute(
				IMarker.MESSAGE, ""));
	}

	public void testNonIntegerEpoch() {
		String testText = "Epoch: blah";
		newFile(testText);
		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(11, marker.getAttribute(IMarker.CHAR_END, 0));
		assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(
				IMarker.SEVERITY, -1));
		assertEquals("Epoch cannot have non-integer value.", marker
				.getAttribute(IMarker.MESSAGE, ""));
	}

	public void testNonIntegerEpoch2() {
		String testText = "Epoch:	blah";
		newFile(testText);
		IMarker marker = getFailureMarkers()[0];
		assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
		assertEquals(11, marker.getAttribute(IMarker.CHAR_END, 0));
		assertEquals(IMarker.SEVERITY_ERROR, marker.getAttribute(
				IMarker.SEVERITY, -1));
		assertEquals("Epoch cannot have non-integer value.", marker
				.getAttribute(IMarker.MESSAGE, ""));
	}
}
