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
package org.eclipse.linuxtools.valgrind.cachegrind.tests;

import org.eclipse.linuxtools.valgrind.cachegrind.CachegrindPlugin;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.linuxtools.valgrind.tests.AbstractValgrindTest;
import org.osgi.framework.Bundle;

public abstract class AbstractCachegrindTest extends AbstractValgrindTest {

	@Override
	protected Bundle getBundle() {
		return CachegrindTestsPlugin.getDefault().getBundle();
	}

	@Override
	protected String getToolID() {
		return CachegrindPlugin.TOOL_ID;
	}

	@Override
	protected IValgrindToolPage getToolPage() {
		return new CachegrindTestToolPage();
	}

	protected CachegrindFile getFileByName(CachegrindOutput output, String name) {
		CachegrindFile file = null;
		for (CachegrindFile f : output.getFiles()) {
			if (f.getName().equals(name)) {
				file = f;
			}
		}
		return file;
	}

}
