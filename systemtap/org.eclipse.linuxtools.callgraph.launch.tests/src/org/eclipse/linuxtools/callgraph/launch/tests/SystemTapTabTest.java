/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.callgraph.launch.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.callgraph.launch.LaunchStapGraph;
import org.eclipse.linuxtools.internal.callgraph.launch.SystemTapOptionsTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
public class SystemTapTabTest {

    @Test
    public void testTabs() throws CoreException{
        Shell sh = new Shell();
        Composite cmp = new Composite(sh, SWT.NONE);

        LaunchStapGraph shortCut = new LaunchStapGraph();
        SystemTapOptionsTab stp = new SystemTapOptionsTab();
        stp.createControl(cmp);
        ILaunchConfiguration configuration;
        configuration = shortCut.outsideGetLaunchConfigType().newInstance(
                null,
                (DebugPlugin.getDefault().getLaunchManager())
                        .generateLaunchConfigurationName("invalid"));
        ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
        stp.setDefaults(wc);
        stp.performApply(wc);
        wc.doSave();
        stp.initializeFrom(configuration);
        sh.open();
    }
}
