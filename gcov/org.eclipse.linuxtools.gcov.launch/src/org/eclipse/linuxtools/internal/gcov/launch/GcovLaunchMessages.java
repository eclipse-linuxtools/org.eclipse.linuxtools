/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat Inc. and others.
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
package org.eclipse.linuxtools.internal.gcov.launch;

import org.eclipse.osgi.util.NLS;

public class GcovLaunchMessages extends NLS {

    public static String GcovCompilerOptions_msg;
    public static String GcovCompileAgain_msg;

    public static String
    GcovMissingFlag_Title,
    GcovMissingFlag_MainMsg,
    GcovMissingFlag_CDTInfo,
    GcovMissingFlag_AutotoolsInfo,
    GcovMissingFlag_PostQuestion;

    static {
        NLS.initializeMessages(GcovLaunchMessages.class.getName(), GcovLaunchMessages.class);
    }

}
