/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.ui.view;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;

/**
 * Refresh menu item.
 */
public class OprofileViewRefreshAction extends Action {
    public OprofileViewRefreshAction() {
        super(OprofileUiMessages.getString("view.actions.refresh.label")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        OprofileUiPlugin.getDefault().getOprofileView().refreshView();
    }
}
