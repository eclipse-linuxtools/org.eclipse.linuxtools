/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

public class ContainerFileProxy {

	private final String path;
	private final String name;
	private final String link;
	private final boolean isFolder;
	private final boolean isLink;

	public ContainerFileProxy(String directory, String name,
			boolean isFolder) {
		this.path = directory + (directory.equals("/") ? "" : "/") + name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.name = name;
		this.isFolder = isFolder;
		this.isLink = false;
		this.link = this.path;
	}

	public ContainerFileProxy(String directory, String name, boolean isFolder,
			boolean isLink, String link) {
		this.path = directory + (directory.equals("/") ? "" : "/") + name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.name = name;
		this.isFolder = isFolder;
		this.isLink = isLink;
		this.link = (link == null ? this.path : link);
	}

	public String getFullPath() {
		return path;
	}

	public String getLabel() {
		if (name.isEmpty())
			return "/"; //$NON-NLS-1$
		return name;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public boolean isLink() {
		return isLink;
	}

	public String getLink() {
		return link;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getFullPath();
	}

}
