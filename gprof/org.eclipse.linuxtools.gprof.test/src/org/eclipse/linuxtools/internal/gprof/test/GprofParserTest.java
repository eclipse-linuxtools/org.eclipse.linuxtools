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
import static org.eclipse.linuxtools.internal.gprof.test.STJunitUtils.OUTPUT_FILE;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.internal.gprof.parser.GmonDecoder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GprofParserTest {
    public static Stream<Arguments> testDirs() {
        List<Arguments> params = new ArrayList<>();
        for (File testDir : STJunitUtils.getTestDirs()) {
            params.add(Arguments.of ( new File(testDir, OUTPUT_FILE),
                    new File(testDir, BINARY_FILE),
                    new File(testDir, "testParse.ref"),
                    new File(testDir, "testParse.dump") ));
        }
        return params.stream();
    }

    @ParameterizedTest @MethodSource("testDirs")
    public void testProcessGmonFile(File gmonFile, File binaryFile, File parserRefFile,
            File parserDumpFile) throws IOException {
        IBinaryObject binary = STSymbolManager.sharedInstance
                .getBinaryObject(binaryFile.getAbsolutePath());
        final GmonDecoder gmondecoder = new GmonDecoder(binary,
                new PrintStream(parserDumpFile), null);
        gmondecoder.setShouldDump(true);
        gmondecoder.read(gmonFile.getAbsolutePath());
        STJunitUtils.compareIgnoreEOL(parserDumpFile.getAbsolutePath(),
                parserRefFile.getAbsolutePath(), true);
    }
}
