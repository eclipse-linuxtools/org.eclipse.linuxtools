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
package org.eclipse.linuxtools.gcov.test;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.gcov.Activator;
import org.eclipse.linuxtools.gcov.parser.CovManager;

public class GcovParserTest extends TestCase {

	public GcovParserTest() {
	}


	public static Test suite() {
		TestSuite ats = new TestSuite("Gcov:Parser");
		File[] testDirs = STJunitUtils.getTestDirs(Activator.PLUGIN_ID + ".test", "test.*");
		for (File testDir : testDirs) {
			final List<String> covPaths = GcovTestUtils.getGCDAPath(testDir);
			final File binaryFile = GcovTestUtils.getBinary(testDir);
			final File parserRefFile = new File(testDir, "testProcessCovFiles.ref");
			final File parserDumpFile = new File(testDir, "testProcessCovFiles.dump");
			ats.addTest(
					new TestCase(testDir.getName() + ":Parser") {
						public void runTest() throws Throwable {
							testGcovParsing(binaryFile, covPaths, parserRefFile, parserDumpFile);
						}
					}
			);
		}	
		return ats;
	}

	public static void testGcovParsing(
			File binaryFile, List<String> covFilesPaths,
			File parserRefFile, File parserDumpFile)
	throws Exception {
		CovManager covManager = new CovManager(binaryFile.getAbsolutePath());
		covManager.processCovFiles(covFilesPaths, null);
		covManager.dumpProcessCovFilesResult(new PrintStream(parserDumpFile));
		STJunitUtils.compareIgnoreEOL(parserDumpFile.getAbsolutePath(), parserRefFile.getAbsolutePath(), true);
	}

}