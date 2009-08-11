/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row.RowParser;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures.ChartStreamDaemon;

import junit.framework.TestCase;

public class ChartStreamDaemonTest extends TestCase {
	public ChartStreamDaemonTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();

		csd = new ChartStreamDaemon(null, null);
		assertNotNull(csd);

		csd1 = new ChartStreamDaemon(new RowDataSet(new String[] {"a"}), new RowParser(new String[] {"\\w", "\\s"}));
		assertNotNull(csd1);
	}
	
	public void testHandleEvent() {
		csd.handleDataEvent("a a a");
		csd1.handleDataEvent("a a a");
	}
	
	public void testIsDisposed() {
		assertFalse(csd1.isDisposed());
	}
	
	public void testDispose() {
		assertFalse(csd1.isDisposed());
		csd1.dispose();
		assertTrue(csd1.isDisposed());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private ChartStreamDaemon csd, csd1;
}