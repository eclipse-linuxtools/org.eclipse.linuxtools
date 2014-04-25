/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor;


/**
 * Definitions of all rpm tags.
 *
 */
public interface RpmTags {
    String SUMMARY = "Summary"; //$NON-NLS-1$
    String NAME = "Name"; //$NON-NLS-1$
    String VERSION = "Version"; //$NON-NLS-1$
    String PACKAGER = "Packager"; //$NON-NLS-1$
    String ICON = "Icon"; //$NON-NLS-1$
    String URL = "URL"; //$NON-NLS-1$
    String PREFIX = "Prefix"; //$NON-NLS-1$
    String GROUP = "Group"; //$NON-NLS-1$
    String LICENSE = "License"; //$NON-NLS-1$
    String RELEASE = "Release"; //$NON-NLS-1$
    String BUILD_ROOT = "BuildRoot"; //$NON-NLS-1$
    String DISTRIBUTION = "Distribution"; //$NON-NLS-1$
    String VENDOR =    "Vendor"; //$NON-NLS-1$
    String PROVIDES = "Provides"; //$NON-NLS-1$
    String EXCLUSIVE_ARCH = "ExclusiveArch"; //$NON-NLS-1$
    String EXCLUDE_ARCH = "ExcludeArch"; //$NON-NLS-1$
    String EXCLUDE_OS = "ExclusiveOS"; //$NON-NLS-1$
    String BUILD_ARCH = "BuildArch"; //$NON-NLS-1$
    String BUILD_ARCHITECTURES = "BuildArchitectures"; //$NON-NLS-1$
    String AUTO_REQUIRES = "AutoRequires"; //$NON-NLS-1$
    String AUTO_REQ = "AutoReq"; //$NON-NLS-1$
    String AUTO_REQ_PROV = "AutoReqProv"; //$NON-NLS-1$
    String AUTO_PROV = "AutoProv"; //$NON-NLS-1$
    String EPOCH = "Epoch"; //$NON-NLS-1$
    String OBSOLETES = "Obsoletes"; //$NON-NLS-1$
    String REQUIRES = "Requires";  //$NON-NLS-1$
    String REQUIRES_PRE = "Requires(pre)"; //$NON-NLS-1$
    String REQUIRES_POST = "Requires(post)"; //$NON-NLS-1$
    String REQUIRES_POSTUN = "Requires(postun)"; //$NON-NLS-1$
}
