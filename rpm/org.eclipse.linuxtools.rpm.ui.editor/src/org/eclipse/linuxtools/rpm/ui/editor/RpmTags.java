/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor;


/**
 * Definitions of all rpm tags.
 *
 */
public interface RpmTags {
	String SUMMARY = "Summary";
	String NAME = "Name";
	String VERSION = "Version";
	String PACKAGER = "Packager";
	String ICON = "Icon";
	String URL = "URL";
	String PREFIX = "Prefix";
	String GROUP = "Group";
	String LICENSE = "License";
	String RELEASE = "Release";
	String BUILD_ROOT = "BuildRoot";
	String DISTRIBUTION = "Distribution";
	String VENDOR =	"Vendor";
	String PROVIDES = "Provides";
	String EXCLUSIVE_ARCH = "ExclusiveArch";
	String EXCLUDE_ARCH = "ExcludeArch";
	String EXCLUDE_OS = "ExclusiveOS";
	String BUILD_ARCH = "BuildArch";
	String BUILD_ARCHITECTURES = "BuildArchitectures";
	String AUTO_REQUIRES = "AutoRequires";
	String AUTO_REQ = "AutoReq";
	String AUTO_REQ_PROV = "AutoReqProv";
	String AUTO_PROV = "AutoProv";
	String EPOCH = "Epoch";
}
