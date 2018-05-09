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
package org.eclipse.linuxtools.valgrind.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.ui.IViewPart;

/**
 * Provides an interface for including controls in the Valgrind view.
 */
public interface IValgrindToolView extends IViewPart {

    /**
     * The valgrind view id.
     * @since 2.0
     */
    String VIEW_ID = ValgrindUIPlugin.PLUGIN_ID + ".valgrindview"; //$NON-NLS-1$

    /**
     * Provides a mechanism to add actions to the Valgrind view's toolbar.
     * @return An array of actions to add to the toolbar
     */
    IAction[] getToolbarActions();

    /**
     * Refreshes the controls within this view.
     */
    void refreshView();

}
