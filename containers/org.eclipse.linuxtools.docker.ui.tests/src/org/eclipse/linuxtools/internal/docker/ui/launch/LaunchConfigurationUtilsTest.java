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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.data.MapEntry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.internal.docker.core.DockerPortBinding;
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

	@Test
	public void shouldSerializeEmptyPortBindingsFromMap() {
		// given
		final Map<String, List<IDockerPortBinding>> bindings = new HashMap<>();
		// when
		final List<String> result = LaunchConfigurationUtils.serializePortBindings(bindings);
		// then
		assertThat(result).isEmpty();
	}

	@Test
	public void shouldSerializePortBindingsFromMap() {
		// given
		final Map<String, List<IDockerPortBinding>> bindings = new HashMap<>();
		bindings.put("8080/tcp",
				Arrays.asList(new DockerPortBinding("1.2.3.4", "8080"), new DockerPortBinding(null, "8080")));
		bindings.put("9090/tcp",
				Arrays.asList(new DockerPortBinding("1.2.3.4", "9090"), new DockerPortBinding(null, "9090")));
		// when
		final List<String> result = LaunchConfigurationUtils.serializePortBindings(bindings);
		// then
		assertThat(result).containsExactly("8080/tcp:1.2.3.4:8080", "8080/tcp::8080", "9090/tcp:1.2.3.4:9090",
				"9090/tcp::9090");
	}

	@Test
	public void shouldNotSerializeNullPortBindingsFromMap() {
		// when
		final List<String> result = LaunchConfigurationUtils
				.serializePortBindings((Map<String, List<IDockerPortBinding>>) null);
		// then
		assertThat(result).isEmpty();
	}

	@Test
	public void shouldSerializeEmptyPortBindingsFromSet() {
		// given
		final Set<String> bindings = new HashSet<>();
		// when
		final List<String> result = LaunchConfigurationUtils.serializePortBindings(bindings);
		// then
		assertThat(result).isEmpty();
	}

	@Test
	public void shouldSerializePortBindingsFromset() {
		// given
		final Set<String> bindings = new HashSet<>();
		bindings.add("8080/tcp");
		bindings.add("9090/tcp");
		// when
		final List<String> result = LaunchConfigurationUtils.serializePortBindings(bindings);
		// then
		assertThat(result).containsExactly("8080/tcp::8080", "9090/tcp::9090");
	}

	@Test
	public void shouldNotSerializeNullPortBindingsFromSet() {
		// when
		final List<String> result = LaunchConfigurationUtils.serializePortBindings((Set<String>) null);
		// then
		assertThat(result).isEmpty();
	}

	@Test
	public void shouldDeserializeBindings() {
		// given
		final List<String> publishedPorts = Arrays.asList("8080/tcp:1.2.3.4:8080", "8080/tcp::8080",
				"9090/tcp:1.2.3.4:9090", "9090/tcp::9090");
		// when
		final Map<String, List<IDockerPortBinding>> result = LaunchConfigurationUtils
				.deserializePortBindings(publishedPorts);
		// then
		assertThat(result).containsOnly(
				MapEntry.entry("8080/tcp",
						Arrays.asList(new DockerPortBinding("1.2.3.4", "8080"), new DockerPortBinding(null, "8080"))),
				MapEntry.entry("9090/tcp",
						Arrays.asList(new DockerPortBinding("1.2.3.4", "9090"), new DockerPortBinding(null, "9090"))));

	}
}
