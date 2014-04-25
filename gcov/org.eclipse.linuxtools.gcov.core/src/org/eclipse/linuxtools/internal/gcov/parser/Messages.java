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
package org.eclipse.linuxtools.internal.gcov.parser;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gcov.parser.messages"; //$NON-NLS-1$
    public static String CovManager_Parsing_Done;
    public static String CovManager_Retrieval_Error;
    public static String CovManager_Strings;
    public static String CovManager_Summary;
    public static String CovManager_No_Funcs_Error;
    public static String CovManager_No_FilePath_Error;

    public static String GcdaRecordsParser_content_inconsistent;
    public static String GcdaRecordsParser_func_block_empty;
    public static String GcdaRecordsParser_func_counter_error;
    public static String GcdaRecordsParser_magic_num_error;
    public static String GcdaRecordsParser_checksum_error;
    public static String GcdaRecordsParser_func_not_found;
    public static String GcnoRecordsParser_null_string;
    public static String GcnoRecordsParser_magic_num_error;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
