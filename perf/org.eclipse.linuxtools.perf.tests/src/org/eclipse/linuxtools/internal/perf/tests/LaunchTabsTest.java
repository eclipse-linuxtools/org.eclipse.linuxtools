/*******************************************************************************
 * (C) Copyright 2010, 2018 IBM Corp.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.PerfVersion;
import org.eclipse.linuxtools.internal.perf.launch.PerfEventsTab;
import org.eclipse.linuxtools.internal.perf.launch.PerfOptionsTab;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.FrameworkUtil;

public class LaunchTabsTest extends AbstractTest {
    private ILaunchConfiguration config;
    private Shell testShell;

    @BeforeEach
    public void setUp() throws Exception {
        proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), "fibTest"); //$NON-NLS-1$
        config = createConfiguration(proj.getProject());
        testShell = new Shell(Display.getDefault());
        testShell.setLayout(new GridLayout());
    }

    @AfterEach
    public void tearDown() throws Exception {
        testShell.dispose();
        deleteProject(proj);
    }

    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(PerfPlugin.LAUNCHCONF_ID);
    }

    @Override
    protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
        PerfEventsTab eventsTab = new PerfEventsTab();
        PerfOptionsTab optionsTab = new PerfOptionsTab();
        eventsTab.setDefaults(wc);
        optionsTab.setDefaults(wc);
    }


    //getter functions for otherwise unaccessible member variables
    private static class TestOptionsTab extends PerfOptionsTab {
        protected Text getTxtKernelLocation() { return txtKernelLocation; }
        protected Button getChkRecordRealtime() { return chkRecordRealtime; }
        protected Button getChkRecordVerbose() { return chkRecordVerbose; }
        protected Button getChkSourceLineNumbers() { return chkSourceLineNumbers; }
        protected Button getChkKernelSourceLineNumbers() { return chkKernelSourceLineNumbers; }
        protected Button getChkMultiplexEvents() { return chkMultiplexEvents; }
        protected Button getChkModuleSymbols() { return chkModuleSymbols; }
        protected Button getChkHideUnresolvedSymbols() { return chkHideUnresolvedSymbols; }
    }
    @Test
    public void testOptionsTab() throws CoreException {
        ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
        TestOptionsTab tab = new TestOptionsTab();
        tab.createControl(new Shell());
        assertNotNull(tab.getImage());
        assertNotNull(tab.getName());

        //default config
        tab.setDefaults(wc);
        tab.initializeFrom(config);
        assertTrue(tab.isValid(config));

        Button rrCheck = tab.getChkRecordRealtime();
        rrCheck.setSelection(true);
        rrCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(true, config.getAttribute(PerfPlugin.ATTR_Record_Realtime, false));
        rrCheck.setSelection(false);
        rrCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(false, config.getAttribute(PerfPlugin.ATTR_Record_Realtime, true));

        Button rvCheck = tab.getChkRecordVerbose();
        rvCheck.setSelection(true);
        rvCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(true, config.getAttribute(PerfPlugin.ATTR_Record_Verbose, false));
        rvCheck.setSelection(false);
        rvCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(false, config.getAttribute(PerfPlugin.ATTR_Record_Verbose, true));

        Button slcCheck = tab.getChkSourceLineNumbers();
        slcCheck.setSelection(true);
        slcCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(true, config.getAttribute(PerfPlugin.ATTR_SourceLineNumbers, false));
        slcCheck.setSelection(false);
        slcCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(false, config.getAttribute(PerfPlugin.ATTR_SourceLineNumbers, true));

        Button kslcCheck = tab.getChkKernelSourceLineNumbers();
        kslcCheck.setSelection(true);
        kslcCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(true, config.getAttribute(PerfPlugin.ATTR_Kernel_SourceLineNumbers, false));
        kslcCheck.setSelection(false);
        kslcCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(false, config.getAttribute(PerfPlugin.ATTR_Kernel_SourceLineNumbers, true));

        PerfVersion version = PerfCore.getPerfVersion(config);
        Button meCheck = tab.getChkMultiplexEvents();
        if (meCheck.isEnabled()) {
            assertTrue (version != null && new PerfVersion(2, 6, 35).isNewer(version));
            meCheck.setSelection(true);
            meCheck.notifyListeners(SWT.Selection, null);
            performApply(tab, wc);
            assertEquals(true, config.getAttribute(PerfPlugin.ATTR_Multiplex, false));
            meCheck.setSelection(false);
            meCheck.notifyListeners(SWT.Selection, null);
            performApply(tab, wc);
            assertEquals(false, config.getAttribute(PerfPlugin.ATTR_Multiplex, true));
        } else {
            assertTrue (version == null || !new PerfVersion(2, 6, 35).isNewer(version));
        }

        Button msCheck = tab.getChkModuleSymbols();
        msCheck.setSelection(true);
        msCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(true, config.getAttribute(PerfPlugin.ATTR_ModuleSymbols, false));
        msCheck.setSelection(false);
        msCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(false, config.getAttribute(PerfPlugin.ATTR_ModuleSymbols, true));

        Button husCheck = tab.getChkHideUnresolvedSymbols();
        husCheck.setSelection(true);
        husCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(true, config.getAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, false));
        husCheck.setSelection(false);
        husCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertEquals(false, config.getAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, true));

        rrCheck.setSelection(true);
        rrCheck.notifyListeners(SWT.Selection, null);
        rvCheck.setSelection(true);
        rvCheck.notifyListeners(SWT.Selection, null);
        slcCheck.setSelection(true);
        slcCheck.notifyListeners(SWT.Selection, null);
        kslcCheck.setSelection(true);
        kslcCheck.notifyListeners(SWT.Selection, null);
        meCheck.setSelection(true);
        meCheck.notifyListeners(SWT.Selection, null);
        msCheck.setSelection(true);
        msCheck.notifyListeners(SWT.Selection, null);
        husCheck.setSelection(true);
        husCheck.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        tab.initializeFrom(config);
        assertTrue(rrCheck.getSelection());
        assertTrue(rvCheck.getSelection());
        assertTrue(slcCheck.getSelection());
        assertTrue(kslcCheck.getSelection());
        assertTrue(meCheck.getSelection());
        assertTrue(msCheck.getSelection());
        assertTrue(husCheck.getSelection());

        Text klocText = tab.getTxtKernelLocation();
        klocText.setText("doesntexist"); //$NON-NLS-1$
        klocText.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertFalse(tab.isValid(config));

        klocText.setText(""); //$NON-NLS-1$
        klocText.notifyListeners(SWT.Selection, null);
        performApply(tab, wc);
        assertTrue(tab.isValid(config));
    }

    //getter functions for otherwise unaccessible member variables
    private static class TestEventsTab extends PerfEventsTab {
        public Button get_chkDefaultEvent() { return chkDefaultEvent; }
    }

    @Test
    public void testEventsTab() throws CoreException {
        TestEventsTab tab = new TestEventsTab();
        tab.createControl(new Shell());
        assertNotNull(tab.getImage());
        assertNotNull(tab.getName());

        //default config
        tab.setDefaults(config.getWorkingCopy());
        tab.initializeFrom(config);
        assertTrue(tab.isValid(config));

        assertTrue(tab.get_chkDefaultEvent().getSelection());

        tab.get_chkDefaultEvent().notifyListeners(SWT.Selection, null);
        tab.get_chkDefaultEvent().setSelection(false);
        tab.get_chkDefaultEvent().notifyListeners(SWT.Selection, null);
        assertFalse(tab.get_chkDefaultEvent().getSelection());
    }

    public void performApply (ILaunchConfigurationTab tab, ILaunchConfigurationWorkingCopy wc) throws CoreException {
        tab.performApply(wc);
        wc.doSave();
    }

}
