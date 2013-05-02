/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchPlugin;
import org.osgi.framework.Version;


public class ValgrindTestsPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.valgrind.tests"; //$NON-NLS-1$

	// The minimum valgrind version supported for testing
	private static final Version MIN_TEST_VER = new Version(3, 7, 0);

	// Java Runtime System Properties
	/**
	 *  usage: -Declipse.valgrind.tests.generateFiles=<yes|no> [default: no]
	 *  if yes, will run Valgrind and store its output files for each test under
	 *          <plugin root>/valgrindFiles
	 *     no, will use default output directory for valgrind's output
	 */
	public static final String SYSTEM_PROPERTY_GENERATE_FILES = "eclipse.valgrind.tests.generateFiles"; //$NON-NLS-1$
	public static final boolean GENERATE_FILES = System.getProperty(SYSTEM_PROPERTY_GENERATE_FILES, "no").equals("yes"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 *  usage: -Declipse.valgrind.tests.runValgrind=<yes|no> [default: yes]
	 *  if yes, will run Valgrind as in a normal launch
	 *     no, will simulate Valgrind execution with pregenerated log files
	 */
	public static final String SYSTEM_PROPERTY_RUN_VALGRIND = "eclipse.valgrind.tests.runValgrind"; //$NON-NLS-1$
	// generateFiles implies runValgrind
	public static final boolean RUN_VALGRIND = (GENERATE_FILES || System.getProperty(SYSTEM_PROPERTY_RUN_VALGRIND, "yes").equals("yes")) //$NON-NLS-1$ //$NON-NLS-2$ 
			&& versionSupported();

	// Launch config attribute to mock valgrind's exit code
	public static final String ATTR_MOCK_EXIT_CODE = PLUGIN_ID + ".MOCK_EXIT_CODE"; //$NON-NLS-1$

	/**
	 * Compare currently available Valgrind version against the minimum
	 * supported testing version.
	 *
	 * @return <code>true</code> if the current valgrind version is greater than
	 *         or equal to the minimum supported test version, and
	 *         <code>false</code> otherwise.
	 */
	public static boolean versionSupported() {
		Version valgrindVersion = new Version(0, 0, 0);
		try {
			valgrindVersion = ValgrindLaunchPlugin.getDefault()
					.getValgrindVersion(null);
		} catch (CoreException e) {
			return false;
		}
		return valgrindVersion.compareTo(MIN_TEST_VER) >= 0;
	}
}
