/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     David Black - bug 198091
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *     Red Hat Inc. - copied over to use with Docker Tooling
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.docker.ui.Activator;

public class ResourceInfoUtils {

	/**
	 * Return whether or not the file called pathName exists.
	 *
	 * @param pathName
	 * @return boolean <code>true</code> if the file exists.
	 * @see IFileInfo#exists()
	 */
	public static boolean exists(String pathName) {
		IFileInfo info = getFileInfo(pathName);
		if (info == null) {
			return false;
		}
		return info.exists();
	}

	/**
	 * Return the fileInfo at pathName or <code>null</code> if the format is
	 * invalid or if the file info cannot be determined.
	 *
	 * @param pathName
	 * @return IFileInfo or <code>null</code>
	 */
	public static IFileInfo getFileInfo(String pathName) {
		IFileStore store = getFileStore(pathName);
		if (store == null) {
			return null;
		}
		return store.fetchInfo();
	}

	/**
	 * Return the fileInfo for location. Return <code>null</code> if there is a
	 * CoreException looking it up
	 *
	 * @param location
	 * @return String or <code>null</code>
	 */
	public static IFileInfo getFileInfo(URI location) {
		if (location.getScheme() == null)
			return null;
		IFileStore store = getFileStore(location);
		if (store == null) {
			return null;
		}
		return store.fetchInfo();
	}

	/**
	 * Get the file store for the local file system path.
	 *
	 * @param string
	 * @return IFileStore or <code>null</code> if there is a
	 *         {@link CoreException}.
	 */
	public static IFileStore getFileStore(String string) {
		Path location = new Path(string);
		// see if there is an existing resource at that location that might have
		// a different file store
		IFile file = ResourcesPlugin.getWorkspace().getRoot()
				.getFileForLocation(location);
		if (file != null) {
			return getFileStore(file.getLocationURI());
		}
		return getFileStore(location.toFile().toURI());
	}

	/**
	 * Get the file store for the URI.
	 *
	 * @param uri
	 * @return IFileStore or <code>null</code> if there is a
	 *         {@link CoreException}.
	 */
	public static IFileStore getFileStore(URI uri) {
		try {
			return EFS.getStore(uri);
		} catch (CoreException e) {
			Activator.log(e);
			return null;
		}
	}

}
