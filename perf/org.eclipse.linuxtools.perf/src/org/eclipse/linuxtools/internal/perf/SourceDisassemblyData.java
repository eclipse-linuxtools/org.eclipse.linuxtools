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
package org.eclipse.linuxtools.internal.perf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;

/**
 * This class handles the execution of the source disassembly command
 * and stores the resulting data.
 */
public class SourceDisassemblyData {

	private String sourceDisassemblyText;
	private String title;
	private IPath workingDir;

	public SourceDisassemblyData (String title, IPath workingDir) {
		this.title = title;
		this.workingDir = workingDir;
	}

	public void parse() {
		String [] cmd = getCommand(workingDir.toOSString());
		performCommand(cmd);
	}

	public String getSourceDisassemblyText() {
		return sourceDisassemblyText;
	}

	public String getTitle () {
		return title;
	}

	private void performCommand(String[] cmd) {
		try {
			Process proc = RuntimeProcessFactory.getFactory().exec(cmd, null);
			BufferedReader buff = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			StringBuffer strBuff = new StringBuffer();
			String line = ""; //$NON-NLS-1$
			while ((line = buff.readLine()) != null) {
				strBuff.append(line + "\n"); //$NON-NLS-1$
			}
			sourceDisassemblyText = strBuff.toString();
		} catch (IOException e) {
			sourceDisassemblyText = ""; //$NON-NLS-1$
		}
	}

	private String [] getCommand(String workingDir) {
		return new String[] { "perf", "annotate", //$NON-NLS-1$ //$NON-NLS-2$
				"-i", workingDir + "perf.data" }; //$NON-NLS-1$ //$NON-NLS-2$
	}

}