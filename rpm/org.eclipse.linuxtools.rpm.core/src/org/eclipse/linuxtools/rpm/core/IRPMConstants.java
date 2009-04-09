/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.core;

import org.eclipse.linuxtools.rpm.core.internal.Messages;

public interface IRPMConstants {

	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the path to the system's <code>rpm</code> binary.
	 */
	public static final String RPM_CMD = "RPM_CMD"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the path to the system's <code>rpmbuild</code> binary.
	 */
	public static final String RPMBUILD_CMD = "RPMBUILD_CMD"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the path to the system's <code>diff</code> binary.
	 */
	public static final String DIFF_CMD = "DIFF_CMD"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the author's name.
	 */
	public static final String AUTHOR_NAME = "AUTHOR_NAME"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the author's email address.
	 */
	public static final String AUTHOR_EMAIL = "AUTHOR_EMAIL"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the name of the RPM log viewer.
	 */
	public static final String RPM_DISPLAYED_LOG_NAME = "RPM_DISPLAYED_LOG_NAME"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the name of the RPM log.
	 */
	public static final String RPM_LOG_NAME = "RPM_LOG_NAME"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the default RPMS folder in an RPM project.
	 */
	public static final String RPMS_FOLDER = "RPMS"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the default SRPMS folder in an RPM project.
	 */
	public static final String SRPMS_FOLDER = "SRPMS"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the default SPECS folder in an RPM project.
	 */
	public static final String SPECS_FOLDER = "SPECS"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the default SOURCES folder in an RPM project.
	 */
	public static final String SOURCES_FOLDER = "SOURCES"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the default BUILD folder in an RPM project.
	 */
	public static final String BUILD_FOLDER = "BUILD"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the project property used to store the project-relative
	 * path of an RPM project's source RPM.
	 */
	public static final String SRPM_PROPERTY = "SRPM_PROPERTY"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the project property used to store the project-relative
	 * path of an RPM project's spec file.
	 */
	public static final String SPEC_FILE_PROPERTY = "SPEC_FILE_PROPERTY"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the project property used to store an RPM project's
	 * checksum value.
	 */
	public static final String CHECKSUM_PROPERTY = "CHECKSUM_PROPERTY"; //$NON-NLS-1$
	
	/**
	 * Contains the system's file separator.
	 */
	public static final String FILE_SEP = System.getProperty("file.separator"); //$NON-NLS-1$

	/**
	 * Contains the system's line separator.
	 */
	public static final String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$
	
	/**
	 * Contains the plug-ins default error message.
	 */
	public static final String ERROR = Messages.getString("RPMCore.Error_1"); //$NON-NLS-1$
	
}
