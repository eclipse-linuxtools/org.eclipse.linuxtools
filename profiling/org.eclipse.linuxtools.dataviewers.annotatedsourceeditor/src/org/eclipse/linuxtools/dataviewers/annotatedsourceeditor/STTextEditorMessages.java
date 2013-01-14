/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class STTextEditorMessages extends NLS {
    private static final String BUNDLE_FOR_CONSTRUCTED_KEYS = "org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.STConstructedTextEditorMessages"; //$NON-NLS-1$
    private static ResourceBundle fgBundleForConstructedKeys = ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);

    /**
     * Returns the message bundle which contains constructed keys.
     * 
     * @since 3.1
     * @return the message bundle
     */
    public static ResourceBundle getBundleForConstructedKeys() {
        return fgBundleForConstructedKeys;
    }

    private static final String BUNDLE_NAME = STTextEditorMessages.class.getName();

    static {
        NLS.initializeMessages(BUNDLE_NAME, STTextEditorMessages.class);
    }

}
