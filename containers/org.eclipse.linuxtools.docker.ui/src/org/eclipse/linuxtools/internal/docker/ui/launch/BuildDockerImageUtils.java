/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.launch;

import java.io.File;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.docker.core.DockerImage;

/**
 * Utility class for building Docker Images
 */
public class BuildDockerImageUtils {

	/**
	 * Computes the path that can be relative to the workspace or absolute.
	 * 
	 * @param pathLocation
	 *            the base location
	 * @param workspaceRelativeLocation
	 *            flag to indicate if the path is relative to the workspace
	 *            location or not.
	 * @return the path or <code>null</code> if it does not exist
	 */
	public static IPath getPath(final String pathLocation,
			final boolean workspaceRelativeLocation) {
		if (workspaceRelativeLocation) {
			final IResource member = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(new Path(pathLocation));
			if (member != null) {
				return member.getLocation();
			}
		} else if (new File(pathLocation).exists()) {
			return new Path(pathLocation);
		}
		return null;
	}

	/**
	 * Finds and returns the <code>name</code> part of the given full image
	 * name.
	 * 
	 * @param imageName
	 *            the full image name
	 * @return the <code>name</code> part of the given full name or
	 *         <code>null</code> if it could not be found.
	 */
	public static String getRepository(final String imageName) {
		final Matcher imageNameMatcher = DockerImage.imageNamePattern
				.matcher(imageName);
		if (imageNameMatcher.matches()) {
			final String repository = imageNameMatcher.group("repository");
			return repository; //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Finds and returns the <code>name</code> part of the given full image
	 * name.
	 * 
	 * @param imageName
	 *            the full image name
	 * @return the <code>name</code> part of the given full name or
	 *         <code>null</code> if it could not be found.
	 */
	public static String getName(final String imageName) {
		final Matcher imageNameMatcher = DockerImage.imageNamePattern
				.matcher(imageName);
		if (imageNameMatcher.matches()) {
			return imageNameMatcher.group("name"); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Finds and returns the <code>tag</code> part of the given full image name.
	 * 
	 * @param imageName
	 *            the full image name
	 * @return the <code>tag</code> part of the given full name or
	 *         <code>null</code> if it could not be found.
	 */
	public static String getTag(final String imageName) {
		final Matcher imageNameMatcher = DockerImage.imageNamePattern
				.matcher(imageName);
		if (imageNameMatcher.matches()) {
			final String tag = imageNameMatcher.group("tag"); //$NON-NLS-1$
			return tag; //$NON-NLS-1$
		}
		return null;
	}
}
