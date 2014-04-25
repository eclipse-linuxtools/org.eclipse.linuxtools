/*******************************************************************************
 * Copyright (c) 2010, 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.MessageConsole;
/**
 * RpmConsole is used to output rpm/rpmbuild output.
 *
 */
public class RpmConsole extends IOConsole {

    /** Id of this console. */
    public static final String ID = "rpmbuild"; //$NON-NLS-1$
    private RPMProject rpmProject;

    /**
     * Creates the console.
     *
     * @param rpmProject
     *            The RPM project to use.
     */
    public RpmConsole(RPMProject rpmProject) {
        super(ID+'('+rpmProject.getSpecFile().getProject().getName()+')', ID, null, true);
        this.rpmProject = rpmProject;
    }

    /**
     * Returns the spec file for this rpm project.
     *
     * @return The spec file.
     */
    public IResource getSpecfile() {
        return rpmProject.getSpecFile();
    }

    /**
     * Get the console.
     *
     * @param packageName The name of the package(RPM) this console will be for.
     * @return A console instance.
     */
    public static IOConsole findConsole(String packageName) {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        String projectConsoleName = ID+'('+packageName+')';
        IOConsole ret = null;
        for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
                .getConsoles()) {
            if (cons.getName().equals(projectConsoleName)) {
                ret = (MessageConsole) cons;
            }
        }
        // no existing console, create new one
        if (ret == null) {
            ret = new MessageConsole(ID+'('+packageName+')', null, null, true);
        }
        conMan.addConsoles(new IConsole[] { ret });
        ret.clearConsole();
        ret.activate();
        return ret;
    }
}
