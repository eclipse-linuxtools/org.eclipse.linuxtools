/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.action;

import java.io.File;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.gcov.dialog.OpenGCDialog;
import org.eclipse.linuxtools.internal.gcov.view.CovView;
import org.eclipse.linuxtools.internal.gcov.view.annotatedsource.GcovAnnotationModelTracker;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.PlatformUI;

/**
 * Action performed when user clicks on a gcda/gcno file
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class OpenGCAction implements IEditorLauncher {

    @Override
    public void open(IPath file) {
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        String extension = file.getFileExtension();
        File gcno;
        File gcda;
        if ("gcno".equals(extension)) { //$NON-NLS-1$
            IPath file2 = file.removeFileExtension().addFileExtension("gcda"); //$NON-NLS-1$
            gcno = file.toFile();
            gcda = file2.toFile();
        } else if ("gcda".equals(extension)) { //$NON-NLS-1$
            IPath file2 = file.removeFileExtension().addFileExtension("gcno"); //$NON-NLS-1$
            gcda = file.toFile();
            gcno = file2.toFile();
        } else {
            // should never occurs
            return;
        }

        if (gcda == null || !gcda.isFile()) {
            String msg = NLS.bind(Messages.OpenGCAction_file_dne_run, gcda);
            MessageDialog.openError(shell, Messages.OpenGCAction_gcov_error, msg);
            return;
        }
        if (gcno == null || !gcno.isFile()) {
            String msg = NLS.bind(Messages.OpenGCAction_file_dne_compile, gcno);
            MessageDialog.openError(shell, Messages.OpenGCAction_gcov_error, msg);
            return;
        }

        String s = getDefaultBinary(file);
        OpenGCDialog d = new OpenGCDialog(shell, s, file);
        if (d.open() != Window.OK) {
            return;
        }
        String binaryPath = d.getBinaryFile();

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(file).getProject();
        GcovAnnotationModelTracker.getInstance().addProject(project, new Path(binaryPath));
        GcovAnnotationModelTracker.getInstance().annotateAllCEditors();

        if (d.isCompleteCoverageResultWanted()) {
            CovView.displayCovResults(binaryPath, gcda.getAbsolutePath());
        } else {
            CovView.displayCovDetailedResult(binaryPath, gcda.getAbsolutePath());
        }
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
                        IBinary[] b = cproject.getBinaryContainer().getBinaries();
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
