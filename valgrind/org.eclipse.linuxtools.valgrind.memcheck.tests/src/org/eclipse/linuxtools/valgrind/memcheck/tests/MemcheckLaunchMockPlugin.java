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
package org.eclipse.linuxtools.valgrind.memcheck.tests;

import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.linuxtools.valgrind.tests.ValgrindLaunchMockPlugin;

public class MemcheckLaunchMockPlugin extends ValgrindLaunchMockPlugin {

	@Override
	public IValgrindToolPage substitutePage() {
		return new MemcheckTestToolPage();
	}

}
