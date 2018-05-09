/*******************************************************************************
 * Copyright (c) 2011, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Otavio Busatto Pontes <obusatto@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tools.launch.core;

/**
 * Constants to make using the Path extension easier.
 *
 * @since 2.0
 */
public interface LaunchCoreConstants {
    String PLUGIN_ID = "org.eclipse.linuxtools.tools.launch.core"; //$NON-NLS-1$
    String LINUXTOOLS_PATH_NAME = LaunchCoreConstants.PLUGIN_ID + ".LinuxtoolsPath"; //$NON-NLS-1$
    String LINUXTOOLS_PATH_SYSTEM_NAME = LaunchCoreConstants.PLUGIN_ID + ".LinuxtoolsSystemEnvPath"; //$NON-NLS-1$
}
