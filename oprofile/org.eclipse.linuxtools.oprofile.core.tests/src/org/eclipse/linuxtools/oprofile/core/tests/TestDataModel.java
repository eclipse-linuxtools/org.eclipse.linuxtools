/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.core.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSession;
import org.eclipse.linuxtools.oprofile.tests.TestingOpModelRoot;
import org.junit.Before;
import org.junit.Test;

public class TestDataModel {
	private TestingOpModelRoot _testRoot;

	@Before
	public void setUp() {
		_testRoot = new TestingOpModelRoot();
		_testRoot.refreshModel();
	}

	@Test
	public void testParse() {
		OpModelSession[] sessions = _testRoot.getSessions();
		assertEquals(3, sessions.length);
		assertEquals(TestingOpModelRoot.NAME_E1, sessions[0].getName());
		assertEquals(TestingOpModelRoot.NAME_E2, sessions[1].getName());

		OpModelEvent[] e1_sessions = sessions[0].getEvents(), e2_sessions = sessions[1].getEvents();
		assertEquals(1, e1_sessions.length);
		assertEquals(4, e2_sessions.length);

		assertEquals(205000, e1_sessions[0].getCount());
		assertEquals(205000, e2_sessions[0].getCount());
		assertEquals(200000, e2_sessions[1].getCount());
		assertEquals(OpModelImage.IMAGE_PARSE_ERROR, e2_sessions[2].getCount());
		assertEquals(0, e2_sessions[3].getCount());

		assertEquals(TestingOpModelRoot.NAME_E1_S1, e1_sessions[0].getName());
		assertEquals(TestingOpModelRoot.NAME_E2_S1, e2_sessions[0].getName());
		assertEquals(TestingOpModelRoot.NAME_E2_S2, e2_sessions[1].getName());
		assertEquals(TestingOpModelRoot.NAME_E2_S3, e2_sessions[2].getName());
		assertEquals(TestingOpModelRoot.NAME_E2_S4, e2_sessions[3].getName());

		//further image parsing is tested in the TestModelDataParse testParse
	}

	@Test
	public void testStringOutput() {
		assertEquals(TestingOpModelRoot.ROOT_OUTPUT, _testRoot.toString());
	}
}
