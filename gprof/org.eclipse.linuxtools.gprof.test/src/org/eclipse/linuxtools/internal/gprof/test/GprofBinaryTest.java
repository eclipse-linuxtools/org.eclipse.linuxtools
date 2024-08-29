/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
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

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.osgi.framework.FrameworkUtil;

public class GprofBinaryTest {

    public static Stream<String> testDirs() {
        return Arrays.stream(STJunitUtils.getTestDirs()).map(p -> p.getName()+File.separator+BINARY_FILE);
    }

    @ParameterizedTest @MethodSource("testDirs")
    public void testValidBinary(String path) {
        STJunitUtils.getAbsolutePath(FrameworkUtil.getBundle(GprofBinaryTest.class).getSymbolicName(), path);
    }

}

