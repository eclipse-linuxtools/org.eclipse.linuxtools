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
package org.eclipse.linuxtools.internal.perf.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.perf.IPerfData;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.ui.StatView;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorLauncher;

/**
 * Class for handling opening of perf stat data files.
 */
public class PerfStatDataOpenHandler implements IEditorLauncher {

    private static final String TITLE_EXCERPT = "Performance counter stats for"; //$NON-NLS-1$

    @Override
    public void open(IPath file) {
        File statFile = file.toFile();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(statFile))) {
            final StringBuilder contents = new StringBuilder();
            final StringBuilder title = new StringBuilder();
            String line;

            // read file contents
            while ((line = fileReader.readLine()) != null) {
                // set data title
                if (title.length() == 0 && line.contains(TITLE_EXCERPT)) {
                    title.append(line);
                }
                contents.append(line);
                contents.append("\n"); //$NON-NLS-1$
            }

            // construct basic title if none was found in the file
            if (title.length() == 0) {
                title.append(NLS.bind(Messages.PerfEditorLauncher_stat_title,
                        statFile.getName()));
            }

            final String timestamp = DateFormat.getInstance().format(new Date(statFile.lastModified()));
            PerfPlugin.getDefault().setStatData(new IPerfData() {

                @Override
                public String getTitle() {
                    return title.toString() + " (" + timestamp + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }

                @Override
                public String getPerfData() {
                    return contents.toString();
                }
            });

            StatView.refreshView();
        } catch (FileNotFoundException e) {
            PerfPlugin.getDefault().openError(e,
                    NLS.bind(Messages.PerfEditorLauncher_file_dne_error, statFile.getName()));
        } catch (IOException e) {
            PerfPlugin.getDefault().openError(e,
                    NLS.bind(Messages.PerfEditorLauncher_file_read_error, statFile.getName()));
        }
    }
}
