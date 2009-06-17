/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc..
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
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.linuxtools.cdt.autotools.ui.properties.AutotoolsPropertyConstants;
import org.eclipse.swt.widgets.Shell;


public class InvokeLibtoolizeAction extends InvokeAction {

	private static final String DEFAULT_OPTION = ""; //$NON-NLS-1$

	public void run(IAction action) {

		IContainer container = getSelectedContainer();
		if (container == null)
			return;
		
		IPath execDir = getExecDir(container);
		String cwd = "CWD:" + getCWD(container);

		InputDialog optionDialog = new SingleInputDialog(
				new Shell(),
				cwd,
				InvokeMessages
						.getString("InvokeLibtoolizeAction.windowTitle.options"), //$NON-NLS-1$
				InvokeMessages
						.getString("InvokeLibtoolizeAction.message.options.otherOptions"), //$NON-NLS-1$
				DEFAULT_OPTION, null);
		optionDialog.open();

		// chop args into string array
		String rawArgList = optionDialog.getValue();

		String[] optionsList = simpleParseOptions(rawArgList);

		String[] argumentList = new String[optionsList.length];

		System.arraycopy(optionsList, 0, argumentList, 0, optionsList.length);

		if (container != null) {
			String autoheaderCommand = null;
			IProject project = getSelectedContainer().getProject();
			try {
				autoheaderCommand = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOHEADER_TOOL);
			} catch (CoreException e) {
				// do nothing
			}
			
			// If unset, use default system path
			if (autoheaderCommand == null)
				autoheaderCommand = "libtoolize"; // $NON-NLS-1$
			
			HashMap<String, String> result = executeCommand(new Path(autoheaderCommand),
					argumentList, null, execDir);

			String autoconf_error = (String)result.get("stderr"); //$NON-NLS-1$
			String autoconf_result = (String)result.get("stdout"); //$NON-NLS-1$

			// if the process produced stdout/err, display in dialog

			if (autoconf_error.length() > 0) {
				showError(InvokeMessages
						.getString("InvokeLibtoolizeAction.windowTitle.stderr"), //$NON-NLS-1$
						autoconf_error);
			} else if (autoconf_result.length() > 0) {
				showInformation(InvokeMessages
						.getString("InvokeLibtoolizeAction.windowTitle.stdout"), //$NON-NLS-1$
						autoconf_result);
			} else {
				showSuccess(InvokeMessages
						.getString("InvokeLibtoolizeAction.command")); //$NON-NLS-1$
			}

		}

	}

	public void dispose() {

	}

}
