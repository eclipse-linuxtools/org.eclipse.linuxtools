/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat Inc. and others.
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
package org.eclipse.linuxtools.internal.docker.ui;

import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

public class PopulateContainerFilesOperation extends PopulateRootOperation {

	private final FileSystemElement rootParent;

	public PopulateContainerFilesOperation(Object rootObject,
			FileSystemElement rootParent,
			IImportStructureProvider structureProvider) {
		super(rootObject, structureProvider);
		this.rootParent = rootParent;
	}

	@Override
	protected FileSystemElement createElement(FileSystemElement parent,
			Object fileSystemObject) throws InterruptedException {
		FileSystemElement element = (parent == null ? this.rootParent : parent);
		return super.createElement(element, fileSystemObject);
	}
}
