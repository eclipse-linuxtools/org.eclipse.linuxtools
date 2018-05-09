/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc.
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.1
 */
public class ProfilingMessages extends NLS {

    public static String
    errorTitle,
    errorGetProjectType,
    errorGetOptionTemplate,
    errorGetOptionForWriting,
    errorRebuilding,
    errorGetProjectToolname;

    static {
        NLS.initializeMessages(ProfilingMessages.class.getName(), ProfilingMessages.class);
    }

}
