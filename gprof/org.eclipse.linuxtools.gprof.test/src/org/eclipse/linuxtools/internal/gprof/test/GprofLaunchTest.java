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
*    Red Hat, Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.test;


import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.linuxtools.internal.gprof.launch.GprofLaunchConfigurationDelegate;
import org.eclipse.linuxtools.internal.profiling.launch.provider.ProviderProfileConstants;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderLaunchShortcut;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.linuxtools.profiling.ui.CProjectBuildHelpers;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.FrameworkUtil;

public class GprofLaunchTest extends AbstractTest {

    protected ILaunchConfiguration config;
    protected AbstractCLaunchDelegate delegate;
    protected ILaunch launch;
    protected ILaunchConfigurationWorkingCopy wc;
    private static final String ID = "org.eclipse.linuxtools.profiling.provider.TimingLaunchShortcut"; //$NON-NLS-1$
    private static final String LAUNCH_SHORT_EXTPT = "org.eclipse.debug.ui.launchShortcuts"; //$NON-NLS-1$
    private static final String GPROF_PROVIDER_ID = "org.eclipse.linuxtools.profiling.provider.timing.gprof"; //$NON-NLS-1$
    private static final String GPROF_CATEGORY = "timing"; //$NON-NLS-1$

    ProviderLaunchShortcut shortcut;
    String launchConfigTypeId;

    @BeforeEach
    public void setUp() throws Exception {
        proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), "fibTest"); //$NON-NLS-1$
        ProjectScope ps = new ProjectScope(proj.getProject());
        ScopedPreferenceStore scoped = new ScopedPreferenceStore(ps, ProviderProfileConstants.PLUGIN_ID);
        scoped.setSearchContexts(new IScopeContext[] { ps, InstanceScope.INSTANCE });
        scoped.setValue(ProviderProfileConstants.PREFS_KEY + GPROF_CATEGORY, GPROF_PROVIDER_ID);
        scoped.setValue(ProviderProfileConstants.USE_PROJECT_SETTINGS + GPROF_CATEGORY, true);
        scoped.save();

        IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(LAUNCH_SHORT_EXTPT);
        IConfigurationElement[] configs = extPoint.getConfigurationElements();
        for (IConfigurationElement cfg : configs) {
            if (cfg.getAttribute("id").equals(ID)){ //$NON-NLS-1$
                try {
                    shortcut = (ProviderLaunchShortcut) cfg.createExecutableExtension("class"); //$NON-NLS-1$
                    launchConfigTypeId = cfg.getChildren("class")[0].getChildren("parameter")[1].getAttribute("value"); //$NON-NLS-1$
                } catch (Exception e){
                    fail (e.getMessage());
                }
            }
        }
        config = createConfiguration(proj.getProject());

        enableGprofSupport();   //(otherwise test hangs on 'enable GGprof support' dialogue. )

        //---- Continue with launch.
        launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);
         wc = config.getWorkingCopy();
    }

    private void enableGprofSupport() {
        String optionId = null;
        if (CProjectBuildHelpers.isCppType(proj.getProject())) {
            optionId = "gnu.cpp.compiler.option.debugging.gprof"; //$NON-NLS-1$
        } else if (CProjectBuildHelpers.isCType(proj.getProject())) {
            optionId = "gnu.c.compiler.option.debugging.gprof"; //$NON-NLS-1$
        }
        CProjectBuildHelpers.setOptionInCDT(proj.getProject(), optionId, true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        deleteProject(proj);
        wc.delete();
        config.delete();
    }

    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(launchConfigTypeId);
    }

    @Override
    protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
    }

    @Test
    public void testDelegateLaunch() throws CoreException {
        delegate = new GprofLaunchConfigurationDelegate();
        delegate.launch(wc, ILaunchManager.PROFILE_MODE, launch, null);
    }

}
