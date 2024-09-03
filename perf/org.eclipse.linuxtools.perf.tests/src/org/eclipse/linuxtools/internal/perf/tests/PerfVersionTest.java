/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alexander Kurtakov <akurtako@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.linuxtools.internal.perf.PerfVersion;
import org.junit.jupiter.api.Test;

public class PerfVersionTest {

	@Test
	public void testPerfVersionString() {
		PerfVersion version = new PerfVersion("4.2.3.300.fc23.x86_64.g21b8");
		assertEquals(4, version.getMajor());
		assertEquals(2, version.getMinor());
		assertEquals(3, version.getMicro());
		assertEquals("300.fc23.x86_64.g21b8", version.getQualifier());
	}

	@Test
	public void testPerfVersionIntIntInt() {
		PerfVersion version = new PerfVersion(4, 2, 3);
		assertEquals(4, version.getMajor());
		assertEquals(2, version.getMinor());
		assertEquals(3, version.getMicro());
		assertEquals("", version.getQualifier());

		version = new PerfVersion("4.0.8-200.fc21.x86_64");
		assertEquals(4, version.getMajor());
		assertEquals(0, version.getMinor());
		assertEquals(8, version.getMicro());
		assertEquals("200.fc21.x86_64", version.getQualifier());

		version = new PerfVersion("4.0.8");
		assertEquals(4, version.getMajor());
		assertEquals(0, version.getMinor());
		assertEquals(8, version.getMicro());
		assertEquals("", version.getQualifier());
	}

	@Test
	public void testIsNewer() {
		PerfVersion version = new PerfVersion("4.2.3.300.fc23.x86_64.g21b8");
		assertTrue(version.isNewer(new PerfVersion(4, 2, 2)));
		assertFalse(version.isNewer(new PerfVersion(4, 2, 4)));

		assertTrue(version.isNewer(new PerfVersion(4, 1, 3)));
		assertFalse(version.isNewer(new PerfVersion(4, 2, 3)));

		assertTrue(version.isNewer(new PerfVersion(3, 2, 3)));
		assertFalse(version.isNewer(new PerfVersion(5, 2, 3)));
	}

	@Test
	public void testToString() {
		String versionString = "4.2.3.300.fc23.x86_64.g21b8";
		PerfVersion version = new PerfVersion(versionString);
		assertTrue(versionString.equals(version.toString()));
	}

}
