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

import static org.eclipse.linuxtools.internal.gprof.test.STJunitUtils.BINARY_FILE;
import static org.eclipse.linuxtools.internal.gprof.test.STJunitUtils.OUTPUT_FILE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.internal.gprof.utils.Aggregator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
@RunWith(Parameterized.class)
public class GprofAggregatorTest {

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> params = new ArrayList<>();
		for (File testDir : STJunitUtils.getTestDirs()) {
			final String dirName = testDir.getName();
			params.add(new Object[] { dirName });
		}
		return params;
	}

	private String dir;

	public GprofAggregatorTest(String dir) {
		this.dir = dir;
	}

	@Test
	public void testAggregation() throws IOException, InterruptedException {
		File directory = new File(STJunitUtils.getAbsolutePath(
				"org.eclipse.linuxtools.gprof.test", dir));
		File gmonPath = new File(STJunitUtils.getAbsolutePath(
				"org.eclipse.linuxtools.gprof.test", dir + File.separator
						+ OUTPUT_FILE));
		File binaryPath = new File(STJunitUtils.getAbsolutePath(
				"org.eclipse.linuxtools.gprof.test", dir + File.separator
						+ BINARY_FILE));

		String gmon = gmonPath.toString();
		String binary = binaryPath.toString();

		LinkedList<String> s = new LinkedList<>();
		s.add(gmon.toString());
		s.add(gmon.toString());

		String gprof2use = "gprof";
		File f = Aggregator.aggregate(gprof2use, binary, s, directory);

		Process p = Runtime.getRuntime().exec(
				new String[] { gprof2use, binary, f.getAbsolutePath() });
		Process p2 = Runtime.getRuntime().exec(
				new String[] { gprof2use, binary,
						directory + File.separator + "gmon.sum.ref" });

		STJunitUtils.compare(p.getInputStream(), p2.getInputStream());
		p.waitFor();
		p2.waitFor();
	}
}
