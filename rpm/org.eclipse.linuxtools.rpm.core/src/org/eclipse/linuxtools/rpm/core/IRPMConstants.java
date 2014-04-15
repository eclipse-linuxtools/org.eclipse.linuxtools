/*******************************************************************************
 * Copyright (c) 2005, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core;

import org.eclipse.linuxtools.internal.rpm.core.Messages;

/**
 * Various constants.
 */
public interface IRPMConstants {

    /**
     * Contains the name of the preference store key for storing and retrieving
     * the path to the system's <code>rpm</code> binary.
     */
    String RPM_CMD = "RPM_CMD"; //$NON-NLS-1$

    /**
     * Contains the name of the preference store key for storing and retrieving
     * the path to the system's <code>rpmbuild</code> binary.
     */
    String RPMBUILD_CMD = "RPMBUILD_CMD"; //$NON-NLS-1$

    /**
     * Contains the name of the preference store key for storing and retrieving
     * the path to the system's <code>diff</code> binary.
     */
    String DIFF_CMD = "DIFF_CMD"; //$NON-NLS-1$

    /**
     * Contains the name of the preference store key for storing and retrieving
     * the name of the RPM log viewer.
     */
    String RPM_DISPLAYED_LOG_NAME = "RPM_DISPLAYED_LOG_NAME"; //$NON-NLS-1$

    /**
     * Contains the name of the preference store key for storing and retrieving
     * the name of the RPM log.
     */
    String RPM_LOG_NAME = "RPM_LOG_NAME"; //$NON-NLS-1$

    /**
     * Contains the name of the default RPMS folder in an RPM project.
     */
    String RPMS_FOLDER = "RPMS"; //$NON-NLS-1$

    /**
     * Contains the name of the default SRPMS folder in an RPM project.
     */
    String SRPMS_FOLDER = "SRPMS"; //$NON-NLS-1$

    /**
     * Contains the name of the default SPECS folder in an RPM project.
     */
    String SPECS_FOLDER = "SPECS"; //$NON-NLS-1$

    /**
     * Contains the name of the default SOURCES folder in an RPM project.
     */
    String SOURCES_FOLDER = "SOURCES"; //$NON-NLS-1$

    /**
     * Contains the name of the default BUILD folder in an RPM project.
     */
    String BUILD_FOLDER = "BUILD"; //$NON-NLS-1$

    /**
     * Contains the name of the project property used to store the project-relative
     * path of an RPM project's source RPM.
     */
    String SRPM_PROPERTY = "SRPM_PROPERTY"; //$NON-NLS-1$

    /**
     * Contains the name of the project property used to store the project-relative
     * path of an RPM project's spec file.
     */
    String SPEC_FILE_PROPERTY = "SPEC_FILE_PROPERTY"; //$NON-NLS-1$

    /**
     * Contains the name of the project property used to store an RPM project's
     * checksum value.
     */
    String CHECKSUM_PROPERTY = "CHECKSUM_PROPERTY"; //$NON-NLS-1$

    /**
     * Contains the system's file separator.
     */
    String FILE_SEP = System.getProperty("file.separator"); //$NON-NLS-1$

    /**
     * Contains the system's line separator.
     */
    String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$

    /**
     * Contains the plug-ins default error message.
     */
    String ERROR = Messages.getString("RPMCore.Error_1"); //$NON-NLS-1$

    /**
     * The ID of the org.eclipse.linuxtools.rpm.core bundle.
     */
    String RPM_CORE_ID = "org.eclipse.linuxtools.rpm.core"; //$NON-NLS-1$

    /**
     * The unique nature ID associated with the RPM project nature.
     */
    String RPM_NATURE_ID = RPM_CORE_ID + ".rpmnature"; //$NON-NLS-1$

}
