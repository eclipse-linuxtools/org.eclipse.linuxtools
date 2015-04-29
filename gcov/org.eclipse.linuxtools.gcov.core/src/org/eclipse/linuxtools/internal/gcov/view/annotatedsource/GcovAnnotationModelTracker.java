/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.view.annotatedsource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.ICEditor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Keep track of windows/parts and listen for opened editors to add
 * the annotation model.
 */
public final class GcovAnnotationModelTracker {

    private static GcovAnnotationModelTracker single;
    private final IWorkbench workbench;
    private final Map<IProject, IPath> trackedProjects = new HashMap<>();

    /**
     * Add/Remove a part listener to every window open/closed.
     */
    private IWindowListener windowListener = new IWindowListener() {
        @Override
        public void windowOpened(IWorkbenchWindow window) {
            window.getPartService().addPartListener(partListener);
        }

        @Override
        public void windowClosed(IWorkbenchWindow window) {
            window.getPartService().removePartListener(partListener);
        }

        @Override
        public void windowActivated(IWorkbenchWindow window) {}

        @Override
        public void windowDeactivated(IWorkbenchWindow window) {}
    };

    /**
     * Add the GcovAnnotationModel to any part that contains an
     * instance of ICEditor.
     */
    private IPartListener2 partListener = new IPartListener2() {
        @Override
        public void partOpened(IWorkbenchPartReference partref) {
            if (partref != null) {
                annotateCEditor(partref);
            }
        }

        @Override
        public void partActivated(IWorkbenchPartReference partRef) {}

        @Override
        public void partBroughtToTop(IWorkbenchPartReference partRef) {}

        @Override
        public void partClosed(IWorkbenchPartReference partRef) {}

        @Override
        public void partDeactivated(IWorkbenchPartReference partRef) {}

        @Override
        public void partHidden(IWorkbenchPartReference partRef) {}

        @Override
        public void partVisible(IWorkbenchPartReference partRef) {}

        @Override
        public void partInputChanged(IWorkbenchPartReference partRef) {}
    };

    private GcovAnnotationModelTracker (IWorkbench workbench) {
        this.workbench = workbench;

        // Add part listener for current windows
        for (IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
            w.getPartService().addPartListener(partListener);
        }

        // Add window listener to workbench for future windows
        workbench.addWindowListener(windowListener);
    }

    public static GcovAnnotationModelTracker getInstance () {
        if (single == null) {
            single = new GcovAnnotationModelTracker(PlatformUI.getWorkbench());
        }
        return single;
    }

    public IPath getBinaryPath (IProject project) {
        return trackedProjects.get(project);
    }

    public boolean containsProject (IProject project) {
        return trackedProjects.containsKey(project);
    }

    public void addProject (IProject project, IPath binary) {
        trackedProjects.put(project, binary);
    }

    public IProject[] getTrackedProjects() {
        return trackedProjects.keySet().toArray(new IProject[0]);
    }

    public void dispose() {
        workbench.removeWindowListener(windowListener);
        for (IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
            w.getPartService().removePartListener(partListener);
        }
    }

    public void annotateAllCEditors() {
        for (IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
            for (IWorkbenchPage p : w.getPages()) {
                for (IEditorReference e : p.getEditorReferences()) {
                    annotateCEditor(e);
                }
            }
        }
    }

    private void annotateCEditor(IWorkbenchPartReference partref) {
        IWorkbenchPart part = partref.getPart(false);
        if (part instanceof ICEditor) {
            ICEditor editor = (ICEditor) part;
            ICElement element = CDTUITools.getEditorInputCElement(editor.getEditorInput());
            IProject project = element.getCProject().getProject();

            // Attach our annotation model to any compatible editor. (ICEditor)
            GcovAnnotationModel.attach((ITextEditor) part);
            // If a user triggers a build we will not render annotations.
            ResourcesPlugin.getWorkspace().addResourceChangeListener(
                    new ProjectBuildListener(project, editor),
                    IResourceChangeEvent.POST_BUILD);
        }
    }


    private class ProjectBuildListener implements IResourceChangeListener {

        // project to keep track of
        private IProject project;
        private ICEditor editor;

        public ProjectBuildListener(IProject targetProject, ICEditor editor) {
            this.editor = editor;
            this.project = targetProject;
        }

        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            if (project != null && isPostBuildEvent(event)) {

                // find the project from event delta and  delete its markers
                IResourceDelta delta = event.getDelta();
                IResourceDelta[] childrenDelta = delta.getAffectedChildren(IResourceDelta.CHANGED);
                for (IResourceDelta childDelta : childrenDelta) {
                    if (isProjectDelta(childDelta, project)) {

                        // do not track this project and de-register this listener
                        GcovAnnotationModel.clear(editor);
                        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
                        trackedProjects.remove(project);
                    }
                }
            }
        }

        /**
         * Check if {@link IResourceDelta} represents a change in the specified {@link IProject}..
         *
         * @param delta IResourceDelta resource delta to check
         * @param project IProject project to compare against
         * @return boolean true if IResourceDelta is a project and equals the
         */
        public boolean isProjectDelta(IResourceDelta delta, IProject project){
            if(delta != null){
                IResource resource = delta.getResource();
                return delta.getKind() == IResourceDelta.CHANGED
                        && resource != null
                        && resource.getType() == IResource.PROJECT
                        && resource.equals(project);
            }
            return false;
        }

        /**
         * Check if {@link IResourceChangeEvent} is a post-build event.
         *
         * @param event IResourceChangeEvent event to check
         * @return boolean true if IResourceChangeEvent is a post-build event, false
         *         otherwise
         */
        private boolean isPostBuildEvent(IResourceChangeEvent event) {
            if(event != null){
                int buildKind = event.getBuildKind();
                return  event.getType() == IResourceChangeEvent.POST_BUILD
                        && (buildKind == IncrementalProjectBuilder.FULL_BUILD
                        || buildKind == IncrementalProjectBuilder.INCREMENTAL_BUILD
                        || buildKind == IncrementalProjectBuilder.CLEAN_BUILD);
            }
            return false;
        }
    }
}
