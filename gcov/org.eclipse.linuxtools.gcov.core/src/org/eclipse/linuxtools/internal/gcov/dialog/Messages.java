/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.dialog;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gcov.dialog.messages"; //$NON-NLS-1$
    public static String OpenGCDialog_bin_browser_button_text;
    public static String OpenGCDialog_bin_browser_fs_button_text;
    public static String OpenGCDialog_bin_browser_handler_text;
    public static String OpenGCDialog_no_bin_error_label;
    public static String OpenGCDialog_bin_dne_error_label;
    public static String OpenGCDialog_bin_group_header;
    public static String OpenGCDialog_bin_group_label;
    public static String OpenGCDialog_bin_group_tooltip;
    public static String OpenGCDialog_coverage_mode_header;
    public static String OpenGCDialog_coverage_mode_tooltip;
    public static String OpenGCDialog_invalid_bin_error_title;
    public static String OpenGCDialog_invalid_bin_error_message;
    public static String OpenGCDialog_open_results;
    public static String OpenGCDialog_summ_button_text;
    public static String OpenGCDialog_open_file_button_text;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
