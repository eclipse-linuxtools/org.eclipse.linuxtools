/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *     Jeff Johnston (Red Hat Inc.) - Modified for usage in Linux Tools project
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.tests;
import static org.junit.Assert.fail;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
/**
 * Helper methods to set up a ICProject.
 */
public class CProjectHelper {

    public final static String PLUGIN_ID = "org.eclipse.linuxtools.profiling.tests"; //$NON-NLS-1$

    public static ICProject createCProject(final String projectName, String binFolderName) throws CoreException {
        return createCCProject(projectName, binFolderName);
    }

    /**
     * Creates a ICProject.
     */
    private static ICProject createCProject2(final String projectName, String binFolderName) throws CoreException {
        final IWorkspace ws = ResourcesPlugin.getWorkspace();
        final ICProject newProject[] = new ICProject[1];
        ws.run((IWorkspaceRunnable) monitor -> {
		    IWorkspaceRoot root = ws.getRoot();
		    IProject project = root.getProject(projectName);
		    if (!project.exists()) {
		        project.create(null);
		    } else {
		        project.refreshLocal(IResource.DEPTH_INFINITE, null);
		    }
		    if (!project.isOpen()) {
		        project.open(null);
		    }
		    if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
		        String projectId = PLUGIN_ID + ".TestProject"; //$NON-NLS-1$
		        addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
		        CCorePlugin.getDefault().mapCProjectOwner(project, projectId, false);
		    }
		    addDefaultBinaryParser(project);
		    newProject[0] = CCorePlugin.getDefault().getCoreModel().create(project);
		}, null);

        return newProject[0];
    }

    /**
     * Add the default binary parser if no binary parser configured.
     *
     * @param project
     * @throws CoreException
     */
    private static boolean addDefaultBinaryParser(IProject project) throws CoreException {
        ICConfigExtensionReference[] binaryParsers= CCorePlugin.getDefault().getDefaultBinaryParserExtensions(project);
        if (binaryParsers == null || binaryParsers.length == 0) {
            ICProjectDescription desc= CCorePlugin.getDefault().getProjectDescription(project);
            if (desc == null) {
                return false;
            }

            desc.getDefaultSettingConfiguration().create(CCorePlugin.BINARY_PARSER_UNIQ_ID, CCorePlugin.DEFAULT_BINARY_PARSER_UNIQ_ID);
            CCorePlugin.getDefault().setProjectDescription(project, desc);
        }
        return true;
    }


    private static String getMessage(IStatus status) {
        StringBuffer message = new StringBuffer("[");
        message.append(status.getMessage());
        if (status.isMultiStatus()) {
            IStatus children[] = status.getChildren();
            for( int i = 0; i < children.length; i++) {
                message.append(getMessage(children[i]));
            }
        }
        message.append("]");
        return message.toString();
    }

    private static ICProject createCCProject(final String projectName, final String binFolderName) throws CoreException {
        final IWorkspace ws = ResourcesPlugin.getWorkspace();
        final ICProject newProject[] = new ICProject[1];
        ws.run((IWorkspaceRunnable) monitor -> {
		    ICProject cproject = createCProject2(projectName, binFolderName);
		    if (!cproject.getProject().hasNature(CCProjectNature.CC_NATURE_ID)) {
		        addNatureToProject(cproject.getProject(), CCProjectNature.CC_NATURE_ID, null);
		    }
		    newProject[0] = cproject;
		}, null);
        return newProject[0];
    }

    /**
     * Removes a ICProject.
     */
    public static void delete(ICProject cproject) {
        try {
            cproject.getProject().delete(true, true, null);
        } catch (CoreException e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
            } finally {
                try {
                    System.gc();
                    System.runFinalization();
                    cproject.getProject().delete(true, true, null);
                } catch (CoreException e2) {
                    fail(getMessage(e2.getStatus()));
                }
            }
        }
    }

    private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = proj.getDescription();
        String[] prevNatures = description.getNatureIds();
        String[] newNatures = new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length] = natureId;
        description.setNatureIds(newNatures);
        proj.setDescription(description, monitor);
    }
}