/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPerspective;
import org.eclipse.ui.PlatformUI;

public abstract class IDEHandler extends AbstractHandler {

    @Override
    public boolean isEnabled() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().
                getPerspective().getId().equals(IDEPerspective.ID);
    }

}
