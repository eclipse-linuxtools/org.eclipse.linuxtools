/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat, Inc.
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
