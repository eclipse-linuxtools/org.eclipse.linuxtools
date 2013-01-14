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

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
final class STRulerMessages extends NLS {

    private static final String BUNDLE_NAME = STRulerMessages.class.getName();

    private STRulerMessages() {
        // Do not instantiate
    }

    public static String AbstractDecoratedTextEditor_revision_quickdiff_switch_title;
    public static String AbstractDecoratedTextEditor_revision_quickdiff_switch_message;
    public static String AbstractDecoratedTextEditor_revision_quickdiff_switch_rememberquestion;

    static {
        NLS.initializeMessages(BUNDLE_NAME, STRulerMessages.class);
    }

}
