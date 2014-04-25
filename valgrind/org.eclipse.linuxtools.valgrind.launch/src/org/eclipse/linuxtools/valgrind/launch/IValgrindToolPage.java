/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.launch;

import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.osgi.framework.Version;

/**
 * Interface for declaring the specifics of integrating a Valgrind tool
 * into this plugin.
 */
public interface IValgrindToolPage extends ILaunchConfigurationTab {

    /**
     * To be called before createControl. This method is used to inform extenders
     * which version of Valgrind is available. The extender may then perform
     * tool-specific version checking. If a value of null is passed, then version
     * checking should not be performed.
     * @param ver - the version of Valgrind, or null
     */
    void setValgrindVersion(Version ver);

}
