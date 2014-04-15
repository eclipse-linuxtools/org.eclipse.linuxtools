/*******************************************************************************
 * Copyright (c) 2014 Kalray.eu
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.launch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * Dialog displayed when no gmon.out is found.
 * User can either recompile the project, or search gmon.out file in another location.
 */
public class GprofNoGmonDialog extends MessageDialog {

    private final IProject project;
    private String gmonPath = null;

    private static String[] buildButtons() {
        //future enhancement: add a button to edit project settings (managed make project only).
        return new String[] {
                GprofLaunchMessages.GprofNoGmonDialog_Cancel, GprofLaunchMessages.GprofNoGmonDialog_Browse, GprofLaunchMessages.GprofNoGmonDialog_Workspace
        };
    }

    protected GprofNoGmonDialog(Shell parentShell, IProject project) {
        super(parentShell,  GprofLaunchMessages.GprofCompilerOptions_msg, null,
                GprofLaunchMessages.GprofCompileAgain_msg, WARNING, buildButtons(), 0);
        this.project = project;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == 1) {
            FileDialog dialog = new FileDialog(this.getShell(), SWT.OPEN);
            dialog.setText(GprofLaunchMessages.GprofNoGmonDialog_OpenGmon);
            if (project != null) {
                dialog.setFilterPath(project.getLocation().toOSString());
            }
            String s = dialog.open();
            if (s != null) {
                gmonPath = s;
            }
        } else if (buttonId == 2) {
            ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(),
                    new WorkbenchContentProvider());
            dialog.setTitle(GprofLaunchMessages.GprofNoGmonDialog_OpenGmon);
            dialog.setMessage(GprofLaunchMessages.GprofNoGmonDialog_OpenGmon);
            dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
            dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
            dialog.setAllowMultiple(false);
            dialog.setInitialSelection(project);
            dialog.setValidator(new ISelectionStatusValidator() {
                @Override
                public IStatus validate(Object[] selection) {
                    if (selection.length != 1) {
                        return new Status(IStatus.ERROR, GprofLaunch.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
                    }
                    if (!(selection[0] instanceof IFile)) {
                        return new Status(IStatus.ERROR, GprofLaunch.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
                    }
                    return new Status(IStatus.OK, GprofLaunch.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
                }
            });
            if (dialog.open() == IDialogConstants.OK_ID) {
                IResource resource = (IResource) dialog.getFirstResult();
                gmonPath = resource.getLocation().toOSString();
            }
        }

        if (gmonPath == null) {
            setReturnCode(0);
        } else {
            setReturnCode(buttonId);
        }
        close();
    }

    public String getPathToGmon() {
        return gmonPath;
    }

}
