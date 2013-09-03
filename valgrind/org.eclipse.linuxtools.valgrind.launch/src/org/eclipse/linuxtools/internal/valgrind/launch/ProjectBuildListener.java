/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.widgets.Display;

/**
 * Implementation of {@link IResourceChangeListener} that listens for project
 * specific post-build events
 */
public class ProjectBuildListener implements IResourceChangeListener {

	// project to keep track of
	private IProject project;

	public ProjectBuildListener(IProject targetProject) {
		project = targetProject;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (project != null && isPostBuildEvent(event)) {

			// find the project from event delta and  delete its markers
			IResourceDelta delta = event.getDelta();
			IResourceDelta[] childrenDelta = delta.getAffectedChildren(IResourceDelta.CHANGED);
			for (IResourceDelta childDelta : childrenDelta) {
				if (isProjectDelta(childDelta, project)) {

					// clear markers and de-register this listener
					clearProjectMarkers(project);
					ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
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
	public boolean isPostBuildEvent(IResourceChangeEvent event) {
		if(event != null){
			int buildKind = event.getBuildKind();
			return  event.getType() == IResourceChangeEvent.POST_BUILD
					&& (buildKind == IncrementalProjectBuilder.FULL_BUILD
					|| buildKind == IncrementalProjectBuilder.INCREMENTAL_BUILD
					|| buildKind == IncrementalProjectBuilder.CLEAN_BUILD);
		}
		return false;
	}

	/**
	 * Clear markers of specified project.
	 *
	 * @param project IProject project to clear markers from
	 */
	protected void clearProjectMarkers(IProject project) {
		try {

			// remove project markers
			project.deleteMarkers(ValgrindLaunchPlugin.MARKER_TYPE, true,IResource.DEPTH_INFINITE);

			// clear valgrind error view
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					ValgrindUIPlugin.getDefault().resetView();
				}
			});
		} catch (CoreException e) {
			Status status = new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, e.getMessage());
			ValgrindLaunchPlugin.getDefault().getLog().log(status);
		}
	}
}