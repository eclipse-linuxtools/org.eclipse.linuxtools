/*******************************************************************************
 * Copyright (c) 2006, 2007 2009 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.actions;

import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.cdt.autotools.ui.properties.AutotoolsPropertyConstants;


/**
 * Class responsible for invoking autoconf.
 * 
 * @author klee
 * 
 */
public class InvokeAutoconfAction extends InvokeAction {

	public void run(IAction action) {
		IContainer container = getSelectedContainer();
		if (container == null)
			return;
		
		IPath execDir = getExecDir(container);

		String autoconf_error;
		String autoconf_result;

		if (container != null) {
			IProject project = container.getProject();
			String autoconfCommand = null;
			try {
				autoconfCommand = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_TOOL);
			} catch (CoreException e) {
				// do nothing
			}
			
			// If unset for the project, default to system path
			if (autoconfCommand == null)
				autoconfCommand = "autoconf"; // $NON-NLS-1$
			
			HashMap<String, String> result = executeCommand(new Path(autoconfCommand), new String[]{}, null, execDir);
			
			autoconf_error = (String)result.get("stderr"); //$NON-NLS-1$
			autoconf_result = (String)result.get("stdout"); //$NON-NLS-1$
			
			// if the process produced stdout/err, display in dialog
			if (autoconf_error.length() > 0) {
				showError(InvokeMessages
						.getString("InvokeAutoconfAction.windowTitle.stderr"), //$NON-NLS-1$
						autoconf_error);
			} else if (autoconf_result.length() > 0) {
				showInformation(InvokeMessages
						.getString("InvokeAutoconfAction.windowTitle.stdout"), //$NON-NLS-1$
						autoconf_result);
			} else {
				String args[] = new String[1];
				args[0] = execDir.toOSString();
				showSuccess(InvokeMessages
						.getFormattedString("InvokeAutoconfAction.command", args)); //$NON-NLS-1$
			}
		}
	}


	public void dispose() {
	}
}
