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
		BufferedReader buffData = null;
		BufferedReader buffTmp = null;

		try {
			Process proc = RuntimeProcessFactory.getFactory().exec(cmd, null);
			StringBuffer strBuffData = new StringBuffer();
			StringBuffer strBuffTmp = new StringBuffer();
			String line = ""; //$NON-NLS-1$

			switch (fd) {
			case 1:
				buffData = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				buffTmp = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

				// If the buffer is not being consumed, the other one may block.
				while ((line = buffData.readLine()) != null) {
					strBuffData.append(line);
					strBuffData.append("\n"); //$NON-NLS-1$
				}

				while ((line = buffTmp.readLine()) != null) {
					strBuffTmp.append(line);
					strBuffTmp.append("\n"); //$NON-NLS-1$
				}
				break;
			case 2:
				buffData = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				buffTmp = new BufferedReader(new InputStreamReader(proc.getInputStream()));

				// If the buffer is not being consumed, the other one may block.
				while ((line = buffTmp.readLine()) != null) {
					strBuffTmp.append(line);
					strBuffTmp.append("\n"); //$NON-NLS-1$
				}

				while ((line = buffData.readLine()) != null) {
					strBuffData.append(line);
					strBuffData.append("\n"); //$NON-NLS-1$
				}
				break;
			default:
				buffData = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				buffTmp = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

				// If the buffer is not being consumed, the other one may block.
				while ((line = buffData.readLine()) != null) {
					strBuffData.append(line);
					strBuffData.append("\n"); //$NON-NLS-1$
				}

				while ((line = buffTmp.readLine()) != null) {
					strBuffTmp.append(line);
					strBuffTmp.append("\n"); //$NON-NLS-1$
				}
			}

			text = strBuffData.toString();
		} catch (IOException e) {
			text = ""; //$NON-NLS-1$
		} finally {
			try {
				if (buffData != null) {
					buffData.close();
				}
				if (buffTmp != null) {
					buffTmp.close();
				}
			} catch (IOException e) {
				// continue
			}
		}
	}

	/**
	 * A combination of setting up the command to run and executing it.
	 * This often calls performCommand(String [] cmd).
	 */
	public abstract void parse();

}
