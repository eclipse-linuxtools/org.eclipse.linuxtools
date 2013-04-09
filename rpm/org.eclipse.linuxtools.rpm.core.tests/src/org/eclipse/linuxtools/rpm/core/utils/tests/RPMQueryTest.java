/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.utils.tests;

import static org.junit.Assert.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpm.core.utils.RPMQuery;
import org.junit.Test;

public class RPMQueryTest {

	@Test
	public void testEval() throws CoreException {
		//check eval for string without macro
		assertEquals("should be same", RPMQuery.eval("should be same").trim());
		//check eval for macro only
		assertEquals("/usr/share", RPMQuery.eval("%{_datadir}").trim());
		//check eval for macro and string
		assertEquals("/usr/share/eclipse", RPMQuery.eval("%{_datadir}/eclipse").trim());
		//check eval for conditional undefined macro
		assertEquals("eclipse", RPMQuery.eval("%{?scl_prefix}eclipse").trim());
	}

}
