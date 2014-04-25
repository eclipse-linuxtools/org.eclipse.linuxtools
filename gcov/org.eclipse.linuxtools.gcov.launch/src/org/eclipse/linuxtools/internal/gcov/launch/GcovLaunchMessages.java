/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.launch;

import org.eclipse.osgi.util.NLS;

public class GcovLaunchMessages extends NLS {

    public static String GcovCompilerOptions_msg;
    public static String GcovCompileAgain_msg;

    static {
        NLS.initializeMessages(GcovLaunchMessages.class.getName(), GcovLaunchMessages.class);
    }

}
