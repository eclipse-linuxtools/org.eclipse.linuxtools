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
import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.internal.gprof.parser.GmonDecoder;


public class GprofParserTest extends TestCase{
	private static final String GMON_BINARY_FILE = "a.out";
	private static final String GMON_OUTPUT_FILE = "gmon.out";
	private static final String GMON_DIRECTORY_SUFFIX = "_gprof_input";

	public static Test suite() {
		TestSuite ats = new TestSuite("Test gmon Parser");
		File[] testDirs = STJunitUtils.getTestDirs("org.eclipse.linuxtools.gprof.test", ".*" + GMON_DIRECTORY_SUFFIX);
		for (File testDir : testDirs) {
			final File logFile = new File(testDir, GMON_OUTPUT_FILE);
			final File binaryFile = new File(testDir, GMON_BINARY_FILE);
			final File parserRefFile = new File(testDir, "testParse.ref");
			final File parserDumpFile = new File(testDir, "testParse.dump");
			ats.addTest(
					new TestCase(testDir.getName() + ":Parser") {
						@Override
						public void runTest() throws Throwable {
							testProcessGmonFile(logFile, binaryFile, parserRefFile, parserDumpFile);
						}
					}
			);
		}
		return ats;
	}

	public static void testProcessGmonFile(File gmonFile, File binaryFile, File parserRefFile, File parserDumpFile) throws Exception {
		IBinaryObject binary = STSymbolManager.sharedInstance.getBinaryObject(binaryFile.getAbsolutePath());
		final GmonDecoder gmondecoder = new GmonDecoder(binary, new PrintStream(parserDumpFile), null);
		gmondecoder.setShouldDump(true);
		gmondecoder.read(gmonFile.getAbsolutePath());
		STJunitUtils.compareIgnoreEOL(parserDumpFile.getAbsolutePath(), parserRefFile.getAbsolutePath(), true);
	}
}
