/*******************************************************************************
 * Copyright (c) 2010, 2018 IBM Corporation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
    private IUpdateListener listener = () -> {
	    //Do nothing;
	};
}
