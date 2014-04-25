/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.profiling.launch.ui;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 2.0
 */
public class LocalResourceSelectorProxy implements IRemoteResourceSelectorProxy {

    @Override
    public URI selectFile(String scheme, String initialPath, String prompt, Shell shell) {
        FileDialog dialog = new FileDialog(shell, SWT.SHEET);
        dialog.setText(prompt);
        dialog.setFilterPath(initialPath);
        try {
            String path = dialog.open();
            if (path != null)
                return new URI(path);
            else
                return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public URI selectDirectory(String scheme, String initialPath, String prompt, Shell shell) {
        DirectoryDialog dialog = new DirectoryDialog(shell, SWT.SHEET);
        dialog.setText(prompt);
        dialog.setFilterPath(initialPath);
        try {
            String path = dialog.open();
            if (path != null)
                return new URI(path);
            else
                return null;
        } catch (URISyntaxException e) {
            return null;
        }

    }

}
