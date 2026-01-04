/*******************************************************************************
 * Copyright (c) 2009, 2026 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.test;

import static org.eclipse.linuxtools.internal.gprof.test.STJunitUtils.BINARY_FILE;
import static org.eclipse.linuxtools.internal.gprof.test.STJunitUtils.OUTPUT_FILE;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Stream;

import org.eclipse.linuxtools.internal.gprof.utils.Aggregator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class GprofAggregatorTest {

    public static Stream<String> testDirs() {
    	return Arrays.stream(STJunitUtils.getTestDirs()).map(p -> p.getName());
    }

    @ParameterizedTest @MethodSource("testDirs")
    public void testAggregation(String dir) throws IOException, InterruptedException {
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

        Process p = new ProcessBuilder().command(gprof2use, binary, f.getAbsolutePath()).start();
		Process p2 = new ProcessBuilder().command(gprof2use, binary, directory + File.separator + "gmon.sum.ref")
				.start();

        STJunitUtils.compare(p.getInputStream(), p2.getInputStream());
        p.waitFor();
        p2.waitFor();
    }
}
