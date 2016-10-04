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
