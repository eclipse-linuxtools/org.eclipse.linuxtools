/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.changelog.core.actions.PrepareChangeLogAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class ChangeLogActionProvider extends CommonActionProvider {
    private Action exampleAction;

    @Override
    public void init(ICommonActionExtensionSite aSite) {
        super.init(aSite);
        exampleAction = new PrepareChangeLogAction() {
            @Override
            public void run() {
                setSelection((IStructuredSelection) getContext().getSelection());
                doRun();
            }

        };

    }

    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
        menu.add(exampleAction);
    }

    @Override
    public void fillActionBars(IActionBars actionBars) {

        actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
                exampleAction);
    }
}
