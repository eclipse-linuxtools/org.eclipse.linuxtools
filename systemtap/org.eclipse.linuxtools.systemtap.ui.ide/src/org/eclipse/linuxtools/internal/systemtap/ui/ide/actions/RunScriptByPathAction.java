/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sami Wagiaalla
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher.SystemTapScriptTester;
import org.eclipse.linuxtools.systemtap.ui.ide.actions.RunScriptAction;
import org.eclipse.linuxtools.systemtap.ui.ide.actions.RunScriptBaseAction;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This is a child of {@link RunScriptAction} which alloes callers to run a
 * script by only passing in the path to the script. An active editor is not
 * needed
 * 
 * @author Sami Wagiaalla
 * 
 */
public class RunScriptByPathAction extends RunScriptBaseAction {

	IPath path;

	public void init(IWorkbenchWindow window, IPath path) {
		super.init(window);
		this.path = path;
	}

	public void init(IWorkbenchWindow window, String path) {
		init(window, new Path(path));
	}

	@Override
	protected boolean isValid() {
		return this.getFilePath().endsWith(SystemTapScriptTester.STP_SUFFIX)
				&& isValidDirectory(this.getFilePath());
	}

	@Override
	protected String getFilePath() {
		return this.path.toOSString();
	}
}
