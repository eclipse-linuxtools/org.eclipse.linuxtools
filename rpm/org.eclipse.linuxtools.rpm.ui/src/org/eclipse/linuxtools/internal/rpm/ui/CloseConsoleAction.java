/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.rpm.ui.RpmConsole.RpmConsoleObserver;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

/**
 * A console toolbar button for closing the console when it is inactive.
 */
public class CloseConsoleAction extends Action implements RpmConsoleObserver {

    RpmConsole fConsole;

    /**
     * Creates a new stop button for the given console.
     * @param console The console that this button will control.
     */
    public CloseConsoleAction(RpmConsole console) {
        fConsole = console;
        setImageDescriptor(
                PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_REMOVE));
        setToolTipText(Messages.getString("RPMConsoleAction.Close")); //$NON-NLS-1$
        fConsole.addConsoleObserver(this);
    }

    /**
     * Closes the console.
     */
    @Override
    public void run() {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                if(fConsole != null){
                    ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{fConsole});
                }
            }
        });
    }

    @Override
    public void runningStateChanged(boolean running) {
        setEnabled(!running);
    }

}
