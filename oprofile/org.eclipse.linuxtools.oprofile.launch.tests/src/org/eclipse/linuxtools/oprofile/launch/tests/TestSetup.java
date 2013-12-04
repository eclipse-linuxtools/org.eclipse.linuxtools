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

package org.eclipse.linuxtools.oprofile.launch.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileSetupTab;
import org.eclipse.linuxtools.oprofile.launch.tests.utils.LaunchTestingOptions;
import org.eclipse.linuxtools.oprofile.launch.tests.utils.OprofileTestingEventConfigTab;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

public class TestSetup extends AbstractTest {
	protected ILaunchConfiguration config;
	protected Shell testShell;
	protected IProject project;

	@Before
	public void setUp() throws Exception {
		proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), "primeTest"); //$NON-NLS-1$
		project = proj.getProject();
		config = createConfiguration(proj.getProject());
		testShell = new Shell(Display.getDefault());
		testShell.setLayout(new GridLayout());
	}

	@After
	public void tearDown() throws Exception {
		testShell.dispose();
		deleteProject(proj);
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(OprofileLaunchPlugin.ID_LAUNCH_PROFILE_MANUAL);
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
		OprofileTestingEventConfigTab configTab = new OprofileTestingEventConfigTab();
		OprofileSetupTab setupTab = new OprofileSetupTab();
		configTab.setOprofileProject(proj.getProject());
		configTab.setDefaults(wc);
		setupTab.setDefaults(wc);
	}

	//getter functions for otherwise unaccessible member variables
	private class OprofileTestingSetupTab extends OprofileSetupTab {
		@Override
		protected IProject getOprofileProject() { return proj.getProject(); }
		@Override
		public void setDefaults(ILaunchConfigurationWorkingCopy config) {
			options = new LaunchTestingOptions();
			options.saveConfiguration(config);
		}
		protected Button getKernelCheck() { return checkSeparateKernel; }
		protected Button getLibraryCheck() { return checkSeparateLibrary; }
		protected Text getTextKernelImage() { return kernelImageFileText; }
	}
	@Test
	public void testSetupTab() throws CoreException {
		OprofileTestingSetupTab tab = new OprofileTestingSetupTab();
		tab.createControl(new Shell());
		assertNotNull(tab.getImage());
		assertNotNull(tab.getName());

		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();

		//default config
		tab.setDefaults(wc);
		tab.initializeFrom(config);

		Button libraryCheck = tab.getLibraryCheck();
		libraryCheck.setSelection(true);
		libraryCheck.notifyListeners(SWT.Selection, null);
		performApply(tab, wc);
		assertEquals(OprofileDaemonOptions.SEPARATE_LIBRARY, config.getAttribute(OprofileLaunchPlugin.ATTR_SEPARATE_SAMPLES, -1));
		libraryCheck.setSelection(false);
		libraryCheck.notifyListeners(SWT.Selection, null);
		performApply(tab, wc);
		assertEquals(OprofileDaemonOptions.SEPARATE_NONE, config.getAttribute(OprofileLaunchPlugin.ATTR_SEPARATE_SAMPLES, -1));

		Button kernelCheck = tab.getKernelCheck();
		kernelCheck.setSelection(true);
		kernelCheck.notifyListeners(SWT.Selection, null);
		performApply(tab, wc);
		assertEquals(OprofileDaemonOptions.SEPARATE_KERNEL, config.getAttribute(OprofileLaunchPlugin.ATTR_SEPARATE_SAMPLES, -1));
		kernelCheck.setSelection(false);
		kernelCheck.notifyListeners(SWT.Selection, null);
		performApply(tab, wc);
		assertEquals(OprofileDaemonOptions.SEPARATE_NONE, config.getAttribute(OprofileLaunchPlugin.ATTR_SEPARATE_SAMPLES, -1));

		libraryCheck.setSelection(true);
		libraryCheck.notifyListeners(SWT.Selection, null);
		kernelCheck.setSelection(true);
		kernelCheck.notifyListeners(SWT.Selection, null);
		performApply(tab, wc);
		tab.initializeFrom(config);
		assertTrue(libraryCheck.getSelection());
		assertTrue(kernelCheck.getSelection());

		Text kernelLocationText = tab.getTextKernelImage();
		kernelLocationText.setText("doesntexist"); //$NON-NLS-1$
		kernelLocationText.notifyListeners(SWT.Selection, null);
		performApply(tab, wc);
		assertFalse(tab.isValid(config));

		kernelLocationText.setText(""); //$NON-NLS-1$
		kernelLocationText.notifyListeners(SWT.Selection, null);
		performApply(tab, wc);
		assertTrue(tab.isValid(config));
	}
	@Test
	public void testEventConfigTab() throws CoreException {
		OprofileTestingEventConfigTab tab = new OprofileTestingEventConfigTab();
		tab.createControl(new Shell());
		assertNotNull(tab.getImage());
		assertNotNull(tab.getName());

		tab.setDefaults(config.getWorkingCopy());
		tab.initializeFrom(config);
		assertTrue(tab.isValid(config));

		assertTrue(tab.getDefaultCheck().getSelection());
		tab.getDefaultCheck().notifyListeners(SWT.Selection, null);
		tab.getDefaultCheck().setSelection(false);
		tab.getDefaultCheck().notifyListeners(SWT.Selection, null);

		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, false);
		performApply(tab, wc);
		assertFalse(tab.isValid(config));
	}
	private void performApply (ILaunchConfigurationTab tab , ILaunchConfigurationWorkingCopy wc) throws CoreException {
		tab.performApply(wc);
		wc.doSave();
	}
}
