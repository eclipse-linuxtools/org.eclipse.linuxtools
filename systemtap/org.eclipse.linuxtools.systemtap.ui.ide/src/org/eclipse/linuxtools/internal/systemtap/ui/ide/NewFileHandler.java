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

package org.eclipse.linuxtools.internal.systemtap.ui.ide;

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
