/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	public static String CovManager_Retrieval_Error_title;
    public static String CovManager_Strings;
    public static String CovManager_Summary;
    public static String CovManager_No_Funcs_Error;
    public static String CovManager_No_FilePath_Error;
	public static String CovManager_No_Strings_Windows_Error;

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
