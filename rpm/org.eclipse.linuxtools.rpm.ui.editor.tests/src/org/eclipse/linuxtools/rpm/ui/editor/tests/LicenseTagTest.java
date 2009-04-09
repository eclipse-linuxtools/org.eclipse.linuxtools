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

public class LicenseTagTest extends FileTestCase {

	public void testSingleLicenseTag() {
		String testText = "License: EPL";
		newFile(testText);
		assertEquals("EPL", specfile.getLicense());
	}
	// FIXME: investigate whether we should keep this or not
	// See SpecfileParser at line 450
	// public void testMultipleLicenseTag() {
	// String testText = "License: Eclipse Public License";
	// try {
	// newFile(testText);
	// IMarker marker= testProject.getFailureMarkers()[0];
	// assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
	// assertEquals(31, marker.getAttribute(IMarker.CHAR_END, 0));
	// assertEquals(IMarker.SEVERITY_WARNING,
	// marker.getAttribute(IMarker.SEVERITY, -1));
	// assertEquals("License should be an acronym.",
	// marker.getAttribute(IMarker.MESSAGE, ""));
	// } catch (Exception e) {
	// e.printStackTrace();
	// fail();
	// }
	// }
}
