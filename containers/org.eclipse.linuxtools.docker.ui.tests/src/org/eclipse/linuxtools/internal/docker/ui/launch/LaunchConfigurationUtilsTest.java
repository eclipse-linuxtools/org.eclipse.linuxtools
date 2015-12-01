/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.launch;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.Platform;
import org.junit.Test;

/**
 * 
 */
public class LaunchConfigurationUtilsTest {
	
	@Test
	public void shouldConvertToUnixPathWhenRunningOnWin32() {
		// given
		final String path = "C:\\path\\to\\data";
		// when
		final String convertedToUnix = LaunchConfigurationUtils.convertToUnixPath(Platform.OS_WIN32, path);
		// then
		assertThat(convertedToUnix).isEqualTo("/c/path/to/data");
	}

	@Test
	public void shouldNotConvertToUnixPathWhenNotRunningOnWin32() {
		// given
		final String path = "/foo/bar";
		// when
		final String convertedToUnix = LaunchConfigurationUtils.convertToUnixPath(Platform.OS_MACOSX, path);
		// then
		assertThat(convertedToUnix).isEqualTo(path);
	}
	
	@Test
	public void shouldConvertToWin32PathWhenRunningOnWin32() {
		// given
		final String path = "/c/path/to/data";
		// when
		final String convertedToUnix = LaunchConfigurationUtils.convertToWin32Path(Platform.OS_WIN32, path);
		// then
		assertThat(convertedToUnix).isEqualTo("C:\\path\\to\\data");
	}

	@Test
	public void shouldNotConvertToWin32PathWhenNotRunningOnWin32() {
		// given
		final String path = "/foo/bar";
		// when
		final String convertedToUnix = LaunchConfigurationUtils.convertToUnixPath(Platform.OS_MACOSX, path);
		// then
		assertThat(convertedToUnix).isEqualTo(path);
	}
	
}
