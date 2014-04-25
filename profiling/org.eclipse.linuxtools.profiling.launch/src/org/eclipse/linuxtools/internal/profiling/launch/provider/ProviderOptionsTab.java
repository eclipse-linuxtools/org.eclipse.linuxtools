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
package org.eclipse.linuxtools.internal.profiling.launch.provider;

import java.util.Map;

import org.eclipse.linuxtools.internal.profiling.launch.AbstractProfilingOptionsTab;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;

public class ProviderOptionsTab extends AbstractProfilingOptionsTab {

    /**
     * ProviderOptionsTab constructor.
     *
     * @param profilingType String type of profiling this tab will be used for.
     * @param profilingName String name of this tab to be displayed.
     */
    public ProviderOptionsTab(String profilingType, String profilingName) {
        setProfilingType(profilingType);
        setName(profilingName);
    }

    @Override
    protected Map<String, String> getProviders() {
        return ProviderFramework.getProviderNamesForType(getProfilingType());
    }

    @Override
    protected String getDefaultProviderId() {
        return ProviderFramework.getProviderIdToRun(null, getProfilingType());
    }


}
