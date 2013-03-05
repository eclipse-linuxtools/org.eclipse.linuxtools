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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class handles the execution of the perf stat command
 * and stores the resulting data.
 */
public class StatData extends AbstractDataManipulator {

	private String prog;
	private String [] args;
	private int runCount;

	public StatData(String title, String prog, String [] args, int runCount) {
		super(title);
		this.prog = prog;
		this.args = args;
		this.runCount = runCount;
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

}
