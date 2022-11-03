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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpContentProducer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.PreferenceConstants;
import org.osgi.framework.FrameworkUtil;

public class DevHelpContentProducer implements IHelpContentProducer {

    @Override
    public InputStream getInputStream(String pluginID, String href, Locale locale) {
        InputStream stream = null;
        try {
            URI uri = new URI(href);

            // This is our custom index page, let the help system deal with it
            if ("html/devhelp.html".equals(uri.getPath())) { //$NON-NLS-1$
                return null;
            }

            IPreferenceStore ps = DevHelpPlugin.getDefault().getPreferenceStore();
            String[] paths = ps.getString(PreferenceConstants.DEVHELP_DIRECTORY).split(File.pathSeparator);
            Optional<Path> doc = Stream.of(paths).map(p -> Path.of(p, uri.getPath())).filter(Files::exists).findFirst();
            if (doc.isPresent()) {
                stream = Files.newInputStream(doc.get());
            }
        } catch (URISyntaxException | IOException e) {
            Platform.getLog(FrameworkUtil.getBundle(getClass()))
                    .warn(MessageFormat.format(Messages.DevHelpContentProducer_ContentReadError, href), e);
        }
        return stream;
    }
}
