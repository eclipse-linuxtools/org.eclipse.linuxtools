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
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.RunScriptByPathAction;
import org.eclipse.linuxtools.systemtap.ui.ide.actions.RunScriptAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class SystemTapScriptLaunchShortcut implements ILaunchShortcut {

	public void launch(IEditorPart editor, String mode) {
		RunScriptAction action = new RunScriptAction();
		action.init(editor.getSite().getWorkbenchWindow());
		action.setLocalScript(true);
		action.run();
	}

	public void launch(ISelection selection, String mode) {
		RunScriptByPathAction action = new RunScriptByPathAction();
		IPath path = ((IFile)((TreeSelection)selection).getFirstElement()).getLocation();
		action.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), path);
		action.setLocalScript(true);
		action.run();
	}

}
