/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.tests;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCommand;

public class ValgrindStubCommand extends ValgrindCommand {

	@Override
	public String whichVersion(IProject project) {
		return "valgrind-3.4.0"; //$NON-NLS-1$
	}

	@Override
	public void execute(String[] commandArray, String[] env, File wd, boolean usePty, IProject project) {
		args = commandArray;
	}

	@Override
	public Process getProcess() {
		return null;
	}
}
