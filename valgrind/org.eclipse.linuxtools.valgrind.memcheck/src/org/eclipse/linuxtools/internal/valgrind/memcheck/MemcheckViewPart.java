/*******************************************************************************
 * Copyright (c) 2008, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.memcheck;

import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class MemcheckViewPart extends ViewPart implements IValgrindToolView {

    @Override
    public void createPartControl(Composite parent) {
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void refreshView() {
    }

    @Override
    public IAction[] getToolbarActions() {
        return null;
    }

}
