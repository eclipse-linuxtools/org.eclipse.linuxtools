/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.createrepo.tests;


/**
 * Constants used for SWTBot testing.
 */
public interface ICreaterepoTestConstants {

	/*
	 * Dialog specific stuff
	 */
	String MAIN_SHELL = "Resource - Eclipse Platform"; //$NON-NLS-1$
	String FILE = "File"; //$NON-NLS-1$
	String NEW = "New"; //$NON-NLS-1$
	String OTHER = "Other..."; //$NON-NLS-1$
	String NEXT_BUTTON = "Next >"; //$NON-NLS-1$
	String BACK_BUTTON = "< Back"; //$NON-NLS-1$
	String FINISH_BUTTON = "Finish"; //$NON-NLS-1$
	String CANCEL_BUTTON = "Cancel"; //$NON-NLS-1$

	/*
	 * Project Wizard Specific Stuff
	 */
	String PROJECT_NAME_LABEL = "Project name:"; //$NON-NLS-1$
	String CREATEREPO_CATEGORY = "Createrepo"; //$NON-NLS-1$
	String CREATEREPO_PROJECT_WIZARD = "Createrepo Wizard"; //$NON-NLS-1$

	/*
	 * Resources
	 */
	String RPM_RESOURCE_LOC =  "resources" + System.getProperty("file.separator")  //$NON-NLS-1$//$NON-NLS-2$
			+ "rpms" + System.getProperty("file.separator"); //$NON-NLS-1$ //$NON-NLS-2$

	/*
	 * Common createrepo files
	 */
	String REPODATA_FOLDER = "repodata"; //$NON-NLS-1$
	String REPO_MD_NAME = "repomd.xml"; //$NON-NLS-1$

}
