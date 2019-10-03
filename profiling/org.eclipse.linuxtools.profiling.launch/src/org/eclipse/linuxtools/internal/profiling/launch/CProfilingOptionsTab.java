/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat, Inc and others.
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
package org.eclipse.linuxtools.internal.profiling.launch;

import java.util.SortedMap;

import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;
import org.eclipse.swt.graphics.Image;

// Special profiling options tab to use with the org.eclipse.cdt.launch.profilingProvider extension
// to extend the Local C/C++ Application configuration to handle profiling.  We do not rename
// the configuration as done in the normal profiling framework since it belongs to the CDT.
/**
 * @since 2.0
 */
public class CProfilingOptionsTab extends AbstractProfilingOptionsTab {

    String defaultType;

    public static final String TAB_ID = "org.eclipse.linuxtools.profiling.launch.profileApplicationLaunch.profilingTab"; //$NON-NLS-1$

    /**
     * ProviderOptionsTab constructor.
     *
     */
    public CProfilingOptionsTab() {
        setName(Messages.ProfilingTabName);
        setId(TAB_ID);
    }

    @Override
    protected SortedMap<String, String> getProviders() {
        return ProviderFramework.getAllProviderNames();
    }

    @Override
    protected String getDefaultProviderId() {
        // get the id of a provider
        if (defaultType == null) {
            String[] categories = ProviderFramework.getProviderCategories();
            if (categories.length == 0) {
                setErrorMessage(Messages.ProfilingTab_no_profilers_installed);
                return ""; //$NON-NLS-1$
            }
            for (String category : categories) {
                // Give precedence to timing category if present
                if (category.equals("timing")){ //$NON-NLS-1$
                    defaultType = "timing"; //$NON-NLS-1$
                }
            }
            // if default category still not set, take first one found
            if (defaultType == null)
                defaultType = categories[0];
        }
        return ProviderFramework.getProviderIdToRun(null, defaultType);
    }

    @Override
    protected void setConfigurationName(String newToolName) {
        // do nothing
    }

    @Override
    public Image getImage() {
        if (img == null)
           img = ResourceLocator.imageDescriptorFromBundle(ProfileLaunchPlugin.PLUGIN_ID,
                "icons/time_obj.gif").get().createImage(); //$NON-NLS-1$
        return img;
    }
}
