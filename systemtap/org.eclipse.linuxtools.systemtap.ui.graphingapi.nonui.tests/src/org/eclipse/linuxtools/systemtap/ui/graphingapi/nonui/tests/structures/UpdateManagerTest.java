package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures;

import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IUpdateListener;
import org.eclipse.linuxtools.systemtap.ui.structures.UpdateManager;

import junit.framework.TestCase;

public class UpdateManagerTest extends TestCase {
	public UpdateManagerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		manager = new UpdateManager(5);
	}

	public void testStop() {
		assertTrue(manager.isRunning());
		manager.stop();
		assertFalse(manager.isRunning());
	}
	
	public void testAddUpdateListener() {
		manager.addUpdateListener(listener);
	}
	
	public void testRemoveUpdateListener() {
		manager.addUpdateListener(listener);
		manager.removeUpdateListener(listener);
	}
	
	public void testIsRunning() {
		assertTrue(manager.isRunning());
		manager.stop();
		assertFalse(manager.isRunning());
	}
	
	public void testDispose() {
		manager.dispose();
		assertFalse(manager.isRunning());
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		manager.stop();
		manager.dispose();
	}
	
	private UpdateManager manager;
	private IUpdateListener listener = new IUpdateListener() {
		public void handleUpdateEvent() {
			//Do nothing;
		}
	};
}
