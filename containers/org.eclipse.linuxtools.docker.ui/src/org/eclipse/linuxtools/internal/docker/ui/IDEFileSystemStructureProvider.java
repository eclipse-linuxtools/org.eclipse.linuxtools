/*******************************************************************************
 * Copyright (c) 2000, 2015, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - modified for use with Docker Tooling
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * This class provides information regarding the structure and content of
 * specified file system File objects.
 *
 * class copied from
 * org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider as its
 * singleton
 */
public class IDEFileSystemStructureProvider
		implements IImportStructureProvider {

	private Set<String> visitedDirs;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List getChildren(Object element) {
		File folder = (File) element;
		String[] children = folder.list();
		int childrenLength = children == null ? 0 : children.length;
		List result = new ArrayList(childrenLength);

		for (int i = 0; i < childrenLength; i++) {
			File file = new File(folder, children[i]);
			if (isRecursiveLink(file))
				continue;
			result.add(file);
		}

		return result;
	}

	private void initVisitedDirs() {
		if (visitedDirs == null) {
			visitedDirs = new HashSet<>();
		}
	}

	private boolean isRecursiveLink(File childFile) {

		if (childFile.isDirectory()) {
			try {
				String canonicalPath = childFile.getCanonicalPath();
				initVisitedDirs();
				return !visitedDirs.add(canonicalPath);
			} catch (IOException e) {
				Activator.log(e);
			}
		}
		return false;
	}

	@Override
	public InputStream getContents(Object element) {
		try {
			return new FileInputStream((File) element);
		} catch (FileNotFoundException e) {
			Activator.log(e);
			return null;
		}
	}

	@Override
	public String getFullPath(Object element) {
		return ((File) element).getPath();
	}

	@Override
	public String getLabel(Object element) {

		// Get the name - if it is empty then return the path as it is a file
		// root
		File file = (File) element;
		String name = file.getName();
		if (name.length() == 0) {
			return file.getPath();
		}
		return name;
	}

	@Override
	public boolean isFolder(Object element) {
		return ((File) element).isDirectory();
	}

	/**
	 * Clears the visited dir information
	 */
	public void clearVisitedDirs() {
		if (visitedDirs != null)
			visitedDirs.clear();
	}
}
