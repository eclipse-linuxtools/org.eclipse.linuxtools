/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.provider.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.cdt.core.model.CModelException;
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
import org.eclipse.linuxtools.internal.profiling.launch.provider.ProviderProfileConstants;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderLaunchConfigurationDelegate;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderLaunchShortcut;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.FrameworkUtil;

public class LaunchTest extends AbstractTest {

    private static final String BIN_NAME = "fibTest"; //$NON-NLS-1$
    private static final String BIN_PATH = "Debug/" + BIN_NAME; //$NON-NLS-1$
    private static final String STUB_ID = "org.eclipse.linuxtools.profiling.stub"; //$NON-NLS-1$
    private static final String LAUNCH_SHORT_EXTPT = "org.eclipse.debug.ui.launchShortcuts"; //$NON-NLS-1$

    ProviderLaunchShortcut shortcut;
    String launchConfigTypeId;

    @BeforeEach
    public void setUp() throws Exception {
        proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()),
                BIN_NAME);

        // Set up the shortcut and launch config ID passed in through
        // IExecutableExtension
        IExtensionPoint extPoint = Platform.getExtensionRegistry()
                .getExtensionPoint(LAUNCH_SHORT_EXTPT);
        IConfigurationElement[] configs = extPoint.getConfigurationElements();
        for (IConfigurationElement cfg : configs) {
            if (cfg.getAttribute("id").equals(STUB_ID)) { //$NON-NLS-1$
                shortcut = (ProviderLaunchShortcut) cfg
                        .createExecutableExtension("class"); //$NON-NLS-1$
                launchConfigTypeId = cfg.getChildren("class")[0].getChildren("parameter")[1].getAttribute("value"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            deleteProject(proj);
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testIExecutableExtension() {
        IExtensionPoint extPoint = Platform.getExtensionRegistry()
                .getExtensionPoint(LAUNCH_SHORT_EXTPT);
        assertNotNull(extPoint);

        IConfigurationElement[] configs = extPoint.getConfigurationElements();
        assertTrue(configs.length > 0);

        for (IConfigurationElement cfg : configs) {
            if (cfg.getAttribute("id").equals(STUB_ID)) { //$NON-NLS-1$
                try {
                    assertTrue(cfg.createExecutableExtension("class") instanceof ProviderLaunchShortcut); //$NON-NLS-1$
                } catch (CoreException e) {
                    fail(e.getMessage());
                }
                assertEquals(cfg.getChildren("class").length, 1); //$NON-NLS-1$
                IConfigurationElement elem = cfg.getChildren("class")[0]; //$NON-NLS-1$
                for (int i = 0; i < 2; i++) {
                    assertNotNull(elem.getChildren("parameter")[i].getAttribute("name")); //$NON-NLS-1$ //$NON-NLS-2$
                    assertNotNull(elem.getChildren("parameter")[i].getAttribute("value")); //$NON-NLS-1$ //$NON-NLS-2$
                }

            }
        }
    }

    @Test
    public void testShortCut() throws CModelException {
        shortcut.launch(proj.getBinaryContainer().getBinaries()[0],
                ILaunchManager.PROFILE_MODE);
    }

    @Test
    public void testDefaultProfileShortcutSettings() throws CModelException {
        testShortCut();

        try {
            for (ILaunchConfiguration config : getLaunchManager()
                    .getLaunchConfigurations()) {
                if (config
                        .getAttribute(
                                ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
                                "").equals(BIN_PATH)) { //$NON-NLS-1$
                    if (config.getAttribute("foo", "").equals("bar")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        return;
                    }
                }
            }
        } catch (CoreException e) {
            fail(e.getMessage());
        }
        fail();
    }

    @Test
    public void testDelegate() throws CoreException {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        ProviderLaunchConfigurationDelegate delegate = new ProviderLaunchConfigurationDelegate();
        ILaunch launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);

        ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
        setProfileAttributes(wc);

        delegate.launch(wc, ILaunchManager.PROFILE_MODE, launch, null);
    }

    @Test
    public void testNoDefaultProfileShortcutSettings() throws CoreException {
        testDelegate();

        for (ILaunchConfiguration config : getLaunchManager()
                .getLaunchConfigurations()) {
            if (config
                    .getAttribute(
                            ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
                            "").equals(BIN_PATH)) { //$NON-NLS-1$
                assertNotSame("bar", config.getAttribute("foo", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }

    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return getLaunchManager()
                .getLaunchConfigurationType(launchConfigTypeId);
    }

    @Override
    protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
        // A delegate launch will have this property set, otherwise a shortcut
        // launch will be assumed
        // This is the provider with the highest priority
        wc.setAttribute(ProviderProfileConstants.PROVIDER_CONFIG_ATT,
                "org.eclipse.linuxtools.profiling.provider.stubby1"); //$NON-NLS-1$
        // Make each configuration unique
        wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                String.valueOf(System.currentTimeMillis()));
    }

}
