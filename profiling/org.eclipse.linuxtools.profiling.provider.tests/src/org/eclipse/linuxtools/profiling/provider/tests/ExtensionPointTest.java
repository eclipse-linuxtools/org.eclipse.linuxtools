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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;
import org.eclipse.linuxtools.profiling.provider.tests.stubby.StubbyLaunchConfigurationDelegate;
import org.eclipse.linuxtools.profiling.provider.tests.stubby.StubbyLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.profiling.provider.tests.stubby.StubbyLaunchShortcut;
import org.junit.jupiter.api.Test;

public class ExtensionPointTest {

    private static final String PROFILING_TYPE = "stub"; //$NON-NLS-1$
    private static final String PLUGIN_ID = "org.eclipse.linuxtools.profiling.provider.stubby"; //$NON-NLS-1$

    @Test
    public void testId() {
        String highestProviderId = ProviderFramework.getHighestProviderId(PROFILING_TYPE);
        assertEquals(PLUGIN_ID + "1", highestProviderId); //$NON-NLS-1$

        String[] providerIds = ProviderFramework.getProviderIdsForType(PROFILING_TYPE);
        HashSet<String> set = new HashSet<>(Arrays.asList(providerIds));
        for (int i = 0; i < providerIds.length; i++){
            assertTrue(set.contains(PLUGIN_ID + (i+1)));
        }
    }

    @Test
    public void testShortCut () {
        ProfileLaunchShortcut shortcut = ProviderFramework.getLaunchShortcutProviderFromId(PLUGIN_ID + "1"); //$NON-NLS-1$
        ProfileLaunchShortcut shortcut2 = ProviderFramework.getProfilingProvider(PROFILING_TYPE);

        assertTrue(shortcut instanceof StubbyLaunchShortcut);
        assertTrue(shortcut2 instanceof StubbyLaunchShortcut);
    }

    @Test
    public void testName () {
        HashMap<String, String> providerNames = ProviderFramework.getProviderNamesForType(PROFILING_TYPE);
        assertEquals(3, providerNames.size());
        for (int i = 1; i <= providerNames.size(); i++){
            assertTrue(providerNames.values().contains(PLUGIN_ID + i));
            assertTrue(providerNames.keySet().contains("Profile As Stubby " + i)); //$NON-NLS-1$
        }
    }

    @Test
    public void testDelegate () {
        AbstractCLaunchDelegate delegate = ProviderFramework.getConfigurationDelegateFromId(PLUGIN_ID + "1"); //$NON-NLS-1$
        assertTrue(delegate instanceof StubbyLaunchConfigurationDelegate);
    }

    @Test
    public void testTabGroup () {
        ProfileLaunchConfigurationTabGroup tabgroup = ProviderFramework.getTabGroupProvider(PROFILING_TYPE);
        ProfileLaunchConfigurationTabGroup tabgroup2 = ProviderFramework.getTabGroupProviderFromId(PLUGIN_ID + "1"); //$NON-NLS-1$

        assertTrue(tabgroup instanceof StubbyLaunchConfigurationTabGroup);
        assertTrue(tabgroup2 instanceof StubbyLaunchConfigurationTabGroup);
    }

}
