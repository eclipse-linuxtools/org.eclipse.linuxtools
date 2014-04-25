/*******************************************************************************
 * Copyright (c) 2012, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch.provider.launch;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;
import org.eclipse.linuxtools.internal.profiling.launch.provider.ProviderProfileConstants;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.osgi.util.NLS;

public class ProviderLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

    @Override
    public void launch(ILaunchConfiguration config, String mode,
            ILaunch launch, IProgressMonitor monitor) throws CoreException {

        if (config != null) {
            // get provider id from configuration.
            String providerId = config.getAttribute(
                    ProviderProfileConstants.PROVIDER_CONFIG_ATT, ""); //$NON-NLS-1$
            String providerToolName = config.getAttribute(
                    ProviderProfileConstants.PROVIDER_CONFIG_TOOLNAME_ATT, ""); //$NON-NLS-1$

            if (providerId == null || providerId.isEmpty()) {
                String cProjectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, ""); //$NON-NLS-1$
                if (cProjectName != null && !cProjectName.isEmpty()) {
                    // We have a previously created C/C++ run/debug configuration and now Linux Tools
                    // profiling framework has been added.  Find a suitable default provider id to use
                    // and perform initialization prior to profiling.
                    String defaultType = null;
                    String[] categories = ProviderFramework.getProviderCategories();
                    if (categories.length == 0) {
                        infoDialog(Messages.ProviderNoProfilers_title_0, Messages.ProviderNoProfilers_msg_0);
                        return;
                    }
                    for (String category : categories) {
                        // Give precedence to timing category if present
                        if (category.equals("timing")) { //$NON-NLS-1$
                            defaultType = "timing"; //$NON-NLS-1$
                        }
                    }
                    // if default category still not set, take first one found
                    if (defaultType == null)
                        defaultType = categories[0];
                    providerId = ProviderFramework.getProviderIdToRun(null, defaultType);
                    ProfileLaunchConfigurationTabGroup tabGroupConfig =
                            ProviderFramework.getTabGroupProviderFromId(providerId);
                    if (tabGroupConfig == null) {
                        infoDialog(Messages.ProviderNoProfilers_title_0, Messages.ProviderNoProfilers_msg_0);
                        return;
                    }
                    AbstractLaunchConfigurationTab[] tabs = tabGroupConfig.getProfileTabs();

                    // Set up defaults according to profiling tabs.  We must use a working copy
                    // to do this which we save at the end to the original copy.
                    ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
                    for (ILaunchConfigurationTab tab : tabs) {
                        tab.setDefaults(wc);
                    }
                    config = wc.doSave();
                }

            }

            // get delegate associated with provider id.
            AbstractCLaunchDelegate delegate = ProviderFramework
                    .getConfigurationDelegateFromId(providerId);

            // launch delegate
            if (delegate != null) {
                delegate.launch(config, mode, launch, monitor);
            } else {
                String message = providerToolName.isEmpty() ?
                        NLS.bind(Messages.ProviderProfilerMissing_msg_0, providerId)
                        : NLS.bind(Messages.ProviderProfilerMissing_msg_1, providerToolName);
                infoDialog(Messages.ProviderProfilerMissing_title_0, message);
            }
        }
    }

    // Display an information dialog to denote there are no profiling plug-ins installed.
    private void infoDialog(final String title, final String message) {
        ProfileLaunchPlugin.getShell().getDisplay().asyncExec( new Runnable() {
            @Override
            public void run() {
                MessageDialog.openInformation(ProfileLaunchPlugin.getShell(), title, message);
            }
        });
    }

    @Override
    protected String getPluginID() {
        return ProviderProfileConstants.PLUGIN_ID;
    }

}
