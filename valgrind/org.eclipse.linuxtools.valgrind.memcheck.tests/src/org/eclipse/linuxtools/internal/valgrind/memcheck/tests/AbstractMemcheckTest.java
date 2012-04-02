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
package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;

import org.eclipse.linuxtools.internal.valgrind.memcheck.MemcheckPlugin;
import org.eclipse.linuxtools.internal.valgrind.tests.AbstractValgrindTest;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public abstract class AbstractMemcheckTest extends AbstractValgrindTest {

	@Override
	public Bundle getBundle() {
		return FrameworkUtil.getBundle(AbstractMemcheckTest.class);
	}
	
	public String getToolID() {
		return MemcheckPlugin.TOOL_ID;
	}

}
