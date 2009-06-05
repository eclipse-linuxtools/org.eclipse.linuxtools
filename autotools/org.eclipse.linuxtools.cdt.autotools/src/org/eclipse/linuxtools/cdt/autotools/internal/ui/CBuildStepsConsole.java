/*******************************************************************************
 * Copyright (c) 2002, 2004 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Red Hat Inc - Adapted for Autotools usage
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.internal.ui;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class CBuildStepsConsole implements IConsole {
	IProject project;
	IBuildConsoleManager fConsoleManager;
	
	private static final String CONTEXT_MENU_ID = "CAutotoolsBuildStepsConsole"; //$NON-NLS-1$
	
	public CBuildStepsConsole() {
		String consoleName = ConsoleMessages.getString("BuildStepsConsole.name"); //$NON-NLS-1$
		fConsoleManager = CUIPlugin.getDefault().getConsoleManager(consoleName, 
				CONTEXT_MENU_ID);
	}
	
//	/**
//	 * Constructor for ConfigureConsole.
//	 */
//	public CConfigureConsole() {
//		fConsoleManager = AutotoolsPlugin.getDefault().getConsoleManager();
//	}
//

	public void start(IProject project ) {
		this.project = project;
		fConsoleManager.getConsole(project).start(project);
	}
	
	/**
	 * @throws CoreException
	 * @see org.eclipse.cdt.core.resources.IConsole#getOutputStream()
	 */
	public ConsoleOutputStream getOutputStream() throws CoreException {
		return fConsoleManager.getConsole(project).getOutputStream();
	}

	public ConsoleOutputStream getInfoStream() throws CoreException {
		return fConsoleManager.getConsole(project).getInfoStream();
	}

	public ConsoleOutputStream getErrorStream() throws CoreException {
		return fConsoleManager.getConsole(project).getErrorStream();
	}
}
