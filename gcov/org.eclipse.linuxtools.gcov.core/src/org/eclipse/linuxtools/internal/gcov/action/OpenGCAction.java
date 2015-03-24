/*******************************************************************************
 * Copyright (c) 2009-2015 STMicroelectronics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.internal.gcov.Activator;
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

    private class GCFilePair {
        final File gcda, gcno;

        private GCFilePair(IPath file) {
            String extension = file.getFileExtension();
            if ("gcno".equals(extension)) { //$NON-NLS-1$
                gcda = file.removeFileExtension().addFileExtension("gcda").toFile(); //$NON-NLS-1$
                gcno = file.toFile();
            } else if ("gcda".equals(extension)) { //$NON-NLS-1$
                gcda = file.toFile();
                gcno = file.removeFileExtension().addFileExtension("gcno").toFile(); //$NON-NLS-1$
            } else {
                gcda = null;
                gcno = null;
            }
        }
    }

    /**
     * Helper method to programmatically show coverage for a given file.
     * @param file The path of the file to view coverage of.
     * @param binaryPath The absolute path of the binary that produced coverage. If invalid,
     * a default binary will be used.
     * @param isCompleteCoverageResultWanted Whether or not to return complete coverage.
     */
    public void autoOpen(final IPath file, final String binaryPath, final boolean isCompleteCoverageResultWanted) {
        final GCFilePair pair = new GCFilePair(file);
        if (isFileValid(pair.gcda) && isFileValid(pair.gcno)) {
            final String safeBinaryPath;
            if (STSymbolManager.sharedInstance.getBinaryObject(binaryPath) == null) {
                safeBinaryPath = getDefaultBinary(file);
            } else {
                safeBinaryPath = binaryPath;
            }

            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    displayCoverage(file, safeBinaryPath, pair.gcda, isCompleteCoverageResultWanted);
                }
            });
        }
    }

    private boolean exists(String binaryFile) {
        File f = new File(binaryFile);
        return f.isFile();
    }

    @Override
    public void open(IPath file) {
        final GCFilePair pair = new GCFilePair(file);
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        if (!isFileValid(pair.gcda)) {
            String msg = NLS.bind(Messages.OpenGCAction_file_dne_run, pair.gcda);
            MessageDialog.openError(shell, Messages.OpenGCAction_gcov_error, msg);
            return;
        }
        if (!isFileValid(pair.gcno)) {
            String msg = NLS.bind(Messages.OpenGCAction_file_dne_compile, pair.gcno);
            MessageDialog.openError(shell, Messages.OpenGCAction_gcov_error, msg);
            return;
        }

        IDialogSettings ds = Activator.getDefault().getDialogSettings();
        IDialogSettings defaultMapping = ds.getSection(OpenGCDialog.class.getName());
        if (defaultMapping == null) {
            defaultMapping = ds.addNewSection(OpenGCDialog.class.getName());
        }
        String defaultBinary = defaultMapping.get(file.toOSString());
        if (defaultBinary == null || defaultBinary.isEmpty() || !exists(defaultBinary)) {
            defaultBinary = getDefaultBinary(file);
        }

        OpenGCDialog d = new OpenGCDialog(shell, defaultBinary, file);
        if (d.open() != Window.OK) {
            return;
        }
        displayCoverage(file, d.getBinaryFile(), pair.gcda, d.isCompleteCoverageResultWanted());
    }

    private void displayCoverage(IPath file, String binaryPath, File gcda, boolean isCompleteCoverageResultWanted) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(file).getProject();
        GcovAnnotationModelTracker.getInstance().addProject(project, new Path(binaryPath));
        GcovAnnotationModelTracker.getInstance().annotateAllCEditors();

        if (isCompleteCoverageResultWanted) {
            CovView.displayCovResults(binaryPath, gcda.getAbsolutePath());
        } else {
            CovView.displayCovDetailedResult(binaryPath, gcda.getAbsolutePath());
        }
    }

    private String getDefaultBinaryFromUserPref(IProject project, IFile infoFile) throws IOException, CoreException {
        if (infoFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(infoFile.getContents(true)))) {
                String line;
                while ((line = br.readLine())!= null){
                    String[] tab = line.split("="); //$NON-NLS-1$
                    if (tab.length > 1){
                        String name = tab[0].trim();
                        if (name.equals("Program Name")){ //$NON-NLS-1$
                            String value = tab[1].trim();
                            IPath p = new Path(value);
                            if (p.isAbsolute()) {
                                if (p.toFile().isFile()) {
                                    return p.toOSString();
                                }
                            } else if (project != null){
                                IFile ifile = project.getFile(value);
                                if (ifile.exists()) {
                                    return ifile.getLocation().toOSString();
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getDefaultBinary(IPath file) {
        IFile c = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(file);
        if (c != null) {
            IProject project = c.getProject();
            if (project != null && project.exists()) {
                IContainer folder = c.getParent();
                IFile infoFile = folder.getFile(new Path("AnalysisInfo.txt")); //$NON-NLS-1$
                try {
                    String defaultBinaryFromUserPref = getDefaultBinaryFromUserPref(project, infoFile);
                    if (defaultBinaryFromUserPref != null) {
                        return defaultBinaryFromUserPref;
                    }
                } catch (IOException|CoreException ex) {
                    // do nothing here.
                }
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

    private boolean isFileValid(File file) {
        return file != null && file.isFile() && file.exists();
    }
}
