/*******************************************************************************
 * Copyright (c) 2008, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
