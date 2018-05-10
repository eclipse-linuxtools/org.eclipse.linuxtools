/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchWindow;


/**
 * This listener is responsible for monitoring workbench close actions.  It is used
 * to veto a shutdown if there are scripts still running and the user does not really
 * want to shutdown.
 * @author Ryan Morse
 */
public class IDECloseMonitor implements IWorkbenchListener {
    @Override
    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        boolean close = true;
        if (!forced) {
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

            if (ScriptConsole.anyRunning()) {
                close = MessageDialog.openQuestion(window.getShell(),
                        Localization.getString("IDECloseMonitor.StillRunningTitle"), //$NON-NLS-1$
                        Localization.getString("IDECloseMonitor.StillRunning")); //$NON-NLS-1$
            }
        }
        return close;
    }

    @Override
    public void postShutdown(IWorkbench workbench) {}
}
