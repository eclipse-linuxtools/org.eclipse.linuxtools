/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
