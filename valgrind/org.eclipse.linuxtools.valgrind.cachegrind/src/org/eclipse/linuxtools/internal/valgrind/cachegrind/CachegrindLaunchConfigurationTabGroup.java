/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.cachegrind;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindSingleToolOptionsTab;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;

public class CachegrindLaunchConfigurationTabGroup extends
		ProfileLaunchConfigurationTabGroup {

	@Override
	public AbstractLaunchConfigurationTab[] getProfileTabs() {
		return new AbstractLaunchConfigurationTab[] {
			new ValgrindSingleToolOptionsTab("org.eclipse.linuxtools.valgrind.launch.cachegrind"), //$NON-NLS-1$
		};
	}	
	
}
