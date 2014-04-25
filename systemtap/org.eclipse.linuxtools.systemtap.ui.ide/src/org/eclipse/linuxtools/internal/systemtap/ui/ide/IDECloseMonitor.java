/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide;

import java.text.MessageFormat;

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
        if(!forced) {
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

            if(ScriptConsole.anyRunning()) {
                String msg = MessageFormat.format(Localization.getString("IDECloseMonitor.StillRunning"),(Object[]) null); //$NON-NLS-1$
                close = MessageDialog.openQuestion(window.getShell(), "Closing...", msg); //$NON-NLS-1$
            }
        }
        return close;
    }

    @Override
    public void postShutdown(IWorkbench workbench) {}
}
