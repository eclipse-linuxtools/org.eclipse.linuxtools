/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.StatData;
import org.eclipse.linuxtools.internal.perf.handlers.AbstractSaveDataHandler;
import org.eclipse.linuxtools.internal.perf.handlers.PerfSaveSessionHandler;
import org.eclipse.linuxtools.internal.perf.handlers.PerfSaveStatsHandler;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.FrameworkUtil;

public class SaveSessionTest extends AbstractTest {
    private static final String WORKING_DIR = "resources/"; //$NON-NLS-1$
    private static final String DATA_FILE_PATH = "/mock/data/path"; //$NON-NLS-1$
    private static final String PERF_DATA_FILE_PATH = "resources/perf.data"; //$NON-NLS-1$
    private static final String PERF_STATS_FILE_PATH = "stat_data"; //$NON-NLS-1$
    private static final String DATA_FILE_NAME = "data"; //$NON-NLS-1$
    private static final String DATA_FILE_EXT = "ext"; //$NON-NLS-1$
    private ArrayList<IPath> testFiles = new ArrayList<>();
    private IProject proj;

    @BeforeEach
    public void setUp() {
        try {
            proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), "fibTest").getProject();
            PerfPlugin.getDefault().setWorkingDir(proj.getLocation());
        } catch (InvocationTargetException | CoreException | URISyntaxException | InterruptedException | IOException e) {
            e.printStackTrace();
            fail("Failed to create test project");
        }
    }

    @AfterEach
    public void tearDown(){
        for (IPath f : testFiles) {
            File file = f.toFile();
            assertTrue(file.delete());
        }
    }

    @Test
    public void testGenericHandler() {
        GenericSaveDataHandler handler = new GenericSaveDataHandler();
        assertTrue(handler.canSave(Path.fromOSString(DATA_FILE_PATH)));
        assertEquals(WORKING_DIR, handler.getWorkingDir().toOSString());

        IPath path = handler.getNewDataLocation(DATA_FILE_NAME, DATA_FILE_EXT);
        assertEquals(WORKING_DIR + DATA_FILE_NAME + '.' + DATA_FILE_EXT,
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

        IPath data = handler.saveData(DATA_FILE_NAME);
        assertNotNull(data);
        assertTrue(!data.toFile().canWrite());
        testFiles.add(data);

    }

    // mock handlers
    @Test
    public void testPerfSaveStatsHandler() {
        PerfSaveStatsTestHandler handler = new PerfSaveStatsTestHandler();

        PerfPlugin.getDefault().setStatData(null);
        assertFalse(handler.verifyData());

        PerfPlugin.getDefault().setStatData(
                new StatData("title", null, "prog", new String[] {}, 1, null, proj) { //$NON-NLS-1$ //$NON-NLS-2$
                    @Override
                    public String getPerfData() {
                        return PERF_STATS_FILE_PATH;
                    }
                });
        assertTrue(handler.verifyData());

        IPath stats = handler.saveData(DATA_FILE_NAME);
        assertNotNull(stats);
        assertTrue(!stats.toFile().canWrite());

        testFiles.add(stats);
    }

    private static class GenericSaveDataHandler extends AbstractSaveDataHandler {
        @Override
        public Object execute(ExecutionEvent event) {
            return null;
        }

        @Override
        public IPath saveData(String filename) {
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

    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return null;
    }

    @Override
    protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
    }

}
