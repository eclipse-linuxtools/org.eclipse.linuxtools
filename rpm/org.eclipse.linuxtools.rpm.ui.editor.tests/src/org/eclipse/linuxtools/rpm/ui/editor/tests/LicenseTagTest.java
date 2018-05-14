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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LicenseTagTest extends FileTestCase {
	@Test
	public void testSingleLicenseTag() {
		String testText = "License: EPL";
		newFile(testText);
		assertEquals("EPL", specfile.getLicense());
	}
}
