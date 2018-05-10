/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Sami Wagiaalla
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;

public class SystemTapScriptLaunchConfigurationTabGroup extends
        AbstractLaunchConfigurationTabGroup {

    public SystemTapScriptLaunchConfigurationTabGroup() {
    }

    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        AbstractLaunchConfigurationTab[] tabs = new AbstractLaunchConfigurationTab[] {
                new SystemTapScriptLaunchConfigurationTab(),
                new SystemTapScriptOptionsTab(),
                new SystemTapScriptGraphOptionsTab(),
                new CommonTab() };
        setTabs(tabs);
    }

}
