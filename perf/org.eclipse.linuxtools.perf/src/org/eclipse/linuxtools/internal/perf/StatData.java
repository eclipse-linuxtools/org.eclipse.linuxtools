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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.perf.handlers.PerfSaveStatsHandler;

/**
 * This class handles the execution of the perf stat command
 * and stores the resulting data.
 */
public class StatData extends AbstractDataManipulator {

	private String prog;
	private String [] args;
	private int runCount;
	private String [] events;

	public StatData(String title, File workDir, String prog, String [] args, int runCount, String[] events) {
		super(title, workDir);
		this.prog = prog;
		this.args = args;
		this.runCount = runCount;
		this.events = events;
	}

	@Override
	public void parse() {
		String [] cmd = getCommand(this.prog, this.args);
		// perf stat prints the data to standard error
		performCommand(cmd, 2);
	}

	protected String [] getCommand(String prog, String [] args) {
		List<String> ret = new ArrayList<String>(Arrays.asList(
				new String[] {"perf", "stat" })); //$NON-NLS-1$ //$NON-NLS-2$
		if (runCount > 1) {
			ret.add("-r"); //$NON-NLS-1$
			ret.add(String.valueOf(runCount));
		}
		if (events != null) {
			for (String event : events) {
				ret.add("-e"); //$NON-NLS-1$
				ret.add(event);
			}
		}
		ret.add(prog);
		ret.addAll(Arrays.asList(args));
		return ret.toArray(new String [0]);
	}

	protected String getProgram () {
		return prog;
	}

	protected String [] getArguments () {
		return args;
	}

	/**
	 * Save latest perf stat result under $workingDirectory/perf.stat. If file
	 * already exists rename it to perf.old.stat, in order to keep a reference
	 * to the previous session and be consistent with the way perf handles perf
	 * report data files.
	 */
	public void updateStatData() {

		// build file name format
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(PerfPlugin.PERF_COMMAND);
		stringBuilder.append("%s."); //$NON-NLS-1$
		stringBuilder.append(PerfSaveStatsHandler.DATA_EXT);
		String statNameFormat = stringBuilder.toString();

		// get current stat file
		Path workingDir = new Path(getWorkDir().getAbsolutePath());
		String curStatName = String.format(statNameFormat, ""); //$NON-NLS-1$
		IPath curStatPath = workingDir.append(curStatName);
		File curStatFile = new File(curStatPath.toOSString());

		if (curStatFile.exists()) {
			// get previous stat file
			String oldStatName = String.format(statNameFormat, ".old"); //$NON-NLS-1$
			IPath oldStatPath = workingDir.append(oldStatName);
			File oldStatFile = oldStatPath.toFile();

			if (oldStatFile.exists()) {
				oldStatFile.delete();
			}

			curStatFile.renameTo(oldStatFile);
		}

		PerfSaveStatsHandler saveStats = new PerfSaveStatsHandler();
		saveStats.saveData(PerfPlugin.PERF_COMMAND);
	}

}
