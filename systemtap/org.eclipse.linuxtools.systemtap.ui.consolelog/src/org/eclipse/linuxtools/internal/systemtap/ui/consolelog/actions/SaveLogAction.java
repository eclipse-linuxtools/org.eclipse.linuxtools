/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *     Red Hat - ongoing maintenance
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.consolelog.actions;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole.ScriptConsoleObserver;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

/**
 * This class is used to allow the user to save the log generated from an active console
 */
public class SaveLogAction extends ConsoleAction implements ScriptConsoleObserver {

    private String logFileName = null;

    /**
     * Creates the action for the given script console.
     * @param consoleParam The console which log is to be saved.
     * @since 2.0
     */
    public SaveLogAction(ScriptConsole consoleParam) {
        super(consoleParam,
                ConsoleLogPlugin.getDefault().getBundle().getEntry("icons/actions/save_log.gif"), //$NON-NLS-1$
                Localization.getString("action.saveLog.name"), //$NON-NLS-1$
                Localization.getString("action.saveLog.desc"), //$NON-NLS-1$
                IAction.AS_CHECK_BOX);
        consoleParam.addScriptConsoleObserver(this);
    }

    /**
     * The main method of this class. Handles getting the current <code>ScriptConsole</code>
     * and telling it to save the output to the selected file.
     */
    @Override
    public void run() {
        File file = getFile();
        if (file != null) {
            if (console.saveStreamAndReturnResult(file)) {
                logFileName = file.toString();
            }
        }
        updateChecked();
    }

    /**
     * Brings up a dialog box for the user to select a place to save the log output.
     * @return File representing the desired destination for the log.
     */
    private File getFile() {
        FileDialog dialog = new FileDialog(PlatformUI
                .getWorkbench().getActiveWorkbenchWindow()
                .getShell(), SWT.SAVE);

        if (logFileName != null) {
            Path logPath = new Path(logFileName);
            dialog.setFilterPath(logPath.removeLastSegments(1).toOSString());
            dialog.setFileName(logPath.lastSegment());
        }
        dialog.setText(Localization.getString(
                !isLogging() ? "SaveLogAction.OutputFile" //$NON-NLS-1$
                        : "SaveLogAction.OutputFileLocation")); //$NON-NLS-1$
        dialog.setOverwrite(true);
        String path = dialog.open();
        return path != null ? new File(path) : null;
    }

    public void updateChecked() {
        if (isLogging()) {
            setToolTipText(MessageFormat.format(
                    Localization.getString("action.saveLog.name2"), //$NON-NLS-1$
                    logFileName));
            setChecked(true);
        } else {
            setToolTipText(Localization.getString("action.saveLog.name")); //$NON-NLS-1$
            setChecked(false);
        }
    }

    private boolean isLogging() {
        return logFileName != null;
    }

    @Override
    public void runningStateChanged(boolean started, boolean stopped) {
        // Uncheck the button whenever a script restarts, for it will be associated with
        // a new command and therefore a new logger.
        if (started && !stopped) {
            logFileName = null;
            updateChecked();
        }
    }

}
