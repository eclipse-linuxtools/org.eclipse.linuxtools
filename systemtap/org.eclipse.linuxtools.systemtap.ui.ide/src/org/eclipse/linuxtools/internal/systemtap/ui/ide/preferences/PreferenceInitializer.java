/* Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;


public class PreferenceInitializer extends AbstractPreferenceInitializer {
    private static final String[] KERNEL_SOURCE_PATH = {"/usr/src/kernels/{kernel_version}", //$NON-NLS-1$
        "/usr/src/linux", //$NON-NLS-1$
        "/usr/src/linux-{kernel_version}" }; //$NON-NLS-1$

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = IDEPlugin.getDefault().getPreferenceStore();

        //ide
        store.setDefault(IDEPreferenceConstants.P_STORED_TREE, false);
        store.setDefault(IDEPreferenceConstants.P_REMOTE_PROBES, false);

        //ide.path
        store.setDefault(IDEPreferenceConstants.P_KERNEL_SOURCE, getKernelSourceLocation());
        store.setDefault(IDEPreferenceConstants.P_EXCLUDED_KERNEL_SOURCE,
                "CVS/" + File.pathSeparator + //$NON-NLS-1$
                ".svn/" + File.pathSeparator + //$NON-NLS-1$
                "{arch}/" + File.pathSeparator + //$NON-NLS-1$
                ".arch-ids/" + File.pathSeparator + //$NON-NLS-1$
                ".bzr/" + File.pathSeparator + //$NON-NLS-1$
                "debian/" + File.pathSeparator + //$NON-NLS-1$
                ".git/"); //$NON-NLS-1$
        store.setDefault(IDEPreferenceConstants.P_REMOTE_LOCAL_KERNEL_SOURCE, PathPreferencePage.LOCAL);

        //ide.stap.tapsets
        store.setDefault(IDEPreferenceConstants.P_TAPSETS, ""); //$NON-NLS-1$

        //ide.stap.stapoptions
        for(int i=0; i<IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS.length; i++) {
            store.setDefault(IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.KEY], false);
        }

        for(int i=0; i<IDEPreferenceConstants.STAP_STRING_OPTIONS.length; i++) {
            store.setDefault(IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.KEY], ""); //$NON-NLS-1$
        }

    }

    private String getKernelSourceLocation(){
        // Find out the version of the currently running kernel.
        String version = ""; //$NON-NLS-1$
        try {
            Process process = RuntimeProcessFactory.getFactory().exec("uname -r", null, null);//$NON-NLS-1$
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            version = reader.readLine();
        } catch (IOException e) {
            // Could not run uname use an empty String
        }

        // Go over the search path looking for the file System.map
        for (String path : KERNEL_SOURCE_PATH) {
            path = path.replace("{kernel_version}", version);  //$NON-NLS-1$
            File file = new File(path+ "/System.map"); //$NON-NLS-1$
            if (file.exists()){
                return path;
            }
        }

        return ""; //$NON-NLS-1$
    }

}
