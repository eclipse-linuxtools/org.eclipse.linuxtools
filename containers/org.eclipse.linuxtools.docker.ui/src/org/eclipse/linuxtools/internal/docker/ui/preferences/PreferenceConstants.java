/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	// Time in seconds to wait before automatically refreshing the Containers
	// view (min 5)
	public static final String REFRESH_TIME = "containerRefreshTime"; //$NON-NLS-1$
	
	public static final String AUTOLOG_ON_START = "autoLogOnStart"; //$NON-NLS-1$
	public static final String LOG_TIMESTAMP = "logTimestamp"; //$NON-NLS-1$
	
}
