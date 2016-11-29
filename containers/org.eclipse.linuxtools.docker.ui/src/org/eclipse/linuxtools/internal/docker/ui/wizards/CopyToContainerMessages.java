package org.eclipse.linuxtools.internal.docker.ui.wizards;

/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Red Hat, Inc - WizardProjectsImportPage[_ArchiveSelectTitle, _SelectArchiveDialogTitle]
 * Red Hat, Inc - copied to Linux Tools Docker UI
 *******************************************************************************/

import org.eclipse.osgi.util.NLS;

public class CopyToContainerMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.docker.ui.wizards.CopyToContainerMessages"; //$NON-NLS-1$

	// ==============================================================================
	// Data Transfer Wizards
	// ==============================================================================
	public static String DataTransfer_fileSystemTitle;

	public static String DataTransfer_browse;
	public static String DataTransfer_selectTypes;
	public static String DataTransfer_selectAll;
	public static String DataTransfer_deselectAll;
	public static String DataTransfer_refresh;
	public static String DataTransfer_cannotOverwrite;
	public static String DataTransfer_emptyString;
	public static String DataTransfer_scanningMatching;
	public static String DataTransfer_information;

	public static String ImportPage_filterSelections;

	public static String ContainerCopyTo_description;
	public static String ContainerCopyTo_title;

	public static String ContainerCopyTo_selectSource;
	public static String ContainerCopyTo_selectSourceTitle;
	public static String ContainerCopyTo_fromDirectory;
	public static String ContainerCopyTo_importFileSystem;
	public static String ContainerCopyTo_overwriteExisting;
	public static String ContainerCopyTo_createTopLevel;
	public static String ContainerCopyTo_createVirtualFolders;
	public static String ContainerCopyTo_importElementsAs;
	public static String ContainerCopyTo_createVirtualFoldersTooltip;
	public static String ContainerCopyTo_createLinksInWorkspace;
	public static String ContainerCopyTo_advanced;
	public static String ContainerCopyTo_noneSelected;
	public static String ContainerCopyTo_cannotImportFilesUnderAVirtualFolder;
	public static String ContainerCopyTo_haveToCreateLinksUnderAVirtualFolder;
	public static String ContainerCopyTo_invalidSource;
	public static String ContainerCopyTo_sourceEmpty;
	public static String ContainerCopyTo_destinationEmpty;
	public static String ContainerCopyTo_importProblems;
	public static String ContainerCopyTo_showAdvanced;
	public static String ContainerCopyTo_hideAdvanced;
	public static String ContainerCopyTo_intoFolder;
	public static String ContainerCopyTo_containerDirectoryMsg;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, CopyToContainerMessages.class);
	}
}

