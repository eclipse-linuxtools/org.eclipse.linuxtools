/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

/**
 * Utility class for createrepo.
 */
public final class CreaterepoUtils {

    private CreaterepoUtils() {}

    /**
     * Find the console to be used, and if none found, create
     * a new console to use.
     *
     * @param name The name of the console.
     * @return The found console or a new one if none found.
     */
    public static MessageConsole findConsole(String name) {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager manager = plugin.getConsoleManager();
        MessageConsole console = null;
        for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
                .getConsoles()) {
            if (cons.getName().equals(name)) {
                console = (MessageConsole) cons;
            }
        }
        // no existing console, create new one
        if (console == null) {
            console = new MessageConsole(name, null, null, true);
        }
        manager.addConsoles(new IConsole[] { console });
        console.clearConsole();
        console.activate();
        return console;
    }

}
