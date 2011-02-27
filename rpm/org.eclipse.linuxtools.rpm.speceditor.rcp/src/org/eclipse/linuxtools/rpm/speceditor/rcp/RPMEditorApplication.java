/*******************************************************************************
 * Copyright (c) 2000, 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.speceditor.rcp;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;


public class RPMEditorApplication implements IApplication {

	private Display display;
	
    public Object start(IApplicationContext context) throws Exception {
        display = PlatformUI.createDisplay();
        DelayedEventsProcessor processor = new DelayedEventsProcessor(display);
    	WorkbenchAdvisor workbenchAdvisor = new RPMEditorWorkbenchAdvisor(processor);
        try {
            int returnCode = PlatformUI.createAndRunWorkbench(display, workbenchAdvisor);
            if (returnCode == PlatformUI.RETURN_RESTART)
                return IApplication.EXIT_RESTART;
 			return IApplication.EXIT_OK;
        } finally {
            display.dispose();
        }
    }

	public void stop() {
		// FIXME: is this ok?
		display.close();
		display.dispose();
	}
}
