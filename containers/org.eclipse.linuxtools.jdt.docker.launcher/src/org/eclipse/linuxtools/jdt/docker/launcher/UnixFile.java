/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.jdt.docker.launcher;

import java.io.File;

/**
 * When our host is Windows, java.io.File doesn't allow us to represent
 * files in the context of a unix filesystem. It will always convert them
 * to Windows paths. Therefore to do things like getAbsolutePath()
 * getName(), etc. we need to have our own convenience class that
 * subclasses java.io.File.
 */
public class UnixFile extends File {

	public static final char separatorChar = '/';
	public static final String separator = "" + separatorChar; //$NON-NLS-1$
	public static final char pathSeparatorChar = ':';
	public static final String pathSeparator = "" + pathSeparatorChar; //$NON-NLS-1$

	private String path;

	public UnixFile(String pathname) {
		super(pathname);
		this.path = pathname;
	}

	public UnixFile(File parent, String child) {
		super(parent, child);
		String parentAbsPath = parent.getAbsolutePath();
		this.path = parentAbsPath.endsWith(separator) //$NON-NLS-1$
				? parentAbsPath + child : parentAbsPath + separator + child; //$NON-NLS-1$
	}

	@Override
	public String getAbsolutePath() {
		return path;
	}

	@Override
	public String getName() {
		int index = path.lastIndexOf(separatorChar);
        return path.substring(index + 1);
	}

	public static String convertDOSPathToUnixPath (String wPath) {
		String result = separator + wPath.replace(pathSeparator, "") //$NON-NLS-1$
		.replace('\\', separatorChar);
		return result;
	}
}
