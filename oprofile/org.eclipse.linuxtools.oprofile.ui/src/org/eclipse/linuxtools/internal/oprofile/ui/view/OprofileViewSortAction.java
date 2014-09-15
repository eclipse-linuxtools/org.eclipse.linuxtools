/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brajesh K Rathore <brrathor@linux.vnet.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.ui.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelRoot;

/**
 *
 * Action handler for tree sorting.
 * tree can be sort by session,event,Lib,function and line number.
 * @since 3.0
 *
 */
public class OprofileViewSortAction extends Action {

    public static Map<UiModelRoot.SortType, String> sortTypeMap = new HashMap<>();
    static{
        sortTypeMap.put(UiModelRoot.SortType.DEFAULT, OprofileUiMessages.getString("view.actions.default.label")); //$NON-NLS-1$
        sortTypeMap.put(UiModelRoot.SortType.SESSION, OprofileUiMessages.getString("view.actions.session.label")); //$NON-NLS-1$
        sortTypeMap.put(UiModelRoot.SortType.EVENT, OprofileUiMessages.getString("view.actions.event.label")); //$NON-NLS-1$
        sortTypeMap.put(UiModelRoot.SortType.LIB, OprofileUiMessages.getString("view.actions.lib.label")); //$NON-NLS-1$
        sortTypeMap.put(UiModelRoot.SortType.FUNCTION, OprofileUiMessages.getString("view.actions.function.label")); //$NON-NLS-1$
        sortTypeMap.put(UiModelRoot.SortType.LINE_NO, OprofileUiMessages.getString("view.actions.line.label")); //$NON-NLS-1$
    }
    private UiModelRoot.SortType sortType;

    public OprofileViewSortAction(UiModelRoot.SortType sortType, String text) {
        super(text);
        this.sortType = sortType;
    }

    @Override
    public void run() {
        UiModelRoot.setSortingType(sortType);
        OprofileUiPlugin.getDefault().getOprofileView().refreshView();
    }

}
