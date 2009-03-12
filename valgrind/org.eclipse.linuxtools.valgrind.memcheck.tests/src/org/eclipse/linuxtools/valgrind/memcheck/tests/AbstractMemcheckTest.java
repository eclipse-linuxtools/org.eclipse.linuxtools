/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
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
import org.eclipse.linuxtools.valgrind.memcheck.MemcheckPlugin;
import org.eclipse.linuxtools.valgrind.tests.AbstractValgrindTest;
import org.osgi.framework.Bundle;

public abstract class AbstractMemcheckTest extends AbstractValgrindTest {

	@Override
	public Bundle getBundle() {
		return MemcheckTestsPlugin.getDefault().getBundle();
	}
	
	public String getToolID() {
		return MemcheckPlugin.TOOL_ID;
	}

	@Override
	protected IValgrindToolPage getToolPage() {
		return new MemcheckTestToolPage();
	}

}
