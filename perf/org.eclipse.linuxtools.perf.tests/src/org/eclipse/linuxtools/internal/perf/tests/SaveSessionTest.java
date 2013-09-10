/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.StatData;
import org.eclipse.linuxtools.internal.perf.handlers.AbstractSaveDataHandler;
import org.eclipse.linuxtools.internal.perf.handlers.PerfSaveSessionHandler;
import org.eclipse.linuxtools.internal.perf.handlers.PerfSaveStatsHandler;
import org.junit.After;
import org.junit.Test;

public class SaveSessionTest {
	private static final String WORKING_DIR = "resources/"; //$NON-NLS-1$
	private static final String DATA_FILE_PATH = "/mock/data/path"; //$NON-NLS-1$
	private static final String PERF_DATA_FILE_PATH = "resources/perf.data"; //$NON-NLS-1$
	private static final String PERF_STATS_FILE_PATH = "stat_data"; //$NON-NLS-1$
	private static final String DATA_FILE_NAME = "data"; //$NON-NLS-1$
	private static final String DATA_FILE_EXT = "ext"; //$NON-NLS-1$
	private ArrayList<File> testFiles = new ArrayList<File>();

	@After
	public void tearDown(){
		for (File file : testFiles) {
			if(!file.delete()){
				fail();
			}
		}
	}

	@Test
	public void testGenericHandler() {
		GenericSaveDataHandler handler = new GenericSaveDataHandler();
		assertTrue(handler.canSave(new File(DATA_FILE_PATH)));
		assertEquals(WORKING_DIR, handler.getWorkingDir().toOSString());

		IPath path = handler.getNewDataLocation(DATA_FILE_NAME, DATA_FILE_EXT);
		assertEquals(WORKING_DIR + DATA_FILE_NAME + '.' + DATA_FILE_EXT, //$NON-NLS-1$
				path.toOSString());

		assertTrue(handler.isEnabled());
		assertTrue(handler.isHandled());
	}
	@Test
	public void testPerfSaveSessionHandler() {
		PerfSaveSessionTestHandler handler = new PerfSaveSessionTestHandler();

		PerfPlugin.getDefault().setPerfProfileData(null);
		assertFalse(handler.verifyData());

		PerfPlugin.getDefault().setPerfProfileData(
				new Path(PERF_DATA_FILE_PATH));
		assertTrue(handler.verifyData());

		File data = handler.saveData(DATA_FILE_NAME);
		assertNotNull(data);
		testFiles.add(data);

	}

	// mock handlers
	@Test
	public void testPerfSaveStatsHandler() {
		PerfSaveStatsTestHandler handler = new PerfSaveStatsTestHandler();

		PerfPlugin.getDefault().setStatData(null);
		assertFalse(handler.verifyData());

		PerfPlugin.getDefault().setStatData(
				new StatData("title", null, "prog", new String[] {}, 1, null) { //$NON-NLS-1$ //$NON-NLS-2$
					@Override
					public String getPerfData() {
						return PERF_STATS_FILE_PATH;
					}
				});
		assertTrue(handler.verifyData());

		File stats = handler.saveData(DATA_FILE_NAME);
		assertNotNull(stats);

		testFiles.add(stats);
	}

	private static class GenericSaveDataHandler extends AbstractSaveDataHandler {
		@Override
		public Object execute(ExecutionEvent event) {
			return null;
		}

		@Override
		public File saveData(String filename) {
			return null;
		}

		@Override
		public boolean verifyData() {
			return true;
		}

		@Override
		protected IPath getWorkingDir() {
			return new Path(WORKING_DIR);
		}
	}

	private static class PerfSaveSessionTestHandler extends PerfSaveSessionHandler {
		@Override
		protected IPath getWorkingDir() {
			return new Path(WORKING_DIR);
		}
	}

	private static class PerfSaveStatsTestHandler extends PerfSaveStatsHandler {
		@Override
		protected IPath getWorkingDir() {
			return new Path(WORKING_DIR);
		}
	}

}
