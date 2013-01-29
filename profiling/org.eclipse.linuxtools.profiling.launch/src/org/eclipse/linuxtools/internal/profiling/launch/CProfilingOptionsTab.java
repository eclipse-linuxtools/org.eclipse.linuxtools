/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch;

import java.util.SortedMap;

import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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
	 * @param profilingType String type of profiling this tab will be used for.
	 * @param profilingName String name of this tab to be displayed.
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
				return "";
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
		   img = AbstractUIPlugin.imageDescriptorFromPlugin(ProfileLaunchPlugin.PLUGIN_ID, 
				"icons/time_obj.gif").createImage(); //$NON-NLS-1$
		return img;
	}
}
