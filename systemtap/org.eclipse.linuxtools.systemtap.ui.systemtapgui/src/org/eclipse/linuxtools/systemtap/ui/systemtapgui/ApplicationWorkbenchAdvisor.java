/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.systemtapgui;

import org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.SystemTapGUIPlugin;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.systemtapgui.preferences.PreferenceConstants;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;



public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	private static final String PERSPECTIVE_ID = Perspective.ID;

	public ApplicationWorkbenchAdvisor() {
		super();
		LogManager.getInstance().begin();
	}
	
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}
	
	/**
	 * Initializes the configurer object, loads preferences.
	 *
	 * @param configurer The IWorkbenchConfigurer object to initialize.
	 */
	public void initialize(IWorkbenchConfigurer configurer) {
		LogManager.logDebug("Start initialize: configurer-" + configurer, this); //$NON-NLS-1$
		LogManager.logInfo("Initializing", this); //$NON-NLS-1$
		super.initialize(configurer);
		configurer.setSaveAndRestore(
				SystemTapGUIPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.P_WINDOW_STATE));
		LogManager.logDebug("End initialize:", this); //$NON-NLS-1$
	}
}
