/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.core.tests.structures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.systemtap.structures.UpdateManager;
import org.eclipse.linuxtools.systemtap.structures.listeners.IUpdateListener;
import org.junit.Before;
import org.junit.Test;

public class UpdateManagerTest {
	@Before
	public void setUp() {
		manager = new UpdateManager(5);
	}
	@Test
	public void testStop() {
		assertTrue(manager.isRunning());
		manager.stop();
		assertFalse(manager.isRunning());
	}
	@Test
	public void testAddUpdateListener() {
		manager.addUpdateListener(listener);
	}
	@Test
	public void testRemoveUpdateListener() {
		manager.addUpdateListener(listener);
		manager.removeUpdateListener(listener);
	}
	@Test
	public void testIsRunning() {
		assertTrue(manager.isRunning());
		manager.stop();
		assertFalse(manager.isRunning());
	}
	@Test
	public void testDispose() {
		manager.dispose();
		assertFalse(manager.isRunning());
	}
	
	private UpdateManager manager;
	private IUpdateListener listener = new IUpdateListener() {
		@Override
		public void handleUpdateEvent() {
			//Do nothing;
		}
	};
}
