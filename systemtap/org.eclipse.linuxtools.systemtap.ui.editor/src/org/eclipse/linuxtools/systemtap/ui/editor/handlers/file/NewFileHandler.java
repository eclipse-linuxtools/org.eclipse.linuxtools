/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - original Action implementation and API
 *     Red Hat - migration to Handler implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.editor.handlers.file;

import org.eclipse.linuxtools.internal.systemtap.ui.editor.Localization;
import org.eclipse.swt.SWT;

/**
 * A handler that can be used to create a new SystemTap file.
 * @since 3.0
 */
public class NewFileHandler extends OpenFileHandler {

    @Override
    protected int dialogStyle() {
        return SWT.SAVE;
    }

    @Override
    protected String dialogName() {
        return Localization.getString("NewFileHandler.NewFile"); //$NON-NLS-1$
    }

}
