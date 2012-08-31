/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.callgraph.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;

/**
 * Expansion of the SystemTapLCTG, which was a stripped down version of the ProfileLaunchConfigurationTabGroup
 * 
 * @author chwang
 *
 */
public class SystemTapLaunchConfigurationTabGroup extends SystemTapLCTG{

	@Override
	public AbstractLaunchConfigurationTab[] getProfileTabs() {
		return new AbstractLaunchConfigurationTab[] {
				new SystemTapOptionsTab()
			};
	}

}
