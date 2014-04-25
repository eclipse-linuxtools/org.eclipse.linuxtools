/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.forms;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.rpm.ui.editor.forms.messages"; //$NON-NLS-1$
    public static String MainPackagePage_0;
    public static String MainPackagePage_1;
    public static String MainPackagePage_2;
    public static String MainPackagePage_3;
    public static String MainPackagePage_4;
    public static String MainPackagePage_5;
    public static String MainPackagePage_6;
    public static String MainPackagePage_7;
    public static String RpmTagText_0;
    public static String SpecfileFormEditor_0;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        super();
    }
}
