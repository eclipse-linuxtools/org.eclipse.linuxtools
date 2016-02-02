/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * Copy of the org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider
 * with the following changes : - Removed the singleton nature of the original
 * class - Only returns modified files between source and destination
 * directories
 * 
 */
@SuppressWarnings("restriction")
public class SyncFileSystemStructureProvider implements IImportStructureProvider {

	private final IPath source;

	private final IPath destination;

	private final List<String> ignoredRelativePaths;

	/**
	 * Creates an instance of <code>SyncFileSystemStructureProvider</code>.
	 * 
	 * @param source
	 */
	private SyncFileSystemStructureProvider(final IPath source, final IPath destination, final List<String> ignoredRelativePaths) {
		super();
		this.source = source;
		this.destination = destination;
		this.ignoredRelativePaths = ignoredRelativePaths;
	}

	/*
	 * (non-Javadoc) Method declared on IImportStructureProvider
	 */
	@Override
	public List<File> getChildren(Object element) {
		File folder = (File) element;
		String[] children = folder.list();
		int childrenLength = children == null ? 0 : children.length;
		List<File> result = new ArrayList<>(childrenLength);

		for (int i = 0; i < childrenLength; i++) {
			File sourceFile = new File(folder, children[i]);
			IPath relativeSourcePath = new Path(sourceFile.getAbsolutePath()).makeRelativeTo(source);
			// always add the sub directories
			if (ignoredRelativePaths.contains(relativeSourcePath.lastSegment())) {
				continue;
			}
			if (sourceFile.isDirectory() && !ignoredRelativePaths.contains(relativeSourcePath.lastSegment())) {
				result.addAll(getChildren(sourceFile));
			}
			// only add the other files if they are missing or were modified in
			// the destination destination
			else {
				IPath relativeDestinationPath = destination.append(relativeSourcePath);
				File destinationFile = new File(relativeDestinationPath.toOSString());
				if (!destinationFile.exists() || destinationFile.lastModified() < sourceFile.lastModified()) {
					result.add(sourceFile);
				}
			}
		}

		return result;
	}

	@Override
	public InputStream getContents(Object element) {
		try {
			return new FileInputStream((File) element);
		} catch (FileNotFoundException e) {
			IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e);
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
	
	public static class Builder {
		private final IPath source;
		
		private final IPath destination;
		
		private final List<String> ignoredRelativePaths = new ArrayList<>();
	
		public Builder(final IPath source, final IPath destination) {
			super();
			this.source = source;
			this.destination = destination;
		}

		public Builder ignoreRelativeSourcePaths(final String... relativePaths) {
			this.ignoredRelativePaths.addAll(Arrays.asList(relativePaths));
			return this;
		}
		
		public SyncFileSystemStructureProvider build() {
			return new SyncFileSystemStructureProvider(source, destination, ignoredRelativePaths);
		}
	}
	
}
