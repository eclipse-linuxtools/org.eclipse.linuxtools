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

import java.io.File;

/**
 * Class for handling launch of perf diff command and storing of
 * the resulting data.
 */
public class ReportComparisonData extends AbstractDataManipulator {

	private File oldFile;
	private File newFile;

	public ReportComparisonData(String title, File oldFile, File newFile) {
		super(title, null);
		this.oldFile = oldFile;
		this.newFile = newFile;
	}

	@Override
	public void parse() {
		performCommand(getCommand(), 1);
	}

	/**
	 * Get perf diff command to execute.
	 *
	 * @return String array representing command to execute.
	 */
	protected String[] getCommand() {
		return new String[] { PerfPlugin.PERF_COMMAND,
				"diff", //$NON-NLS-1$
				oldFile.getAbsolutePath(),
				newFile.getAbsolutePath() };
	}

}
