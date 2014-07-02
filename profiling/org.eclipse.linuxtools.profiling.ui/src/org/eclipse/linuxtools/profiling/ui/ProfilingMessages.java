/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
