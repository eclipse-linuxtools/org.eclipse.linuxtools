/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package com.st.stgcov.test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.gcov.Activator;
import org.eclipse.linuxtools.gcov.parser.CovManager;

public class STgcovGCDARetrieverTest extends TestCase {

	public STgcovGCDARetrieverTest() {
	}

	public static Test suite() {
		TestSuite ats = new TestSuite("STGcov:GCDA_Retriever");
		File[] testDirs = STJunitUtils.getTestDirs(Activator.PLUGIN_ID + ".test", "test.*");
		for (File testDir : testDirs) {	
			final String[] covFiles = STGcovTestUtils.getGCDANames(testDir);
			final File binaryFile = STGcovTestUtils.getBinary(testDir);
			ats.addTest(
					new TestCase(testDir.getName() + ":GCDA_Retriever") {
						public void runTest() throws Throwable {
							testGcdaRetriever(binaryFile, covFiles);
						}
					}
			);
		}	
		return ats;
	}


	public static void testGcdaRetriever(
			File binaryFile, String[] covFilesPaths)
	throws Exception {
		CovManager covManager = new CovManager(binaryFile.getAbsolutePath());
		List<String> list = covManager.getGCDALocations();
		String[] generatedCovFilesPath = new String[list.size()];
		int index = 0;
		for (String string : list) {
			int i = string.lastIndexOf('/');
			string = string.substring(i+1);
			i = string.lastIndexOf('\\');
			string = string.substring(i+1);
			generatedCovFilesPath[index] = string;
			index++;
		}
		Arrays.sort(generatedCovFilesPath);
		boolean b = Arrays.equals(generatedCovFilesPath, covFilesPaths);
		Assert.assertEquals("May be normal if binary not complied with gcov options", true, b);
	}

}