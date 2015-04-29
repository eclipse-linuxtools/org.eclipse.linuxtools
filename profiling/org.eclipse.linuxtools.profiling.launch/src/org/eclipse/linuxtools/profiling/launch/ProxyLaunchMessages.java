/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.launch;

import org.eclipse.osgi.util.NLS;

/**
 * @since 1.1
 */
public class ProxyLaunchMessages extends NLS {

    public ProxyLaunchMessages() {
    }

    public static String connection_of_copy_from_exe_cannot_be_opened;
    public static String uri_of_copy_from_exe_is_invalid;
    public static String copy_from_exe_is_not_specified;
    public static String copy_from_exe_does_not_exist;
    public static String error_accessing_copy_from_exe;
    public static String copy_from_exe_does_not_have_execution_rights;
    public static String scheme_error_in_copy_from_exe;

    public static String connection_of_executable_cannot_be_opened;
    public static String uri_of_executable_is_invalid;
    public static String executable_is_not_specified;
    public static String executable_does_not_exist;
    public static String error_accessing_executable;
    public static String executable_does_not_have_execution_rights;
    public static String scheme_error_in_executable;

    public static String connection_of_working_directory_cannot_be_opened;
    public static String uri_of_working_directory_is_invalid;
    public static String working_directory_does_not_exist;
    public static String error_accessing_working_directory;
    public static String working_directory_is_not_a_directory;
    public static String scheme_error_in_working_directory;

    public static String scheme_of_working_directory_and_program_do_not_match;
    public static String connection_of_working_directory_and_program_do_not_match;

    /**
     * @since 3.1
     */
    public static String copy_cpp_executable;
    /**
     * @since 3.1
     */
    public static String executable_origin;
    /**
     * @since 3.1
     */
    public static String to;
    /**
     * @since 3.1
     */
    public static String uri_syntax_error;

    static {
        // Load message values from bundle file
        NLS.initializeMessages(ProxyLaunchMessages.class.getCanonicalName(),
                ProxyLaunchMessages.class);
    }

}
