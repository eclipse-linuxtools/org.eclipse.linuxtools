/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *    Thavidu Ranatunga (IBM) - derived from
 *       org.eclipse.linuxtools.oprofile.launch.configuration.OprofileSetupTab
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.remote.launch;

import org.eclipse.debug.core.ILaunchConfiguration;

public class PerfOptionsTab extends org.eclipse.linuxtools.internal.perf.launch.PerfOptionsTab {

	private Exception ex;

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
	}

	@Override
	public boolean isValid (ILaunchConfiguration config) {
		if (ex != null) {
			setErrorMessage(ex.getLocalizedMessage());
			return false;
		}
		return super.isValid(config);
	}
}
