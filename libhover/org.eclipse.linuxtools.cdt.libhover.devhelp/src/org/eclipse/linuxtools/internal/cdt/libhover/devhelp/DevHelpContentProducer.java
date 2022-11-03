/*******************************************************************************
 * Copyright (c) 2011, 2022 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import java.io.InputStream;
import java.util.Locale;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.IHelpContentProducer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.PreferenceConstants;

public class DevHelpContentProducer implements IHelpContentProducer {

    @Override
    public InputStream getInputStream(String pluginID, String href,
            Locale locale) {
        // Eclipse help system adds parameters to the href but this breaks our path creation so we just strip them.
        if (href.contains("?")) { //$NON-NLS-1$
            href = href.substring(0, href.indexOf('?'));
        }
        // This is our custom index page, let the help system deal with it
        if ("html/devhelp.html".equals(href)) { //$NON-NLS-1$
            return null;
        }
        IPreferenceStore ps = DevHelpPlugin.getDefault().getPreferenceStore();
        IPath devhelpLocation = new Path(ps.getString(PreferenceConstants.DEVHELP_DIRECTORY)).append(href);
        IFileSystem fs = EFS.getLocalFileSystem();
        IFileStore localLocation = fs.getStore(devhelpLocation);
        InputStream stream = null;
        try {
            stream = localLocation.openInputStream(EFS.NONE, new NullProgressMonitor());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return stream;
    }

}
