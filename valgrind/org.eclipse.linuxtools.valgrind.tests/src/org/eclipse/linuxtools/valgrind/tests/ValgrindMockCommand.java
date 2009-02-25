/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.tests;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;

public class ValgrindMockCommand extends ValgrindCommand {
	protected int exitcode;
	
	public ValgrindMockCommand(int exitcode) {
		this.exitcode = exitcode;
	}
	
	@Override
	public String whichValgrind() throws IOException {
		return "/path/to/valgrind"; //$NON-NLS-1$
	}
	
	@Override
	public void execute(String[] commandArray, String[] env, File wd,
			boolean usePty) throws IOException {
		args = commandArray;
	}
	
	@Override
	public Process getProcess() {
		return new ValgrindMockProcess(exitcode);
	}
}
