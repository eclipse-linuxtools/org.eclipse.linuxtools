/*******************************************************************************
 * Copyright (c) 2014 Kalray.eu
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *    Red Hat Inc. - fix #408543
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.launch;


import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.profiling.ui.TitleAreaDialogWithRadioButtons;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * Dialog displayed when no gmon.out is found. This happens after -pg flag options check,
 * meaning that it's probably a makefile project or gmon.out is generated elesewhere.
 * User is promted with Radio options whether he wants to browse the workspace, file system or cancle the launch.
 * He/she can either recompile the project, or search gmon.out file in another location.
 */
public class GprofNoGmonDialog {

    private String gmonExpected;


    /**
     * <h1>Construct a new dialogue for missing gprof file. </h1>
     * <p>
     * Prompt the user to browse workspace or file system if gprof file is not found. <br>
     * Note, this should be instantiated inside a UI thread.
     * </p>
     *
     * @param project The IProject that the user will browse if file is missing.
     * @param shell   the shell on top of which this dialogue will show.
     */
    public GprofNoGmonDialog(IProject project, Shell shell) {

        // Missing gmon.out logic:
        //      This point is reached if pg flags were not set. (e.g in an unmanaged makefile project.)
        //      or PG flag was set but gmon.out could not be found.

        // Construct Dialog for user.
        // Declare a list for the buttons.
        List<Entry<String,String>> buttonList = new ArrayList<>();

        // Add buttons:
        buttonList.add(new SimpleEntry<>("browseWorkSpace", GprofLaunchMessages.GprofNoGmonOut_BrowseWorkSpace)); //$NON-NLS-1$
        buttonList.add(new SimpleEntry<>("browseFileSystem",GprofLaunchMessages.GprofNoGmonOut_BrowseFileSystem)); //$NON-NLS-1$
        buttonList.add(new SimpleEntry<>("cancleLaunch",    GprofLaunchMessages.GprofNoGmonOut_CancleLaunch)); //$NON-NLS-1$

        // Set Dialogue options.
        String title = GprofLaunchMessages.GprofNoGmonOut_title;
        String body  = GprofLaunchMessages.GprofNoGmonOut_body;
        int msgType = IMessageProvider.ERROR;

        // Instantiate & open the dialogue.
        TitleAreaDialogWithRadioButtons gmonMissingDialog =
                new TitleAreaDialogWithRadioButtons(shell, title, body, buttonList, msgType);
        int retVal = gmonMissingDialog.open();

        // Handle user's selection. (OK/ Cancle)
        switch (retVal) {
        case Window.OK:
            // Handle which button the user selected.
            switch (gmonMissingDialog.getSelectedButton()) {
            case "browseWorkSpace": //$NON-NLS-1$
                gmonExpected = browseWorkspaceHandler(shell, project);
                break;
            case "browseFileSystem": //$NON-NLS-1$
                gmonExpected = browseFileSystemHandler(shell, project);
                //gmonExpected = browseFileSystemHandler(parent);
                break;
            default: // this can happen if the user pressed escape.
                gmonExpected = null;
                return;
            }

        case Window.CANCEL:
            return; // Launch cancled if user clicked Cancle.
        default:
            return; // if somethign broke with the dialogue (manual kill, cancle launch).
        }
    }

    /**
     *  Retrieve location of gmon.out file after dialogue(s) completed.
     */
    public String getGmonExpected() {
        return this.gmonExpected;
    }

   /**
    * Browse file sytem to find gmon.out. Return null if bad input.
    */
   private String browseFileSystemHandler(Shell shell, IProject project) {

        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setText(GprofLaunchMessages.GprofNoGmonDialog_OpenGmon);

        // Open Project path.
        if (project != null) {
            dialog.setFilterPath(project.getLocation().toOSString());
        }

        //return gmon.oot path string to caller. null if not selected or user pressed cancle.
        return dialog.open(); //
    }

   /**
    * Handle the case when the user browses the Workspace.
    * @param parent
    * @return gmon.out location or null if bad.
    */
   private String browseWorkspaceHandler(Shell shell, IProject project) {

       //New tree Dialogue.
       ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
               new WorkbenchLabelProvider(), new WorkbenchContentProvider());

       //Set dialogue settings.
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

       //Open dialogue.
       if (dialog.open() == IDialogConstants.OK_ID) {
           IResource resource = (IResource) dialog.getFirstResult();
           return resource.getLocation().toOSString();                     // If things were ok, return the gmon path.
       } else {
           return null;
       }
   }
}