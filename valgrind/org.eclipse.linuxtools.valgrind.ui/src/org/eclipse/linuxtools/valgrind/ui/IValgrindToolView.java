/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
