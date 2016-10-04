/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.core.ContainerFileProxy;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

public class ContainerFileSystemProvider implements IImportStructureProvider {

	private final IDockerConnection connection;
	private final String containerId;

	public ContainerFileSystemProvider(IDockerConnection connection,
			String containerId) {
		this.connection = connection;
		this.containerId = containerId;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getChildren(Object element) {
		try {
			ContainerFileProxy proxy = (ContainerFileProxy) element;
			if (proxy.isFolder()) {
				return ((DockerConnection) connection).readContainerDirectory(
						containerId,
						proxy.getFullPath());
			}
		} catch (DockerException e) {
			// do nothing for now
		}
		return new ArrayList<ContainerFileProxy>();
	}

	@Override
	public InputStream getContents(Object element) {
		return null; // we do not have the contents of container file
	}

	@Override
	public String getFullPath(Object element) {
		return ((ContainerFileProxy) element).getFullPath();
	}

	@Override
	public String getLabel(Object element) {
		return ((ContainerFileProxy) element).getLabel();
	}

	@Override
	public boolean isFolder(Object element) {
		return ((ContainerFileProxy) element).isFolder();
	}

}
