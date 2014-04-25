/*******************************************************************************
 * Copyright (c) 2012 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
