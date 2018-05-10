/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - original Action implementation and API
 *     Red Hat - migration to Handler implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

public class ImportTapsetHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) {
        String pageID = "org.eclipse.linuxtools.systemtap.prefs.ide.tapsets"; //$NON-NLS-1$
        PreferencesUtil.createPreferenceDialogOn(HandlerUtil.getActiveShell(event), pageID, new String[]{pageID}, null).open();
        return null;
    }

}
