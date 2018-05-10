/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat Inc. and others.
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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.views;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * A handler for refreshing the contents of {@link BrowserView}s.
 */
public class RefreshHandler extends AbstractHandler {
    public static final String COMMAND_ID = "org.eclipse.linuxtools.systemtap.ui.ide.refreshView"; //$NON-NLS-1$
    private boolean active = true;

    /**
     * Use this method to enable/disable the handler. Simpler than working with
     * the standard ways to control toolbar button enablement.
     * @param state
     */
    public void setActive(boolean state) {
        active = state;
    }

    @Override
    public Object execute(ExecutionEvent event) {
        if (active) {
            final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
            if (activePart instanceof BrowserView) {
                new Thread(() -> ((BrowserView) activePart).refresh()).start();
            }
        }
        return null;
    }

}
