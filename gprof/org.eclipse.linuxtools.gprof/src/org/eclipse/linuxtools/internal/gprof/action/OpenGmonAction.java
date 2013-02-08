/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.action;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.gprof.dialog.OpenGmonDialog;
import org.eclipse.linuxtools.internal.gprof.view.GmonView;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.PlatformUI;

/**
 * Action performed when user clicks on a gmon file
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class OpenGmonAction implements IEditorLauncher {

	@Override
	public void open(IPath file) {
		String s = getDefaultBinary(file);
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		OpenGmonDialog d = new OpenGmonDialog(shell, s, file);
		if (d.open() != Window.OK) {
			return;
		}
		String binaryPath = d.getBinaryFile();
		IProject project = null;
		IFile f = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(file);
		if (f != null) project = f.getProject();
		GmonView.displayGprofView(binaryPath, file.toOSString(), project);
	}

	private String getDefaultBinary(IPath file) {
		IProject project = null;
		IFile c = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(file);
		if (c != null) {
		    project = c.getProject();
		    if (project != null && project.exists()) {
		        ICProject cproject = CoreModel.getDefault().create(project);
		        if (cproject != null) {
		            try {
		                IBinary[] b = cproject.getBinaryContainer()
		                        .getBinaries();
		                if (b != null && b.length > 0 && b[0] != null) {
		                    IResource r = b[0].getResource();
		                    return r.getLocation().toOSString();
		                }
		            } catch (CModelException _) {
		            }
		        }
		    }
		}
		return ""; //$NON-NLS-1$
	}
}
