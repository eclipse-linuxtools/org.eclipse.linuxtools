/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.provider.tests;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.linuxtools.internal.profiling.provider.ProviderProfileConstants;
import org.eclipse.linuxtools.internal.profiling.provider.launch.ProviderLaunchConfigurationDelegate;
import org.eclipse.linuxtools.internal.profiling.provider.launch.ProviderLaunchShortcut;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

public class LaunchTest extends AbstractTest {

	private static final String BIN_NAME = "fibTest";
	private static final String BIN_PATH = "Debug/" + BIN_NAME;
	private static final String STUB_ID = "org.eclipse.linuxtools.profiling.stub";
	private static final String LAUNCH_SHORT_EXTPT = "org.eclipse.debug.ui.launchShortcuts";

	ProviderLaunchShortcut shortcut;
	String launchConfigTypeId;

	@Override
	public void setUp() throws Exception {
		proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), BIN_NAME);

		// Set up the shortcut and launch config ID passed in through IExecutableExtension
		IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(LAUNCH_SHORT_EXTPT);
		IConfigurationElement[] configs = extPoint.getConfigurationElements();
		for (IConfigurationElement cfg : configs) {
			if (cfg.getAttribute("id").equals(STUB_ID)){
				try {
					shortcut = (ProviderLaunchShortcut) cfg.createExecutableExtension("class");
					launchConfigTypeId = cfg.getChildren("class")[0].getChildren("parameter")[1].getAttribute("value");
				} catch (Exception e){
					fail ();
				}
			}
		}
	}

	@Override
	public void tearDown () {
		try {
			deleteProject(proj);
		} catch (CoreException e) {
			fail ();

		}
	}

	@Test
	public void testIExecutableExtension () {
		IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(LAUNCH_SHORT_EXTPT);
		assertNotNull(extPoint);

		IConfigurationElement[] configs = extPoint.getConfigurationElements();
		assertTrue(configs.length > 0);

		for (IConfigurationElement cfg : configs) {
			if (cfg.getAttribute("id").equals(STUB_ID)){
				try {
					assertTrue(cfg.createExecutableExtension("class") instanceof ProviderLaunchShortcut);
					assertTrue(cfg.getChildren("class").length == 1);
					IConfigurationElement elem = cfg.getChildren("class")[0];
					for (int i = 0; i < 2; i++) {
						assertNotNull(elem.getChildren("parameter")[i].getAttribute("name"));
						assertNotNull(elem.getChildren("parameter")[i].getAttribute("value"));
					}

				} catch (Exception e){
					fail ();
				}
			}
		}
	}

	@Test
	public void testShortCut() {
		try {
			shortcut.launch(proj.getBinaryContainer().getBinaries()[0], ILaunchManager.PROFILE_MODE);
		} catch (Exception e) {
			fail ();
		}
	}

	@Test
	public void testDefaultProfileShortcutSettings () {
		testShortCut();

		try {
			for (ILaunchConfiguration config : getLaunchManager().getLaunchConfigurations()){
				if (config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "").equals(BIN_PATH)){
					if (config.getAttribute("foo", "").equals("bar")){
						return;
					}
				}
			}
		} catch (CoreException e) {
			fail ();
		}
		fail ();
	}

	@Test
	public void testDelegate() {
		try {
			ILaunchConfiguration config = createConfiguration(proj.getProject());
			ProviderLaunchConfigurationDelegate delegate = new ProviderLaunchConfigurationDelegate();
			ILaunch launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);

			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			setProfileAttributes(wc);

			delegate.launch(wc, ILaunchManager.PROFILE_MODE, launch, null);
		} catch (Exception e) {
			fail ();
		}
	}


	@Test
	public void testNoDefaultProfileShortcutSettings () {
		testDelegate();

		try {
			for (ILaunchConfiguration config : getLaunchManager().getLaunchConfigurations()){
				if (config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "").equals(BIN_PATH)){
					assertNotSame("bar", config.getAttribute("foo", ""));
				}
			}
		} catch (CoreException e) {
			fail ();
		}
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(launchConfigTypeId);
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc)
			throws CoreException {
		// A delegate launch will have this property set, otherwise a shortcut launch will be assumed
		// This is the provider with the highest priority
		wc.setAttribute(ProviderProfileConstants.PROVIDER_CONFIG_ATT, "org.eclipse.linuxtools.profiling.provider.stubby1");
		// Make each configuration unique
		wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, String.valueOf(System.currentTimeMillis()));
	}

}
