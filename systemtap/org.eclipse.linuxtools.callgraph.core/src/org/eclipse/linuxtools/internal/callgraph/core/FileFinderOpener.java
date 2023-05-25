/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.linuxtools.profiling.ui.ProfileUIUtils;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Helper class that finds and opens files. Finds based on function names,
 * opens based on path and in the current default editor.
 *
 */
public class FileFinderOpener {

    private static Map<String, int []> map = new HashMap<>();

    /**
     * Seeks all functions in the given proejct that contains the given function name.
     * Farms off the work of generating the list to the findFunctionsInProject function.
     * Opens a selection dialog if more than one file is found.
     *
     * @param project
     * @param functionName
     * @return
     */
    public static void findAndOpen(ICProject project, String functionName) {
        //Safety valve: Do not enforce use of project names
        if (project == null) {
            return;
        }

        map = ProfileUIUtils.findFunctionsInProject(project, functionName, -1, null);
        ArrayList<String> files = new ArrayList<>(map.keySet());

        if (files == null || files.size() < 1) {
            return;
        }

        if (files.size() == 1) {
            open(files.get(0), map.get(files.get(0))[0], map.get(files.get(0))[1]);
        } else {
            ElementListSelectionDialog d = new ElementListSelectionDialog(new Shell(), new LabelProvider());
            d.setTitle(Messages.getString("FileFinderOpener.MultipleFilesDialog"));  //$NON-NLS-1$
            d.setMessage(Messages.getString("FileFinderOpener.MultFilesDialogM1") + functionName + Messages.getString("FileFinderOpener.MultFilesDialogM2") +   //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.getString("FileFinderOpener.MultFilesDialogM3"));  //$NON-NLS-1$
            d.setElements(files.toArray());
            d.open();

            if (d.getResult() == null) {
                return;
            }

            for (Object o : d.getResult()) {
                if (o instanceof String s) {
                    open(s, map.get(s)[0], map.get(s)[1]);
                }
            }
        }
    }

    private static void open(String path, int offset, int length) {
        if (path == null) {
            return;
        }
        try {
            ProfileUIUtils.openEditorAndSelect(path, offset, length);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }
}
