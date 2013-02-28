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

import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;

/**
 * This class represents the general flow of a perf command being
 * set up, executed, and having its data collected.
 */
public abstract class AbstractDataManipulator {

	private String text;
	private String title;

	AbstractDataManipulator (String title) {
		this.title = title;
	}

	public String getPerfData() {
		return text;
	}

	public String getTitle () {
		return title;
	}

	public void performCommand(String[] cmd, int fd) {
		try {
			Process proc = RuntimeProcessFactory.getFactory().exec(cmd, null);

			BufferedReader buff;
			switch (fd) {
			case 1:
				buff = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				break;
			case 2:
				buff = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				break;
			default:
				buff = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			}

			StringBuffer strBuff = new StringBuffer();
			String line = ""; //$NON-NLS-1$
			while ((line = buff.readLine()) != null) {
				strBuff.append(line);
				strBuff.append("\n"); //$NON-NLS-1$
			}
			text = strBuff.toString();
		} catch (IOException e) {
			text = ""; //$NON-NLS-1$
		}
	}

	/**
	 * A combination of setting up the command to run and executing it.
	 * This often calls performCommand(String [] cmd).
	 */
	public abstract void parse();

}
