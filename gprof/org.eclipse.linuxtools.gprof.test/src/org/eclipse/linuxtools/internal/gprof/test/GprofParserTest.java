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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.internal.gprof.parser.GmonDecoder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GprofParserTest {
	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> params = new ArrayList<Object[]>();
		for (File testDir : STJunitUtils.getTestDirs()) {
			params.add(new Object[] { new File(testDir, OUTPUT_FILE),
					new File(testDir, BINARY_FILE),
					new File(testDir, "testParse.ref"),
					new File(testDir, "testParse.dump") });
		}
		return params;
	}

	private File gmonFile;
	private File binaryFile;
	private File parserRefFile;
	private File parserDumpFile;

	public GprofParserTest(File gmonFile, File binaryFile, File parserRefFile,
			File parserDumpFile) {
		this.gmonFile = gmonFile;
		this.binaryFile = binaryFile;
		this.parserRefFile = parserRefFile;
		this.parserDumpFile = parserDumpFile;
	}

	@Test
	public void testProcessGmonFile() throws IOException {
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
