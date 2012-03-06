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

import org.eclipse.linuxtools.gprof.Activator;
import org.eclipse.linuxtools.internal.gprof.utils.GprofProgramChecker;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class GprofBinaryTest extends TestCase {

	private static final String GMON_DIRECTORY_SUFFIX = "_gprof_input";
	private static final String GMON_BINARY_FILE = "a.out"; 
	private static final String GMON_OUTPUT_FILE = "gmon.out"; 

	public GprofBinaryTest() {
	}

	public static Test suite() {
		TestSuite ats = new TestSuite("Test Binary Consistency");
		File[] testDirs = STJunitUtils.getTestDirs("org.eclipse.linuxtools.internal.gprof.test", ".*" + GMON_DIRECTORY_SUFFIX);
		for (File testDir : testDirs) {
			final String dirName = testDir.getName();
			ats.addTest(
					new TestCase(dirName + ":BinaryChecker") {
						public void runTest() throws Throwable {
							testValidBinary(dirName+File.separator+GMON_BINARY_FILE);
						}
					}
			);

//			ats.addTest(
//					new TestCase(dirName + ":GmonChecker") {
//						public void runTest() throws Throwable {
//							testInvalidBinary(dirName+"\\"+GMON_OUTPUT_FILE);
//						}
//					}
//			);
		}	
		return ats;
	}

	public static void testValidBinary(String relativeBinaryPath) throws Exception {
		String binary = STJunitUtils.getAbsolutePath(Activator.PLUGIN_ID , relativeBinaryPath);
		//Assert.assertEquals(true, GprofProgramChecker.isGProfCompatible(binary));
		// enhance coverage: testing cache
//		new File(binary).setLastModified(System.currentTimeMillis());
//		Assert.assertEquals(true, GprofProgramChecker.isGProfCompatible(binary));

	}

	public static void testInvalidBinary(String relativeGmonPath) throws Exception {
		String binary = STJunitUtils.getAbsolutePath(Activator.PLUGIN_ID , relativeGmonPath);
		//Assert.assertEquals(false, GprofProgramChecker.isGProfCompatible(binary));
	}
}

