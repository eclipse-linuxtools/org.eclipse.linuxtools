/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.test;

import java.io.File;

import org.osgi.framework.FrameworkUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class GprofBinaryTest extends TestCase {

	private static final String GMON_DIRECTORY_SUFFIX = "_gprof_input";
	private static final String GMON_BINARY_FILE = "a.out";
	public GprofBinaryTest() {
	}

	public static Test suite() {
		TestSuite ats = new TestSuite("Test Binary Consistency");
		File[] testDirs = STJunitUtils.getTestDirs("org.eclipse.linuxtools.gprof.test", ".*" + GMON_DIRECTORY_SUFFIX);
		for (File testDir : testDirs) {
			final String dirName = testDir.getName();
			ats.addTest(
					new TestCase(dirName + ":BinaryChecker") {
						@Override
						public void runTest() {
							testValidBinary(dirName+File.separator+GMON_BINARY_FILE);
						}
					}
			);

		}
		return ats;
	}

	public static void testValidBinary(String relativeBinaryPath) {
		@SuppressWarnings("unused")
		String binary = STJunitUtils.getAbsolutePath(FrameworkUtil.getBundle(GprofBinaryTest.class).getSymbolicName(), relativeBinaryPath);
	}

	public static void testInvalidBinary(String relativeGmonPath) {
		@SuppressWarnings("unused")
		String binary = STJunitUtils.getAbsolutePath(FrameworkUtil.getBundle(GprofBinaryTest.class).getSymbolicName(), relativeGmonPath);
	}
}

