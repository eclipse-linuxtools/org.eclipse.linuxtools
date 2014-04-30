/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Andrew Ferrazzutti
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;

import org.eclipse.linuxtools.internal.systemtap.ui.consolelog.structures.Messages;

/**
 * A class containing all properties relating to a remote run of a SystemTap
 * script, such as user name and password.
 * @since 3.0
 * @author Andrew Ferrazzutti
 */
public class RemoteScriptOptions {

    public final String userName;
    public final String password;
    public final String hostName;

    public RemoteScriptOptions(String userName, String password, String hostName) {
        if (userName == null || password == null || hostName == null) {
            throw new IllegalArgumentException(Messages.RemoteScriptOptions_invalidArguments);
        }
        this.userName = userName;
        this.password = password;
        this.hostName = hostName;
    }
}
