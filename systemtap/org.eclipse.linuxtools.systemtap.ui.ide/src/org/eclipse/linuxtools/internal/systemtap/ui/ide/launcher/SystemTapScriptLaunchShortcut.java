/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sami Wagiaalla
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.systemtap.ui.ide.actions.RunScriptHandler;
import org.eclipse.ui.IEditorPart;

public class SystemTapScriptLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(IEditorPart editor, String mode) {
		RunScriptHandler action = new RunScriptHandler();
		action.setLocalScript(true);
		action.execute(null);
	}

	@Override
	public void launch(ISelection selection, String mode) {
		RunScriptHandler action = new RunScriptHandler();
		IPath path = ((IFile)((TreeSelection)selection).getFirstElement()).getLocation();
		action.setPath(path);
		action.setLocalScript(true);
		action.execute(null);
	}

}
