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

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * This class is responsible for creating and initializing UI for a {@link RpmConsole}.
 */
public class RpmConsolePageParticipant implements IConsolePageParticipant {

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        return null;
    }

    @Override
    public void init(IPageBookViewPage page, IConsole console) {
        if (!(console instanceof RpmConsole)) {
            return;
        }
        IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();

        StopBuildAction stopBuildAction = new StopBuildAction((RpmConsole) console);
        manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, stopBuildAction);

        CloseConsoleAction closeConsoleAction = new CloseConsoleAction((RpmConsole) console);
        manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, closeConsoleAction);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void activated() {
    }

    @Override
    public void deactivated() {
    }

}
