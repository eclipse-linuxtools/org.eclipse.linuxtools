/*******************************************************************************
 * Copyright (c) 2011, 2018 IBM Corporation
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Daniel H Barboza <danielhb@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.helgrind;

import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class HelgrindViewPart extends ViewPart implements IValgrindToolView {

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
