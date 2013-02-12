/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.perf.SourceDisassemblyData;

public class DataManipulatorTest extends TestCase {

	public void testEchoSourceDisassemblyData () {
		IPath path = new Path("/a/b/c/"); //$NON-NLS-1$
		StubSourceDisassemblyData sdData = new StubSourceDisassemblyData(
				"test data", path); //$NON-NLS-1$
		sdData.parse();

		String [] cmd = sdData.getCommand(path.toOSString());
		String expected = ""; //$NON-NLS-1$
		for (int i = 1; i < cmd.length; i++) {
			expected += cmd[i] + " "; //$NON-NLS-1$
		}

		assertEquals(expected.trim(), sdData.getPerfData().trim());
	}

	private class StubSourceDisassemblyData extends SourceDisassemblyData {

		public StubSourceDisassemblyData(String title, IPath workingDir) {
			super(title, workingDir);
		}

		@Override
		public String [] getCommand(String workingDir) {
			List<String> ret = new ArrayList<String> ();
			// return the same command with 'echo' prepended
			ret.add("echo"); //$NON-NLS-1$
			ret.addAll(Arrays.asList(super.getCommand(workingDir)));
			return ret.toArray(new String [0]);
		}
	}

}
