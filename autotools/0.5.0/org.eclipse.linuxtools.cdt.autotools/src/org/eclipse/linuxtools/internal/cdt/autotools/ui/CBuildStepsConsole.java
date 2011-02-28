/*******************************************************************************
 * Copyright (c) 2002, 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Red Hat Inc - Adapted for Autotools usage
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.autotools.ui;

import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;

public class CBuildStepsConsole extends Console {
	IProject project;
	IBuildConsoleManager fConsoleManager;
	
	private static final String CONTEXT_MENU_ID = "CAutotoolsBuildStepsConsole"; //$NON-NLS-1$
	private static final String CONSOLE_NAME = ConsoleMessages.getString("BuildStepsConsole.name"); //$NON-NLS-1$
	
	public CBuildStepsConsole() {
		super(CONSOLE_NAME, CONTEXT_MENU_ID);
	}
}
