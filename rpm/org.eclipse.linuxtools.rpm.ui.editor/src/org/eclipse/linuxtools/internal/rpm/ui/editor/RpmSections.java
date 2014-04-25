/*******************************************************************************
 * Copyright (c) 2008 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor;

/**
 * Definitions of all the rpm sections.
 */
public interface RpmSections {

    String PREP_SECTION = "%prep"; //$NON-NLS-1$
    String BUILD_SECTION = "%build"; //$NON-NLS-1$
    String INSTALL_SECTION = "%install"; //$NON-NLS-1$
    String PRETRANS_SECTION = "%pretrans"; //$NON-NLS-1$
    String PRE_SECTION = "%pre"; //$NON-NLS-1$
    String PREUN_SECTION = "%preun"; //$NON-NLS-1$
    String POST_SECTION = "%post"; //$NON-NLS-1$
    String POSTUN_SECTION = "%postun"; //$NON-NLS-1$
    String POSTTRANS_SECTION = "%posttrans"; //$NON-NLS-1$
    String CLEAN_SECTION = "%clean"; //$NON-NLS-1$
    String FILES_SECTION = "%files"; //$NON-NLS-1$
    String CHECK_SECTION = "%check"; //$NON-NLS-1$
    String CHANGELOG_SECTION = "%changelog"; //$NON-NLS-1$
    String PACKAGE_SECTION = "%package"; //$NON-NLS-1$
    String DESCRIPTION_SECTION = "%description"; //$NON-NLS-1$

}
