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
import java.io.IOException;
import java.util.LinkedList;

import org.eclipse.linuxtools.internal.gprof.utils.Aggregator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class GprofAggregatorTest extends TestCase {

	private static final String GMON_DIRECTORY_SUFFIX = "_gprof_input";
	private static final String GMON_BINARY_FILE = "a.out";
	private static final String GMON_OUTPUT_FILE = "gmon.out";

	public static Test suite() {
		TestSuite ats = new TestSuite("Test Aggregation");
		File[] testDirs = STJunitUtils.getTestDirs("org.eclipse.linuxtools.gprof.test", ".*" + GMON_DIRECTORY_SUFFIX);
		for (File testDir : testDirs) {
			final String dirName = testDir.getName();
			ats.addTest(
					new TestCase(dirName + ":Aggregate") {
						@Override
						public void runTest() throws Throwable {
							testAggregation(dirName);
						}
					}
			);
		}
		return ats;
	}

	public static void testAggregation(String dir) throws IOException, InterruptedException  {
		File directory = new File (STJunitUtils.getAbsolutePath("org.eclipse.linuxtools.gprof.test", dir));
		File gmonPath = new File (STJunitUtils.getAbsolutePath("org.eclipse.linuxtools.gprof.test", dir+File.separator+GMON_OUTPUT_FILE));
		File binaryPath = new File (STJunitUtils.getAbsolutePath("org.eclipse.linuxtools.gprof.test", dir+File.separator+GMON_BINARY_FILE));

		String gmon = gmonPath.toString();
		String binary = binaryPath.toString();

		LinkedList<String> s = new LinkedList<String>();
		s.add(gmon.toString());
		s.add(gmon.toString());

		String gprof2use="gprof";
		File f = Aggregator.aggregate(gprof2use, binary, s, directory);

		Process p = Runtime.getRuntime().exec(new String[] {gprof2use, binary, f.getAbsolutePath()});
		Process p2 = Runtime.getRuntime().exec(new String[] {gprof2use, binary, directory + File.separator + "gmon.sum.ref"});

		STJunitUtils.compare(p.getInputStream(), p2.getInputStream());
		p.waitFor();
		p2.waitFor();
	}
}
