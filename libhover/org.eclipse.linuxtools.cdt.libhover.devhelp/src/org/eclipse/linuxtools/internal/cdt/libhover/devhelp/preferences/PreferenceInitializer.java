/*******************************************************************************
 * Copyright (c) 2011, 2022 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.FrameworkUtil;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	// FreeDesktop standard environment variable defining the set of base locations
	// to search for system data files
	private final String XDG_DATA_DIRS = "XDG_DATA_DIRS"; //$NON-NLS-1$

	// Default locations to use if the above environment var is not set, see:
	// https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html
	private final List<String> DEFAULT_DATA_DIRS = List.of("/usr/local/share", "/usr/share"); //$NON-NLS-1$ //$NON-NLS-2$

	// Additional default locations to use when running in Flatpak, these are
	// locations on the sandbox host that are accessible if the "--filesystem=host"
	// permission is granted
	private final List<String> DEFAULT_FLATPAK_DATA_DIRS = List.of("/run/host/usr/local/share", "/run/host/usr/share"); //$NON-NLS-1$ //$NON-NLS-2$

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences prefs = DefaultScope.INSTANCE
				.getNode(FrameworkUtil.getBundle(getClass()).getSymbolicName());
		List<Path> locations = computeDefaultDevhelpLocations();
		String defaults = locations.stream().filter(Files::exists).map(Path::toString)
				.collect(Collectors.joining(File.pathSeparator));

		prefs.put(PreferenceConstants.DEVHELP_DIRECTORY, defaults);
	}

	/**
	 * Computes the default list of locations in which to search for Devhelp books.
	 * First we check the <code>XDG_DATA_DIRS</code> environment variable and for
	 * each directory specified, we check the following locations where Devhelp
	 * books commonly live:
	 * <ul>
	 * <li>xdg_data_dir/doc</li>
	 * <li>xdg_data_dir/gtk-doc/html</li>
	 * <li>xdg_data_dir/devhelp/books</li>
	 * </ul>
	 * 
	 * If <code>XDG_DATA_DIRS</code> is empty, then we instead use the directories
	 * given by {@link #DEFAULT_DATA_DIRS}. Additionally if we detect we are running
	 * in a sandbox, then also use the directories given by
	 * {@link #DEFAULT_FLATPAK_DATA_DIRS}.
	 */
	private List<Path> computeDefaultDevhelpLocations() {
		String datadirsEnv = System.getenv(XDG_DATA_DIRS);
		List<String> datadirs = null;
		if (datadirsEnv == null || datadirsEnv.isBlank()) {
			datadirs = DEFAULT_DATA_DIRS;
		} else {
			datadirs = Arrays.asList(datadirsEnv.split(File.pathSeparator));
		}
		List<Path> paths = new ArrayList<>();
		for (String datadir : datadirs) {
			addBookPathsForDataDir(datadir, paths);
		}
		if (Files.exists(Path.of("/.flatpak-info"))) { //$NON-NLS-1$
			for (String datadir : DEFAULT_FLATPAK_DATA_DIRS) {
				addBookPathsForDataDir(datadir, paths);
			}
		}
		return paths;
	}

	private void addBookPathsForDataDir(String datadir, List<Path> bookPaths) {
		bookPaths.add(Path.of(datadir, "doc")); //$NON-NLS-1$
		bookPaths.add(Path.of(datadir, "gtk-doc", "html")); //$NON-NLS-1$ //$NON-NLS-2$
		bookPaths.add(Path.of(datadir, "devhelp", "books")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
