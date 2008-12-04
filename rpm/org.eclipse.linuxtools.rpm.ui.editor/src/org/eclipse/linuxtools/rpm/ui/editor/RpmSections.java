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
package org.eclipse.linuxtools.rpm.ui.editor;

/**
 * Definitions of all the rpm sections.
 */
public interface RpmSections {
	
	String PREP_SECTION = "%prep";
	String BUILD_SECTION = "%build";
	String INSTALL_SECTION = "%install";
	String PRETRANS_SECTION = "%pretrans";
	String PRE_SECTION = "%pre";
	String PREUN_SECTION = "%preun";
	String POST_SECTION = "%post";
	String POSTUN_SECTION = "%postun";
	String POSTTRANS_SECTION = "%posttrans";
	String CLEAN_SECTION = "%clean";
	String FILES_SECTION = "%files";
	String CHECK_SECTION = "%check";
	String CHANGELOG_SECTION = "%changelog";
	String PACKAGE_SECTION = "%package";
	String DESCRIPTION_SECTION = "%description";

}
