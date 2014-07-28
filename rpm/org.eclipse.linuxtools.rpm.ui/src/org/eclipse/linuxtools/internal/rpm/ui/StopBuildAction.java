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

/**
 * A console toolbar button for allowing the user to stop console-monitored jobs.
 */
public class StopBuildAction extends Action implements RpmConsoleObserver {

    RpmConsole fConsole;

    /**
     * Creates a new stop button for the given console.
     * @param console The console that this button will control.
     */
    public StopBuildAction(RpmConsole console) {
        fConsole = console;
        setImageDescriptor(
                PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_STOP));
        setToolTipText(Messages.getString("RPMConsoleAction.Stop")); //$NON-NLS-1$
        fConsole.addConsoleObserver(this);
    }

    /**
     * Attempts to stops the console's currently active job.
     */
    @Override
    public void run() {
        fConsole.stop();
    }

    @Override
    public void runningStateChanged(boolean running) {
        setEnabled(running);
    }

}
