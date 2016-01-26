/*******************************************************************************
 * Copyright (c) 2009, 2016 Red Hat, Inc.
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

	// The minimum valgrind version supported for testing
	private static final Version MIN_TEST_VER = new Version(3, 7, 0);

	// generateFiles implies runValgrind
	public static final boolean RUN_VALGRIND = versionSupported();

	/**
	 * Compare currently available Valgrind version against the minimum
	 * supported testing version.
	 *
	 * @return <code>true</code> if the current valgrind version is greater than
	 *         or equal to the minimum supported test version, and
	 *         <code>false</code> otherwise.
	 */
	private static boolean versionSupported() {
		Version valgrindVersion = new Version(0, 0, 0);
		try {
			valgrindVersion = ValgrindLaunchPlugin.getDefault().getValgrindVersion(null);
		} catch (CoreException e) {
			return false;
		}
		return valgrindVersion.compareTo(MIN_TEST_VER) >= 0;
	}
}
