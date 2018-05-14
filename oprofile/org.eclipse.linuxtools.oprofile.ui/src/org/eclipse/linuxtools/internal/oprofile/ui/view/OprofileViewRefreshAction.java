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
