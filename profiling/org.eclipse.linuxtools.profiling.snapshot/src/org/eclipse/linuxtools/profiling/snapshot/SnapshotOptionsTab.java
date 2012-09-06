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
package org.eclipse.linuxtools.profiling.snapshot;

import org.eclipse.linuxtools.internal.profiling.provider.ProviderOptionsTab;

/**
 * The options tab used for this plug-in's launch configuration tab group.
 * 
 */
public class SnapshotOptionsTab extends ProviderOptionsTab {

	public String getName() {
		return SnapshotConstants.PLUGIN_NAME;
	}

	@Override
	protected String getProfilingType() {
		return SnapshotConstants.PROFILING_TYPE;
	}

}