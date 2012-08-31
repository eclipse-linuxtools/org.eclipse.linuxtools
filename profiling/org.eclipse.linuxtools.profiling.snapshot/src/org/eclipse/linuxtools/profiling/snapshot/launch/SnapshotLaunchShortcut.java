/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.snapshot.launch;

import org.eclipse.linuxtools.internal.profiling.provider.launch.ProviderLaunchShortcut;
import org.eclipse.linuxtools.profiling.snapshot.SnapshotProviderPlugin;

/**
 * The launch shortcut for this plug-in.
 *
 */
public class SnapshotLaunchShortcut extends ProviderLaunchShortcut {

	@Override
	protected String getProfilingType() {
		return SnapshotProviderPlugin.PROFILING_TYPE;
	}
}
