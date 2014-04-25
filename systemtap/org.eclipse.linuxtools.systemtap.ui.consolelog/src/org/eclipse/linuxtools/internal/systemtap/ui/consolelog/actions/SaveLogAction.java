/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.consolelog.actions;

import java.io.File;

import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

/**
 * This class is used to allow the user to save the log generated from an active console
 * @author Ryan Morse
 */
public class SaveLogAction extends ConsoleAction {

    /**
     * @since 2.0
     */
    public SaveLogAction(ScriptConsole fConsole) {
        super(fConsole,
                ConsoleLogPlugin.getDefault().getBundle().getEntry("icons/actions/save_log.gif"), //$NON-NLS-1$
                Localization.getString("action.saveLog.name"), //$NON-NLS-1$
                Localization.getString("action.saveLog.desc")); //$NON-NLS-1$
    }

    /**
     * The main method of this class. Handles getting the current <code>ScriptConsole</code>
     * and telling it to save the output to the selected file.
     */
    @Override
    public void run() {
        if(null != console) {
            File file = getFile();
            if(null != file){
                console.saveStream(file);
            }
        }
    }

    /**
     * Brings up a dialog box for the user to select a place to save the log output.
     * @return File representing the desired destination for the log.
     */
    private File getFile() {
        String path = null;
        FileDialog dialog= new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
        dialog.setText(Localization.getString("SaveLogAction.OutputFile")); //$NON-NLS-1$

        path = dialog.open();

        if(null == path){
            return null;
        }

        return new File(path);
    }
}
